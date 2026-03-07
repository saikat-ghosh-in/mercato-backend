package com.mercato.Service;

import com.mercato.Entity.Category;
import com.mercato.Payloads.Request.CategoryRequestDTO;
import com.mercato.Payloads.Response.CategoryResponseDTO;
import com.mercato.Payloads.Response.CategoryResponse;

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
