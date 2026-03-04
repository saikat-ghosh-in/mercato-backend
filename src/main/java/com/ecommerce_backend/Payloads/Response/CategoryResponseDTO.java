package com.ecommerce_backend.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class CategoryResponseDTO {
    private String categoryId;
    private String categoryName;
    private Instant createdAt;
    private Instant updatedAt;
}
