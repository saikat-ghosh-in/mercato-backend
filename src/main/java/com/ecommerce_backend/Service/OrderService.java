package com.ecommerce_backend.Service;

import com.ecommerce_backend.Payloads.OrderDto;
import com.ecommerce_backend.Payloads.OrderRequestDto;

import java.util.List;

public interface OrderService {
    OrderDto placeOrder(OrderRequestDto orderRequestDto);

    List<OrderDto> getAllOrders();

    List<OrderDto> getCurrentUserOrders();

    OrderDto getCurrentUserOrder(String orderNumber);

    OrderDto getOrder(String orderNumber);
}
