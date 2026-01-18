package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Payloads.OrderDto;
import com.ecommerce_backend.Payloads.OrderRequestDto;
import com.ecommerce_backend.Service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/users/orders/capture")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderRequestDto orderRequestDto) {

        OrderDto order = orderService.placeOrder(orderRequestDto);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}
