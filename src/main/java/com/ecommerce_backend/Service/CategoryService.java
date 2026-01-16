package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Category;
import com.ecommerce_backend.Payloads.CategoryDto;
import com.ecommerce_backend.Payloads.CategoryResponse;

public interface CategoryService {

    CategoryDto createCategory(CategoryDto categoryDto);

    CategoryDto getCategory(Long categoryId);

    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder);

    CategoryDto updateCategory(CategoryDto newCategoryDto);

    void deleteCategory(Long categoryId);

    Category getCategoryById(Long categoryId);

    String addDummyCategories();
}
