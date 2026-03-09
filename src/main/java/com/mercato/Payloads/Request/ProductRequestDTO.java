package com.mercato.Payloads.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProductRequestDTO {
    private String productName;
    private boolean active;
    private String imageUrl;
    private String description;
    private Integer quantity;
    private BigDecimal retailPrice;
    private BigDecimal discountPercent;
}