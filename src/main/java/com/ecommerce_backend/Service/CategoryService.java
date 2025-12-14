package com.ecommerce_backend.Service;

import com.ecommerce_backend.Payloads.CategoryDto;
import com.ecommerce_backend.Payloads.CategoryResponse;

public interface CategoryService {

    CategoryDto createCategory(CategoryDto categoryDto);

    CategoryDto getCategoryById(String categoryId);

    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder);

    CategoryDto updateCategory(CategoryDto newCategoryDto);

    String deleteCategory(String categoryId);
}
