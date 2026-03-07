package com.mercato.Payloads.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartItemRequestDTO {
    private final String productId;
    private final Integer quantity;
}