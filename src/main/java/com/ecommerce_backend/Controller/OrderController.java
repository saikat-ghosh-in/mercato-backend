package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Payloads.OrderDto;
import com.ecommerce_backend.Payloads.OrderRequestDto;
import com.ecommerce_backend.Service.OrderService;
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

    @PostMapping("/users/orders/capture")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderRequestDto orderRequestDto) {

        OrderDto order = orderService.placeOrder(orderRequestDto);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
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
