package com.mercato.Service;

import com.mercato.Entity.*;
import com.mercato.Entity.cart.Cart;
import com.mercato.Entity.cart.CartItem;
import com.mercato.Entity.fulfillment.*;
import com.mercato.Entity.fulfillment.payment.Payment;
import com.mercato.Entity.fulfillment.payment.PaymentMethod;
import com.mercato.Entity.fulfillment.payment.PaymentStatus;
import com.mercato.ExceptionHandler.CustomBadRequestException;
import com.mercato.ExceptionHandler.InsufficientInventoryException;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Mapper.OrderMapper;
import com.mercato.Payloads.Request.OrderCancelRequestDTO;
import com.mercato.Payloads.Request.OrderLineUpdateRequestDTO;
import com.mercato.Payloads.Response.OrderPlacementResponseDTO;
import com.mercato.Payloads.Response.OrderResponseDTO;
import com.mercato.Payloads.Request.OrderCaptureRequestDTO;
import com.mercato.Repository.AddressRepository;
import com.mercato.Repository.OrderRepository;
import com.mercato.Repository.PaymentRepository;
import com.mercato.Repository.ProductRepository;
import com.mercato.Utils.AuthUtil;
import com.mercato.Utils.CartContext;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final StripeService stripeService;
    private final AddressRepository addressRepository;
    private final AuthUtil authUtil;
    private final CartReservationService cartReservationService;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final OrderLineUpdateService orderLineUpdateService;

    @Override
    @Transactional
    public OrderPlacementResponseDTO placeOrder(OrderCaptureRequestDTO orderCaptureRequestDTO) {
        EcommUser user = authUtil.getLoggedInUser();
        Cart cart = cartService.getCartByUser(user);

        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new CustomBadRequestException("Cart is empty");
        }

        Order order = buildOrder(cart, orderCaptureRequestDTO, user);

        cart.getCartItems().forEach(this::clearCartReservationAndValidateStock);
        orderRepository.save(order);

        CartContext context = new CartContext(user.getUserId(), null);
        cartService.clearCart(context);

        try {
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(order, user);
            stripeService.initiatePayment(
                    order,
                    PaymentMethod.getFromString(orderCaptureRequestDTO.getPaymentMethod()),
                    paymentIntent.getId(),
                    paymentIntent.getClientSecret()
            );
        } catch (StripeException e) {
            throw new CustomBadRequestException(
                    "Payment initiation failed: " + e.getMessage()
            );
        }

        return new OrderPlacementResponseDTO(
                order.getPayment().getClientSecret(),
                OrderMapper.toDto(order)
        );
    }

    @Override
    @Transactional
    public String retryPayment(String orderId) throws StripeException {
        EcommUser currentUser = authUtil.getLoggedInUser();
        Order order = orderRepository.findByOrderIdAndCustomerEmailWithLines(orderId, currentUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));

        if (!OrderStatus.CREATED.equals(order.getOrderStatus())) {
            throw new CustomBadRequestException("Order is not in a payable state");
        }

        Payment payment = order.getPayment();

        if (payment == null) {
            throw new CustomBadRequestException("No payment found for this order");
        }

        if (PaymentStatus.SUCCESS.equals(payment.getStatus())) {
            throw new CustomBadRequestException("Order is already paid");
        }

        String stripeStatus = PaymentIntent.retrieve(payment.getGatewayReference()).getStatus();

        if ("requires_payment_method".equals(stripeStatus) || "requires_confirmation".equals(stripeStatus)) {
            return payment.getClientSecret();
        }

        if ("canceled".equals(stripeStatus)) {
            PaymentIntent newIntent = stripeService.createPaymentIntent(order, currentUser);
            payment.setStatus(PaymentStatus.INITIATED);
            payment.setGatewayReference(newIntent.getId());
            payment.setClientSecret(newIntent.getClientSecret());
            payment.setGatewayResponseMessage(null);
            payment.setCompletedAt(null);
            payment.setInitiatedAt(Instant.now());
            paymentRepository.save(payment);
            return newIntent.getClientSecret();
        }

        throw new CustomBadRequestException("Payment cannot be retried at this time, stripe status: " + stripeStatus);
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

        Payment payment = order.getPayment();
        if (!PaymentStatus.SUCCESS.equals(payment.getStatus())) {
            if (PaymentStatus.CANCELLED.equals(payment.getStatus())) {
                throw new CustomBadRequestException("Order is already cancelled");
            }
            try {
                stripeService.cancelPaymentIntent(payment.getGatewayReference());
            } catch (StripeException e) {
                log.warn("Failed to cancel PaymentIntent on Stripe: {}", e.getMessage());
            }
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setGatewayResponseMessage("Cancelled by user before payment");
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            return OrderMapper.toDto(order);
        }

        if (trigger != TransitionTrigger.ADMIN) {
            Instant confirmedAt = payment.getCompletedAt();
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

        if (product.getAvailableQty() < cartItem.getQuantity()) {
            throw new InsufficientInventoryException(
                    product.getProductName(), product.getAvailableQty()
            );
        }
    }

    private Order buildOrder(Cart cart, OrderCaptureRequestDTO orderCaptureRequestDTO, EcommUser customer) {
        List<CartItem> cartItems = cart.getCartItems();

        Order order = new Order();
        order.setCustomerName(customer.getUsername());
        order.setCustomerEmail(customer.getEmail());
        order.setOrderStatus(OrderStatus.CREATED);

        Address address = addressRepository.findByAddressId(orderCaptureRequestDTO.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address", "addressId", orderCaptureRequestDTO.getAddressId())
                );
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
