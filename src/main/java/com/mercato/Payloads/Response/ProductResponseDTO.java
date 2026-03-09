package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class ProductResponseDTO {
    private final String productId;
    private final String productName;
    private final boolean active;
    private final String categoryId;
    private final String categoryName;
    private final String imageUrl;
    private final String description;
    private final int physicalQty;
    private final int reservedQty;
    private final int availableQty;
    private final BigDecimal retailPrice;
    private final BigDecimal discountPercent;
    private final BigDecimal sellingPrice;
    private final String seller;
    private final Instant createdAt;
    private final Instant updatedAt;
}
