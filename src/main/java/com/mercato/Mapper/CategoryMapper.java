package com.mercato.Mapper;

import com.mercato.Entity.Category;
import com.mercato.Payloads.Response.CategoryResponseDTO;

public class CategoryMapper {

    public static CategoryResponseDTO toDto(Category category) {
        return new CategoryResponseDTO(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
