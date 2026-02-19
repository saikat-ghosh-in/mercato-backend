package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Payloads.Request.PaymentConfirmationRequestDTO;
import com.ecommerce_backend.Payloads.Request.StripePaymentRequestDTO;
import com.ecommerce_backend.Payloads.Response.OrderDto;
import com.ecommerce_backend.Payloads.Response.OrderRequestDto;
import com.ecommerce_backend.Payloads.Response.PaymentConfirmationResponseDTO;
import com.ecommerce_backend.Service.OrderService;
import com.ecommerce_backend.Service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
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

    @PostMapping("/users/orders/capture")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderRequestDto orderRequestDto) {

        OrderDto order = orderService.placeOrder(orderRequestDto);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PostMapping("/users/orders/create-payment-intent")
    public ResponseEntity<String> createStripePaymentIntent(@RequestBody StripePaymentRequestDTO stripePaymentRequestDTO) throws StripeException {

        PaymentIntent paymentIntent = stripeService.createStripePaymentIntent(stripePaymentRequestDTO);
        return new ResponseEntity<>(paymentIntent.getClientSecret(), HttpStatus.CREATED);
    }

    @PostMapping("/users/orders/payment-confirmation")
    public ResponseEntity<PaymentConfirmationResponseDTO> confirmPayment(@RequestBody PaymentConfirmationRequestDTO paymentConfirmationRequestDTO) {

        PaymentConfirmationResponseDTO paymentConfirmationResponseDTO = stripeService.confirmPayment(paymentConfirmationRequestDTO);
        return new ResponseEntity<>(paymentConfirmationResponseDTO, HttpStatus.OK);
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<List<OrderDto>> getAllOrders() {

        List<OrderDto> orders = orderService.getAllOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/users/orders")
    public ResponseEntity<List<OrderDto>> getCurrentUserOrders() {

        List<OrderDto> orders = orderService.getCurrentUserOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/users/orders/{orderNumber}")
    public ResponseEntity<OrderDto> getCurrentUserOrder(@PathVariable String orderNumber) {

        OrderDto order = orderService.getCurrentUserOrder(orderNumber);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @GetMapping("/admin/orders/{orderNumber}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable String orderNumber) {

        OrderDto order = orderService.getOrder(orderNumber);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}
