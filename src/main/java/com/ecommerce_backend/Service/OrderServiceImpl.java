package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.*;
import com.ecommerce_backend.Entity.Fulfillment.*;
import com.ecommerce_backend.ExceptionHandler.CustomBadRequestException;
import com.ecommerce_backend.ExceptionHandler.ResourceNotFoundException;
import com.ecommerce_backend.Payloads.Response.OrderResponseDTO;
import com.ecommerce_backend.Payloads.Response.OrderLineResponseDTO;
import com.ecommerce_backend.Payloads.Response.OrderCaptureRequestDTO;
import com.ecommerce_backend.Repository.OrderRepository;
import com.ecommerce_backend.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final StripeService stripeService;
    private final AddressService addressService;
    private final AuthUtil authUtil;

    @Override
    @Transactional
    public OrderResponseDTO placeOrder(OrderCaptureRequestDTO orderCaptureRequestDTO) {

        EcommUser currentUser = authUtil.getLoggedInUser();
        Cart cart = cartService.getCartByUser(currentUser);
        if(cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new CustomBadRequestException("Cart is empty");
        }

        Order order = buildOrder(cart, orderCaptureRequestDTO, currentUser);

        orderRepository.save(order);
        cartService.clearCart();

        return buildOrderDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::buildOrderDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getCurrentUserOrders() {
        EcommUser currentUser = authUtil.getLoggedInUser();
        List<Order> orders = orderRepository.findAllByCustomerEmail(currentUser.getEmail());
        return orders.stream()
                .map(this::buildOrderDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getCurrentUserOrder(String orderNumber) {
        EcommUser currentUser = authUtil.getLoggedInUser();
        Order order = orderRepository.findByOrderIdAndCustomerEmail(orderNumber, currentUser.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return buildOrderDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(String orderNumber) {
        Order order = getOrderByOrderNumber(orderNumber);
        return buildOrderDto(order);
    }

    private Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderId(orderNumber)
                .orElseThrow(()-> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
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

        int lineNumber = 1;
        for (CartItem cartItem : cartItems) {
            OrderLine orderLine = new OrderLine();

            orderLine.setOrderLineNumber(lineNumber++); // post-increment orderLineNumber
            orderLine.setProductId(cartItem.getProduct().getProductId());
            orderLine.setProductName(cartItem.getProduct().getProductName());
            orderLine.setUnitPrice(cartItem.getItemPrice());
            orderLine.setQuantity(cartItem.getQuantity());
            orderLine.setLineTotal(cartItem.getLineTotal());
            orderLine.setSellerName(cartItem.getProduct().getProductName());

            EcommUser seller = cartItem.getProduct().getSeller();
            orderLine.setSellerName(seller.getSellerDisplayName());
            orderLine.setSellerEmail(seller.getEmail());

            orderLine.setOrderLineStatus(OrderLineStatus.CREATED);

            order.addOrderLine(orderLine);
        }

        order.setCurrency("INR");
        order.setSubtotal(cart.getSubtotal());
        order.setCharges(BigDecimal.ZERO); // will design cart to provide this
        BigDecimal totalAmount = order.getSubtotal().add(order.getCharges());
        order.setTotalAmount(totalAmount);

        stripeService.initiatePayment(order, PaymentMethod.getFromString(orderCaptureRequestDTO.getPaymentMethod()));

        return order;
    }

    private OrderResponseDTO buildOrderDto(Order order) {

        List<OrderLineResponseDTO> orderLineResponseDTOList = order.getOrderLines().stream()
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
                orderLineResponseDTOList,
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
                orderLine.getQuantity(),
                orderLine.getLineTotal(),
                orderLine.getCreatedAt(),
                orderLine.getUpdatedAt()
        );
    }
}
