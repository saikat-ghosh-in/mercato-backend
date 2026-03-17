package com.mercato.Service;

import com.mercato.Configuration.AppConstants;
import com.mercato.Entity.Category;
import com.mercato.ExceptionHandler.CustomBadRequestException;
import com.mercato.ExceptionHandler.ResourceAlreadyExistsException;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Mapper.CategoryMapper;
import com.mercato.Payloads.Request.CategoryRequestDTO;
import com.mercato.Payloads.Response.CategoryResponseDTO;
import com.mercato.Repository.CategoryRepository;
import com.mercato.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;


    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) {

        String categoryName = categoryRequestDTO.getCategoryName();
        validateCategory(categoryName); // throws

        Category category = new Category();
        category.setCategoryName(categoryName);

        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories(String sortBy, String sortingOrder) {
        validateSortBy(sortBy);
        Sort sort = "desc".equalsIgnoreCase(sortingOrder)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return categoryRepository.findAll(sort)
                .stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategory(String categoryId) {
        Category category = getCategoryByCategoryId(categoryId);
        return CategoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(String categoryId, CategoryRequestDTO categoryRequestDTO) {
        String categoryName = categoryRequestDTO.getCategoryName();

        Category category = getCategoryByCategoryId(categoryId);
        if (!categoryName.equals(category.getCategoryName())) {
            validateCategory(categoryName);
        }

        category.setCategoryName(categoryName);
        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(String categoryId) {
        Category category = getCategoryByCategoryId(categoryId);
        if (productRepository.existsByCategoryCategoryId(categoryId)) {
            throw new CustomBadRequestException(
                    "Cannot delete category with existing products"
            );
        }
        categoryRepository.delete(category);
    }


    private Category getCategoryByCategoryId(String categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("categoryId must not be null");
        }
        return categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
    }

    private void validateCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("categoryName must not be null or blank");
        }
        if (categoryRepository.existsByCategoryName(categoryName)) {
            throw new ResourceAlreadyExistsException("Category", "categoryName", categoryName);
        }
    }

    private void validateSortBy(String sortBy) {
        if (!AppConstants.ALLOWED_SORT_CATEGORY_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sortBy value: " + sortBy);
        }
    }
}
