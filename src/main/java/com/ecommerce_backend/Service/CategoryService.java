package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Category;
import com.ecommerce_backend.Payloads.Request.CategoryRequestDTO;
import com.ecommerce_backend.Payloads.Response.CategoryResponseDTO;
import com.ecommerce_backend.Payloads.Response.CategoryResponse;

public interface CategoryService {

    CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO);

    CategoryResponseDTO getCategory(String categoryId);

    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder);

    CategoryResponseDTO updateCategory(String categoryId, CategoryRequestDTO categoryRequestDTO);

    void deleteCategory(String categoryId);

    Category getCategoryByCategoryId(String categoryId);

    boolean existsByCategoryName(String categoryName);

    boolean existsByCategoryId(String categoryId);

    String addDummyCategories();
}
