package com.ecommerce_backend.Payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private String productId;
    private String gtin;
    private String name;
    private String imagePath;
    private String categoryId;
    private Double unitPrice;
    private Double markDown;
    private String updateUser;
    private Instant updateDate;
}
