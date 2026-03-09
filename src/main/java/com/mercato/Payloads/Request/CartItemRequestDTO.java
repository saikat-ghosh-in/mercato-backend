package com.mercato.Payloads.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartItemRequestDTO {
    private String productId;
    private Integer quantity;
}