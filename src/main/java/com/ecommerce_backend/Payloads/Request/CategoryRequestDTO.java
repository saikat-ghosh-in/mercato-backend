package com.ecommerce_backend.Payloads.Request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryRequestDTO {
    private String categoryName;
}
