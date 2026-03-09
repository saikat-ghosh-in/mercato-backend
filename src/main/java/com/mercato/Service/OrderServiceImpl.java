package com.mercato.Service;

import com.mercato.Entity.*;
import com.mercato.Entity.cart.Cart;
import com.mercato.Entity.cart.CartItem;
import com.mercato.Entity.fulfillment.*;
import com.mercato.Entity.fulfillment.payment.PaymentMethod;
import com.mercato.ExceptionHandler.CustomBadRequestException;
import com.mercato.ExceptionHandler.InsufficientInventoryException;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Mapper.OrderMapper;
import com.mercato.Payloads.Request.OrderCancelRequestDTO;
import com.mercato.Payloads.Request.OrderLineUpdateRequestDTO;
import com.mercato.Payloads.Response.OrderResponseDTO;
import com.mercato.Payloads.Request.OrderCaptureRequestDTO;
import com.mercato.Repository.OrderRepository;
import com.mercato.Repository.ProductRepository;
import com.mercato.Utils.AuthUtil;
import com.mercato.Utils.CartContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final StripeService stripeService;
    private final AddressService addressService;
    private final AuthUtil authUtil;
    private final CartReservationService cartReservationService;
    private final ProductRepository productRepository;
    private final OrderLineUpdateService orderLineUpdateService;

    @Override
    @Transactional
    public OrderResponseDTO placeOrder(OrderCaptureRequestDTO orderCaptureRequestDTO) {
        EcommUser currentUser = authUtil.getLoggedInUser();
        Cart cart = cartService.getCartByUser(currentUser);

        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new CustomBadRequestException("Cart is empty");
        }

        cart.getCartItems().forEach(this::clearCartReservationAndValidateStock);

        Order order = buildOrder(cart, orderCaptureRequestDTO, currentUser);
        stripeService.initiatePayment(
                order,
                PaymentMethod.getFromString(orderCaptureRequestDTO.getPaymentMethod())
        );

        orderRepository.save(order);

        CartContext context = new CartContext(currentUser.getUserId(), null);
        cartService.clearCart(context);

        return OrderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getCurrentUserOrders() {
        EcommUser currentUser = authUtil.getLoggedInUser();
        return orderRepository.findAllByCustomerEmailWithLines(currentUser.getEmail())
                .stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getCurrentUserOrder(String orderId) {
        EcommUser currentUser = authUtil.getLoggedInUser();
        Order order = orderRepository.findByOrderIdAndCustomerEmailWithLines(orderId, currentUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        return OrderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(String orderId) {
        Order order = orderRepository.findByOrderIdWithLines(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        return OrderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(OrderCancelRequestDTO request) {
        TransitionTrigger trigger = authUtil.resolveTransitionTrigger();

        Order order;
        if (trigger == TransitionTrigger.ADMIN) {
            order = orderRepository.findByOrderIdWithLines(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", request.getOrderId()));
        } else {
            EcommUser currentUser = authUtil.getLoggedInUser();
            order = orderRepository.findByOrderIdAndCustomerEmailWithLines(
                            request.getOrderId(), currentUser.getEmail()
                    )
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", request.getOrderId()));
        }

        if (trigger != TransitionTrigger.ADMIN) {
            Instant confirmedAt = order.getPayment().getCompletedAt();
            if (confirmedAt == null) {
                throw new CustomBadRequestException("Order has not been confirmed yet");
            }
            if (Instant.now().isAfter(confirmedAt.plusSeconds(6 * 60 * 60))) {
                throw new CustomBadRequestException(
                        "Cancellation window has expired. Orders can only be cancelled within 6 hours of confirmation"
                );
            }
        }

        order.getOrderLines().forEach(line -> {
            if (!line.isTerminal() && line.hasPendingQty()) {
                OrderLineUpdateRequestDTO lineRequest = new OrderLineUpdateRequestDTO();
                lineRequest.setFulfillmentId(line.getFulfillmentId());
                lineRequest.setOrderLineNumber(line.getOrderLineNumber());
                lineRequest.setAction(OrderLineAction.CANCEL);
                lineRequest.setQty(line.getPendingQty());
                lineRequest.setReason(request.getReason());
                orderLineUpdateService.updateOrderLine(lineRequest, trigger);
            }
        });

        Order updatedOrder = orderRepository.findByOrderIdWithLines(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", request.getOrderId()));

        return OrderMapper.toDto(updatedOrder);
    }


    private void clearCartReservationAndValidateStock(CartItem cartItem) {
        cartReservationService.release(cartItem);

        Product product = productRepository.findByProductIdForUpdate(
                        cartItem.getProduct().getProductId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "productId", cartItem.getProduct().getProductId()
                ));

        if (product.getPhysicalQty() < cartItem.getQuantity()) {
            throw new InsufficientInventoryException(
                    product.getProductName(), product.getPhysicalQty()
            );
        }

        product.adjustInventory(-cartItem.getQuantity());
        productRepository.save(product);
    }

    private Order buildOrder(Cart cart, OrderCaptureRequestDTO orderCaptureRequestDTO, EcommUser customer) {
        List<CartItem> cartItems = cart.getCartItems();

        Order order = new Order();
        order.setCustomerName(customer.getUsername());
        order.setCustomerEmail(customer.getEmail());
        order.setOrderStatus(OrderStatus.CREATED);

        Address address = addressService.getAddressById(orderCaptureRequestDTO.getAddressId());
        order.setRecipientName(address.getRecipientName());
        order.setRecipientPhone(address.getRecipientPhone());
        order.setDeliveryAddressLine1(address.getAddressLine1());
        order.setDeliveryAddressLine2(address.getAddressLine2());
        order.setDeliveryCity(address.getCity());
        order.setDeliveryState(address.getState());
        order.setDeliveryPincode(address.getPincode());

        Map<String, String> sellerFulfillmentIds = new HashMap<>();

        int lineNumber = 1;
        for (CartItem cartItem : cartItems) {
            EcommUser seller = cartItem.getProduct().getSeller();

            String fulfillmentId = sellerFulfillmentIds.computeIfAbsent(
                    seller.getEmail(), k -> generateFulfillmentId()
            );

            OrderLine orderLine = new OrderLine();
            orderLine.setFulfillmentId(fulfillmentId);
            orderLine.setOrderLineNumber(lineNumber++);
            orderLine.setProductId(cartItem.getProduct().getProductId());
            orderLine.setProductName(cartItem.getProduct().getProductName());
            orderLine.setUnitPrice(cartItem.getItemPrice());
            orderLine.setOrderedQty(cartItem.getQuantity());
            orderLine.setLineTotal(cartItem.getLineTotal());
            orderLine.setSellerName(seller.getSellerDisplayName());
            orderLine.setSellerEmail(seller.getEmail());
            orderLine.setOrderLineStatus(OrderLineStatus.CREATED);

            order.addOrderLine(orderLine);
        }

        order.setCurrency("INR");
        order.setSubtotal(cart.getSubtotal());
        order.setCharges(cart.getTotalCharges());
        order.setTotalAmount(order.getSubtotal().add(order.getCharges()));

        return order;
    }

    private String generateFulfillmentId() {
        String datePart = LocalDate.now().toString().replace("-", "");
        String randomPart = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase();
        return "FUL-" + datePart + "-" + randomPart;
    }
}
