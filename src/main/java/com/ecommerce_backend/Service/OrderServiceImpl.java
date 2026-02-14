package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.*;
import com.ecommerce_backend.Entity.Fulfillment.*;
import com.ecommerce_backend.ExceptionHandler.CustomBadRequestException;
import com.ecommerce_backend.ExceptionHandler.ResourceNotFoundException;
import com.ecommerce_backend.Payloads.Response.OrderDto;
import com.ecommerce_backend.Payloads.Response.OrderLineDto;
import com.ecommerce_backend.Payloads.Response.OrderRequestDto;
import com.ecommerce_backend.Repository.OrderRepository;
import com.ecommerce_backend.Utils.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final AddressService addressService;
    private final AuthUtil authUtil;

    @Override
    @Transactional
    public OrderDto placeOrder(OrderRequestDto orderRequestDto) {

        EcommUser currentUser = authUtil.getLoggedInUser();
        Cart cart = cartService.getCartByUser(currentUser);
        if(cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new CustomBadRequestException("Cart is empty");
        }

        Order order = buildOrder(cart, orderRequestDto, currentUser);

        orderRepository.save(order);
        cartService.clearCart();

        return buildOrderDto(order);
    }

    @Override
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::buildOrderDto)
                .toList();
    }

    @Override
    public List<OrderDto> getCurrentUserOrders() {
        EcommUser currentUser = authUtil.getLoggedInUser();
        List<Order> orders = orderRepository.findAllByCustomerEmail(currentUser.getEmail());
        return orders.stream()
                .map(this::buildOrderDto)
                .toList();
    }

    @Override
    public OrderDto getCurrentUserOrder(String orderNumber) {
        EcommUser currentUser = authUtil.getLoggedInUser();
        Order order = orderRepository.findByOrderNumberAndCustomerEmail(orderNumber, currentUser.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return buildOrderDto(order);
    }

    @Override
    public OrderDto getOrder(String orderNumber) {
        Order order = getOrderByOrderNumber(orderNumber);
        return buildOrderDto(order);
    }

    private Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(()-> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
    }

    private Order buildOrder(Cart cart, OrderRequestDto orderRequestDto, EcommUser customer) {

        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new CustomBadRequestException("Cart is empty");
        }

        Order order = new Order();

        order.setOrderNumber(generateOrderNumber(cart.getCartId()));
        order.setCustomerName(customer.getUsername());
        order.setCustomerEmail(customer.getEmail());
        order.setOrderStatus(OrderStatus.CREATED);

        Address address = addressService.getAddressById(orderRequestDto.getAddressId());
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

            EcommUser seller = productService.getSeller(cartItem.getProduct());
            orderLine.setSellerName(seller.getUsername());
            orderLine.setSellerEmail(seller.getEmail());

            orderLine.setOrderLineStatus(OrderLineStatus.CREATED);

            order.addOrderLine(orderLine);
        }

        order.setCurrency("INR");
        order.setSubtotal(cart.getSubtotal());
        order.setCharges(BigDecimal.ZERO); // will design cart to provide this
        BigDecimal totalAmount = order.getSubtotal().add(order.getCharges());
        order.setTotalAmount(totalAmount);

        paymentService.initiatePayment(order, PaymentMethod.getFromString(orderRequestDto.getPaymentMethod()));

        return order;
    }

    private String generateOrderNumber(Long cartId) {
        String prefix = "ORD";
        String datePart = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("ddMMyyyy"));

        return String.format("%s-%s-%06d", prefix, datePart, cartId);
    }

    private OrderDto buildOrderDto(Order order) {

        List<OrderLineDto> orderLineDtoList = order.getOrderLines().stream()
                .map(this::buildOrderLineDto)
                .toList();

        return new OrderDto(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getOrderStatus().toString(),
                new OrderDto.Customer(
                        order.getCustomerName(),
                        order.getCustomerEmail()
                ),
                new OrderDto.DeliveryAddress(
                        order.getRecipientName(),
                        order.getRecipientPhone(),
                        order.getDeliveryAddressLine1(),
                        order.getDeliveryAddressLine2(),
                        order.getDeliveryCity(),
                        order.getDeliveryState(),
                        order.getDeliveryPincode()
                ),
                paymentService.buildPaymentDto(order.getPayment()),
                orderLineDtoList,
                order.getCurrency(),
                order.getSubtotal(),
                order.getCharges(),
                order.getTotalAmount(),
                null,
                order.getCreateDate(),
                order.getUpdateDate()
        );
    }

    private OrderLineDto buildOrderLineDto(OrderLine orderLine) {
        return new OrderLineDto(
                orderLine.getOrderLineId(),
                orderLine.getOrder().getOrderNumber(),
                orderLine.getOrderLineNumber(),
                orderLine.getOrderLineStatus().toString(),
                new OrderLineDto.ProductDetails(
                        orderLine.getProductId(),
                        orderLine.getProductName(),
                        orderLine.getUnitPrice()
                ),
                new OrderLineDto.Seller(
                        orderLine.getSellerName(),
                        orderLine.getSellerEmail()
                ),
                orderLine.getQuantity(),
                orderLine.getLineTotal(),
                orderLine.getCreateDate(),
                orderLine.getUpdateDate()
        );
    }
}
