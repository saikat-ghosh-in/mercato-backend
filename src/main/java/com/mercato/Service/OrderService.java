package com.mercato.Service;

import com.mercato.Payloads.Request.OrderCancelRequestDTO;
import com.mercato.Payloads.Response.OrderResponseDTO;
import com.mercato.Payloads.Request.OrderCaptureRequestDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO placeOrder(OrderCaptureRequestDTO orderCaptureRequestDTO);

    List<OrderResponseDTO> getAllOrders();

    List<OrderResponseDTO> getCurrentUserOrders();

    OrderResponseDTO getCurrentUserOrder(String orderId);

    OrderResponseDTO getOrder(String orderId);

    OrderResponseDTO cancelOrder(OrderCancelRequestDTO request);
}
