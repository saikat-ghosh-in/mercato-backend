package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class CategoryResponseDTO {
    private final String categoryId;
    private final String categoryName;
    private final Instant createdAt;
    private final Instant updatedAt;
}
