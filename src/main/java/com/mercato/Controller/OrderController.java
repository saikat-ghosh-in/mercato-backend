package com.mercato.Controller;

import com.mercato.Entity.fulfillment.TransitionTrigger;
import com.mercato.Payloads.Request.*;
import com.mercato.Payloads.Response.FulfillmentOrderResponseDTO;
import com.mercato.Payloads.Response.OrderLineResponseDTO;
import com.mercato.Payloads.Response.OrderResponseDTO;
import com.mercato.Payloads.Response.PaymentConfirmationResponseDTO;
import com.mercato.Service.OrderLineUpdateService;
import com.mercato.Service.OrderService;
import com.mercato.Service.SellerService;
import com.mercato.Service.StripeService;
import com.mercato.Utils.AuthUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
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
    private final StripeService stripeService;
    private final OrderLineUpdateService orderLineUpdateService;
    private final SellerService sellerService;
    private final AuthUtil authUtil;

    @PostMapping("/users/orders/capture")
    public ResponseEntity<OrderResponseDTO> placeOrder(@RequestBody OrderCaptureRequestDTO orderCaptureRequestDTO) {

        OrderResponseDTO order = orderService.placeOrder(orderCaptureRequestDTO);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PostMapping("/users/orders/create-payment-intent")
    public ResponseEntity<String> createStripePaymentIntent(@RequestBody StripePaymentRequestDTO stripePaymentRequestDTO) throws StripeException {

        PaymentIntent paymentIntent = stripeService.createStripePaymentIntent(stripePaymentRequestDTO);
        return new ResponseEntity<>(paymentIntent.getClientSecret(), HttpStatus.CREATED);
    }

    @PostMapping("/public/orders/payment-confirmation")
    public ResponseEntity<PaymentConfirmationResponseDTO> confirmPayment(@RequestBody PaymentConfirmationRequestDTO paymentConfirmationRequestDTO) {

        PaymentConfirmationResponseDTO paymentConfirmationResponseDTO = stripeService.confirmPayment(paymentConfirmationRequestDTO);
        return new ResponseEntity<>(paymentConfirmationResponseDTO, HttpStatus.OK);
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

    @GetMapping("/seller/fulfillment-orders")
    public ResponseEntity<List<FulfillmentOrderResponseDTO>> getAllFulfillmentOrders() {
        return ResponseEntity.ok(sellerService.getAllFulfillmentOrders());
    }

    @GetMapping("/seller/fulfillment-orders/{fulfillmentId}")
    public ResponseEntity<FulfillmentOrderResponseDTO> getFulfillmentOrder(@PathVariable String fulfillmentId) {
        return ResponseEntity.ok(sellerService.getFulfillmentOrder(fulfillmentId));
    }
}
