package com.ecommerce_backend.Payloads.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductRequestDTO {
    private final String productName;
    private final boolean active;
    private final String imageUrl;
    private final String description;
    private final Integer quantity;
    private final BigDecimal retailPrice;
    private final BigDecimal discountPercent;
}