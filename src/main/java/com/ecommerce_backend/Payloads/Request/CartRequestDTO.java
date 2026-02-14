package com.ecommerce_backend.Payloads.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CartRequestDTO {
    private final List<CartItemRequestDTO> cartItems;
}