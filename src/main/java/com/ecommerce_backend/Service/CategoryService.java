package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Category;
import com.ecommerce_backend.Payloads.Response.CategoryDto;
import com.ecommerce_backend.Payloads.Response.CategoryResponse;

public interface CategoryService {

    CategoryDto createCategory(CategoryDto categoryDto);

    CategoryDto getCategory(Long categoryId);

    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder);

    CategoryDto updateCategory(CategoryDto newCategoryDto);

    void deleteCategory(Long categoryId);

    Category getCategoryById(Long categoryId);

    boolean existsByCategoryName(String categoryName);

    String addDummyCategories();
}
