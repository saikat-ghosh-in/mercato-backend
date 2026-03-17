package com.mercato.Service;

import com.mercato.Payloads.Request.CategoryRequestDTO;
import com.mercato.Payloads.Response.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO);

    List<CategoryResponseDTO> getAllCategories(String sortBy, String sortingOrder);

    CategoryResponseDTO getCategory(String categoryId);

    CategoryResponseDTO updateCategory(String categoryId, CategoryRequestDTO categoryRequestDTO);

    void deleteCategory(String categoryId);
}
