package com.ecommerce_backend.Payloads.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartItemRequestDTO {
    private final Long productId;
    private final Integer quantity;
}