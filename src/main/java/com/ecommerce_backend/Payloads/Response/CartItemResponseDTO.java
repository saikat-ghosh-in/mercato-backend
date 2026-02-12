package com.ecommerce_backend.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartItemResponseDTO {

    private final Long cartItemId;
    private final Long cartId;
    private final Long productId;
    private final String productName;
    private final String productImage;
    private final Integer quantity;
    private final BigDecimal itemPrice;
    private final BigDecimal lineTotal;
}