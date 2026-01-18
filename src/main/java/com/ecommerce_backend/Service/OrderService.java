package com.ecommerce_backend.Service;

import com.ecommerce_backend.Payloads.OrderDto;
import com.ecommerce_backend.Payloads.OrderRequestDto;

public interface OrderService {
    OrderDto placeOrder(OrderRequestDto orderRequestDto);
}
