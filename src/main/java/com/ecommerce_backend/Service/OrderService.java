package com.ecommerce_backend.Service;

import com.ecommerce_backend.Payloads.Response.OrderResponseDTO;
import com.ecommerce_backend.Payloads.Response.OrderCaptureRequestDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO placeOrder(OrderCaptureRequestDTO orderCaptureRequestDTO);

    List<OrderResponseDTO> getAllOrders();

    List<OrderResponseDTO> getCurrentUserOrders();

    OrderResponseDTO getCurrentUserOrder(String orderNumber);

    OrderResponseDTO getOrder(String orderNumber);
}
