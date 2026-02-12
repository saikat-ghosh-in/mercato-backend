package com.ecommerce_backend.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class CategoryDto {

    private Long categoryId;
    private String categoryName;
    private String updateUser;
    private Instant updateDate;
}
