package com.ecommerce_backend.Payloads;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartItemDto {

    private final Long cartItemId;
    private final Long cartId;
    private final Long productId;
    private final String productName;
    private final String productImage;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal lineTotal;
}