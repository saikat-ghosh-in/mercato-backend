package com.mercato.Service;

import com.mercato.Entity.fulfillment.TransitionTrigger;
import com.mercato.Payloads.Request.OrderCancelRequestDTO;
import com.mercato.Payloads.Response.AdminOrderSummaryDTO;
import com.mercato.Payloads.Response.OrderPlacementResponseDTO;
import com.mercato.Payloads.Response.OrderResponseDTO;
import com.mercato.Payloads.Request.OrderCaptureRequestDTO;
import com.mercato.Payloads.Response.OrderSummaryDTO;

import java.util.List;

public interface OrderService {

    OrderPlacementResponseDTO placeOrder(OrderCaptureRequestDTO orderCaptureRequestDTO);

    String retryPayment(String orderId);

    List<OrderResponseDTO> getAllOrders();

    List<OrderResponseDTO> getCurrentUserOrders();

    OrderResponseDTO getCurrentUserOrder(String orderId);

    List<OrderSummaryDTO> getCurrentUserOrderSummaries();

    List<AdminOrderSummaryDTO> getAllOrderSummaries();

    OrderResponseDTO getOrder(String orderId);

    OrderResponseDTO cancelOrder(OrderCancelRequestDTO request, TransitionTrigger trigger);
}
