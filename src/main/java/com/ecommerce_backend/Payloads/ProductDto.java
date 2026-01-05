package com.ecommerce_backend.Payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long productId;
    private String productName;
    private Long categoryId;
    private String imagePath;
    private String description;
    private Integer quantity;
    private double retailPrice;
    private double discount;
    private double specialPrice;
    private Instant updateDate;
}
