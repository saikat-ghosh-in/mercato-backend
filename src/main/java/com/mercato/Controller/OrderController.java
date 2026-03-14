package com.mercato.Controller;

import com.mercato.Entity.fulfillment.TransitionTrigger;
import com.mercato.Payloads.Request.*;
import com.mercato.Payloads.Response.*;
import com.mercato.Service.CashfreeService;
import com.mercato.Service.OrderLineUpdateService;
import com.mercato.Service.OrderService;
import com.mercato.Utils.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CashfreeService cashfreeService;
    private final OrderLineUpdateService orderLineUpdateService;
    private final AuthUtil authUtil;

    @PostMapping("/orders/capture")
    public ResponseEntity<OrderPlacementResponseDTO> placeOrder(@RequestBody OrderCaptureRequestDTO orderCaptureRequestDTO) {

        OrderPlacementResponseDTO order = orderService.placeOrder(orderCaptureRequestDTO);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PostMapping(value = "/public/cashfree/webhook", consumes = "application/json")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("x-webhook-signature") String signature,
            @RequestHeader("x-webhook-timestamp") String timestamp) {
        cashfreeService.handleWebhookEvent(payload, signature, timestamp);
        return ResponseEntity.ok("Webhook received");
    }

    @PostMapping("/orders/{orderId}/retry-payment")
    public ResponseEntity<PaymentRetryResponseDTO> retryPayment(@PathVariable String orderId) {
        String paymentSessionId = orderService.retryPayment(orderId);
        return ResponseEntity.ok(new PaymentRetryResponseDTO(paymentSessionId));
    }

    @PostMapping("/orders/{orderId}/verify-payment")
    public ResponseEntity<String> verifyPayment(@PathVariable String orderId) {
        return ResponseEntity.ok(cashfreeService.verifyAndSyncPayment(orderId));
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {

        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/user/orders")
    public ResponseEntity<List<OrderResponseDTO>> getCurrentUserOrders() {

        List<OrderResponseDTO> orders = orderService.getCurrentUserOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/user/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> getCurrentUserOrder(@PathVariable String orderId) {

        OrderResponseDTO order = orderService.getCurrentUserOrder(orderId);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @GetMapping("/user/orders/summary")
    public ResponseEntity<List<OrderSummaryDTO>> getCurrentUserOrderSummaries() {
        return ResponseEntity.ok(orderService.getCurrentUserOrderSummaries());
    }

    @GetMapping("/admin/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable String orderId) {

        OrderResponseDTO order = orderService.getOrder(orderId);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PostMapping("/order-line/update")
    public ResponseEntity<OrderLineResponseDTO> updateOrderLine(@RequestBody @Valid OrderLineUpdateRequestDTO request) {
        TransitionTrigger trigger = authUtil.resolveTransitionTrigger();
        OrderLineResponseDTO response = orderLineUpdateService.updateOrderLine(request, trigger);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/orders/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@RequestBody @Valid OrderCancelRequestDTO request) {
        OrderResponseDTO response = orderService.cancelOrder(request);
        return ResponseEntity.ok(response);
    }
}
