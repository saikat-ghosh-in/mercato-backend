package com.mercato.Controller;

import com.mercato.Entity.fulfillment.TransitionTrigger;
import com.mercato.Payloads.Request.*;
import com.mercato.Payloads.Response.OrderLineResponseDTO;
import com.mercato.Payloads.Response.OrderPlacementResponseDTO;
import com.mercato.Payloads.Response.OrderResponseDTO;
import com.mercato.Payloads.Response.PaymentRetryResponseDTO;
import com.mercato.Service.OrderLineUpdateService;
import com.mercato.Service.OrderService;
import com.mercato.Service.StripeService;
import com.mercato.Utils.AuthUtil;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final StripeService stripeService;
    private final OrderLineUpdateService orderLineUpdateService;
    private final AuthUtil authUtil;

    @PostMapping("/users/orders/capture")
    public ResponseEntity<OrderPlacementResponseDTO> placeOrder(@RequestBody OrderCaptureRequestDTO orderCaptureRequestDTO) {

        OrderPlacementResponseDTO order = orderService.placeOrder(orderCaptureRequestDTO);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PostMapping(value = "/public/stripe/webhook", consumes = "application/json")
    public ResponseEntity<String> handleWebhook(@RequestBody byte[] payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {
        String payloadString = new String(payload, StandardCharsets.UTF_8);
        stripeService.handleWebhookEvent(payloadString, sigHeader);
        return ResponseEntity.ok("Webhook received");
    }

    @PostMapping("/orders/{orderId}/retry-payment")
    public ResponseEntity<PaymentRetryResponseDTO> retryPayment(@PathVariable String orderId) throws StripeException {
        String clientSecret = orderService.retryPayment(orderId);
        return ResponseEntity.ok(new PaymentRetryResponseDTO(clientSecret));
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {

        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/users/orders")
    public ResponseEntity<List<OrderResponseDTO>> getCurrentUserOrders() {

        List<OrderResponseDTO> orders = orderService.getCurrentUserOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/users/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> getCurrentUserOrder(@PathVariable String orderId) {

        OrderResponseDTO order = orderService.getCurrentUserOrder(orderId);
        return new ResponseEntity<>(order, HttpStatus.OK);
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

    @PostMapping("/users/orders/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@RequestBody @Valid OrderCancelRequestDTO request) {
        OrderResponseDTO response = orderService.cancelOrder(request);
        return ResponseEntity.ok(response);
    }
}
