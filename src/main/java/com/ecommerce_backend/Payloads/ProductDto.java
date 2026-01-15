package com.ecommerce_backend.Payloads;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class ProductDto {

    private final Long productId;
    private final String productName;
    private final Long categoryId;
    private final String imagePath;
    private final String description;
    private final Integer quantity;
    private final BigDecimal retailPrice;
    private final BigDecimal discountPercent;
    private final BigDecimal sellingPrice;
    private final Instant updateDate;
}
