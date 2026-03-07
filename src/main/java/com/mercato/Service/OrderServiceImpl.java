package com.mercato.Service;

import com.mercato.Entity.*;
import com.mercato.Entity.cart.Cart;
import com.mercato.Entity.cart.CartItem;
import com.mercato.Entity.fulfillment.*;
import com.mercato.ExceptionHandler.CustomBadRequestException;
import com.mercato.ExceptionHandler.InsufficientInventoryException;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Payloads.Response.OrderResponseDTO;
import com.mercato.Payloads.Response.OrderLineResponseDTO;
import com.mercato.Payloads.Request.OrderCaptureRequestDTO;
import com.mercato.Repository.OrderLineRepository;
import com.mercato.Repository.OrderRepository;
import com.mercato.Repository.ProductRepository;
import com.mercato.Repository.StateTransitionRepository;
import com.mercato.Utils.AuthUtil;
import com.mercato.Utils.CartContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final OrderLineRepository orderLineRepository;
    private final StateTransitionRepository stateTransitionRepository;

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

        return buildOrderDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::buildOrderDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getCurrentUserOrders() {
        EcommUser currentUser = authUtil.getLoggedInUser();
        return orderRepository.findAllByCustomerEmailWithLines(currentUser.getEmail())
                .stream()
                .map(this::buildOrderDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getCurrentUserOrder(String orderId) {
        EcommUser currentUser = authUtil.getLoggedInUser();
        Order order = orderRepository.findByOrderIdAndCustomerEmailWithLines(orderId, currentUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        return buildOrderDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(String orderId) {
        Order order = orderRepository.findByOrderIdWithLines(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        return buildOrderDto(order);
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

    private Order buildOrder(Cart cart, OrderCaptureRequestDTO orderCaptureRequestDTO,
                             EcommUser customer) {
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

    private OrderResponseDTO buildOrderDto(Order order) {
        List<OrderLineResponseDTO> orderLineDTOs = order.getOrderLines().stream()
                .map(this::buildOrderLineDto)
                .toList();

        return new OrderResponseDTO(
                order.getOrderId(),
                order.getOrderStatus().toString(),
                new OrderResponseDTO.Customer(
                        order.getCustomerName(),
                        order.getCustomerEmail()
                ),
                new OrderResponseDTO.DeliveryAddress(
                        order.getRecipientName(),
                        order.getRecipientPhone(),
                        order.getDeliveryAddressLine1(),
                        order.getDeliveryAddressLine2(),
                        order.getDeliveryCity(),
                        order.getDeliveryState(),
                        order.getDeliveryPincode()
                ),
                stripeService.buildPaymentDto(order.getPayment()),
                orderLineDTOs,
                order.getCurrency(),
                order.getSubtotal(),
                order.getCharges(),
                order.getTotalAmount(),
                null,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderLineResponseDTO buildOrderLineDto(OrderLine orderLine) {
        return new OrderLineResponseDTO(
                orderLine.getOrder().getOrderId(),
                orderLine.getOrderLineNumber(),
                orderLine.getOrderLineStatus().toString(),
                new OrderLineResponseDTO.ProductDetails(
                        orderLine.getProductId(),
                        orderLine.getProductName(),
                        orderLine.getUnitPrice()
                ),
                new OrderLineResponseDTO.Seller(
                        orderLine.getSellerName(),
                        orderLine.getSellerEmail()
                ),
                orderLine.getOrderedQty(),
                orderLine.getAcceptedQty(),
                orderLine.getShippedQty(),
                orderLine.getCancelledQty(),
                orderLine.getPendingQty(),
                orderLine.getLineTotal(),
                orderLine.getCreatedAt(),
                orderLine.getUpdatedAt()
        );
    }
}
