package com.mercato.Service;

import com.mercato.Configuration.AppConstants;
import com.mercato.Entity.Category;
import com.mercato.Entity.EcommUser;
import com.mercato.ExceptionHandler.ForbiddenOperationException;
import com.mercato.ExceptionHandler.ResourceAlreadyExistsException;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Payloads.Request.CategoryRequestDTO;
import com.mercato.Payloads.Response.CategoryResponseDTO;
import com.mercato.Payloads.Response.CategoryResponse;
import com.mercato.Repository.CategoryRepository;
import com.mercato.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuthUtil authUtil;


    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) {
        String categoryName = categoryRequestDTO.getCategoryName();
        validateCategory(categoryName); // throws

        Category category = new Category();
        category.setCategoryName(categoryName);

        Category savedCategory = categoryRepository.save(category);
        return buildCategoryDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder) {

        validateSortBy(sortBy);
        Sort sort = "desc".equalsIgnoreCase(sortingOrder)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);

        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
        List<Category> categories = categoryPage.getContent();
        List<CategoryResponseDTO> categoryResponseDTOList = categories.stream()
                .map(this::buildCategoryDto)
                .toList();

        return CategoryResponse.builder()
                .content(categoryResponseDTOList)
                .pageNumber(categoryPage.getNumber())
                .pageSize(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .lastPage(categoryPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategory(String categoryId) {
        Category category = this.getCategoryByCategoryId(categoryId);
        return buildCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(String categoryId, CategoryRequestDTO categoryRequestDTO) {
        String categoryName = categoryRequestDTO.getCategoryName();
        validateCategory(categoryRequestDTO.getCategoryName()); // throws

        Category category = this.getCategoryByCategoryId(categoryId); // throws
        category.setCategoryName(categoryName);

        Category savedCategory = categoryRepository.save(category);
        return buildCategoryDto(savedCategory);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(String categoryId) {
        Category category = this.getCategoryByCategoryId(categoryId); // throws
        categoryRepository.delete(category);
    }

    @Override
    public Category getCategoryByCategoryId(String categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("categoryId must not be null");
        }
        return categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
    }

    @Override
    public boolean existsByCategoryName(String categoryName) {
        return categoryRepository.existsByCategoryName(categoryName);
    }

    @Override
    public boolean existsByCategoryId(String categoryId) {
        return categoryRepository.existsByCategoryId(categoryId);
    }

    @Override
    @Transactional
    public String addDummyCategories() {
        EcommUser user = authUtil.getLoggedInUser();
        if (!user.isAdmin()) {
            throw new ForbiddenOperationException("You are not authorized to perform this action.");
        }

        List<String> dummyCategories = List.of(
                "Mens T-Shirts",
                "Smartphones",
                "Apparel",
                "Home Appliances",
                "Toys",
                "Furniture",
                "Books",
                "Sports Equipment",
                "Beauty Products",
                "Automotive",
                "Outdoor Gear",
                "Electronics",
                "Kitchen Appliances",
                "Baby Products",
                "Health & Fitness",
                "Garden & Outdoor",
                "Pet Supplies",
                "Office Supplies",
                "Jewelry & Watches",
                "Travel & Luggage",
                "Musical Instruments",
                "Crafts & Hobbies",
                "Collectibles & Memorabilia",
                "Art & Decor",
                "Food & Beverages",
                "Stationery & Gift Wrapping",
                "Electrical & Lighting",
                "DIY & Tools",
                "Party Supplies",
                "Educational Toys"
        );

        dummyCategories.forEach(categoryName -> {
            if (!categoryRepository.existsByCategoryName(categoryName)) {
                Category category = new Category();
                category.setCategoryName(categoryName);
                categoryRepository.save(category);
            }
        });

        return "Categories inserted safely";
    }

    private CategoryResponseDTO buildCategoryDto(Category category) {
        return new CategoryResponseDTO(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
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
