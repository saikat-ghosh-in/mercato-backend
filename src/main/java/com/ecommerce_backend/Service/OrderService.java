package com.ecommerce_backend.Service;

import com.ecommerce_backend.Payloads.Response.OrderDto;
import com.ecommerce_backend.Payloads.Response.OrderRequestDto;

import java.util.List;

public interface OrderService {
    OrderDto placeOrder(OrderRequestDto orderRequestDto);

    List<OrderDto> getAllOrders();

    List<OrderDto> getCurrentUserOrders();

    OrderDto getCurrentUserOrder(String orderNumber);

    OrderDto getOrder(String orderNumber);
}
