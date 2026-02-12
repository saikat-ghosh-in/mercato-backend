package com.ecommerce_backend.Service;

import com.ecommerce_backend.Configuration.AppConstants;
import com.ecommerce_backend.Entity.Category;
import com.ecommerce_backend.ExceptionHandler.ResourceAlreadyExistsException;
import com.ecommerce_backend.ExceptionHandler.ResourceNotFoundException;
import com.ecommerce_backend.Payloads.Response.CategoryDto;
import com.ecommerce_backend.Payloads.Response.CategoryResponse;
import com.ecommerce_backend.Repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;


    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        validateFields(categoryDto); // throws
        validateIfAlreadyExists(categoryDto); // throws

        Category category = new Category();
        category.setCategoryName(categoryDto.getCategoryName());
        category.setUpdateUser(categoryDto.getUpdateUser());

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
        List<CategoryDto> categoryDtoList = categories.stream()
                .map(this::buildCategoryDto)
                .toList();

        return CategoryResponse.builder()
                .content(categoryDtoList)
                .pageNumber(categoryPage.getNumber())
                .pageSize(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .lastPage(categoryPage.isLast())
                .build();
    }

    @Override
    public CategoryDto getCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        return buildCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(CategoryDto categoryDto) {
        validateFields(categoryDto); // throws

        String categoryName = categoryDto.getCategoryName();
        Optional<Category> categoryByName = categoryRepository.findByCategoryName(categoryName);
        if (categoryByName.isPresent() && !categoryByName.get().getCategoryId().equals(categoryDto.getCategoryId())) {
            throw new ResourceAlreadyExistsException("Category", "categoryName", categoryName);
        }

        Category category = getCategoryById(categoryDto.getCategoryId()); // throws
        category.setCategoryName(categoryName);
        category.setUpdateUser(categoryDto.getUpdateUser());

        Category savedCategory = categoryRepository.save(category);
        return buildCategoryDto(savedCategory);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryById(categoryId); // throws
        categoryRepository.delete(category);
    }

    @Override
    public Category getCategoryById(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("categoryId must not be null");
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
    }

    @Override
    public Category getCategoryByName(String categoryName) {
        if (categoryName == null) {
            throw new IllegalArgumentException("categoryName must not be null");
        }
        return categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryName", categoryName));
    }

    @Override
    public boolean existsByCategoryName(String categoryName) {
        return categoryRepository.existsByCategoryName(categoryName);
    }

    @Override
    @Transactional
    public String addDummyCategories() {
        Map<String, String> categoryMap = new HashMap<>();

        categoryMap.put("Mens T-Shirts", "saikat");
        categoryMap.put("Smartphones", "saikat");
        categoryMap.put("Apparel", "saikat");
        categoryMap.put("Home Appliances", "saikat");
        categoryMap.put("Toys", "saikat");
        categoryMap.put("Furniture", "saikat");
        categoryMap.put("Books", "saikat");
        categoryMap.put("Sports Equipment", "saikat");
        categoryMap.put("Beauty Products", "saikat");
        categoryMap.put("Automotive", "saikat");
        categoryMap.put("Outdoor Gear", "saikat");
        categoryMap.put("Electronics", "saikat");
        categoryMap.put("Kitchen Appliances", "saikat");
        categoryMap.put("Baby Products", "saikat");
        categoryMap.put("Health & Fitness", "saikat");
        categoryMap.put("Garden & Outdoor", "saikat");
        categoryMap.put("Pet Supplies", "saikat");
        categoryMap.put("Office Supplies", "saikat");
        categoryMap.put("Jewelry & Watches", "saikat");
        categoryMap.put("Travel & Luggage", "saikat");
        categoryMap.put("Musical Instruments", "saikat");
        categoryMap.put("Crafts & Hobbies", "saikat");
        categoryMap.put("Collectibles & Memorabilia", "saikat");
        categoryMap.put("Art & Decor", "saikat");
        categoryMap.put("Food & Beverages", "saikat");
        categoryMap.put("Stationery & Gift Wrapping", "saikat");
        categoryMap.put("Electrical & Lighting", "saikat");
        categoryMap.put("DIY & Tools", "saikat");
        categoryMap.put("Party Supplies", "saikat");
        categoryMap.put("Educational Toys", "saikat");

        categoryMap.forEach((categoryName, updateUser) -> {
            Category category = new Category();
            category.setCategoryName(categoryName);
            category.setUpdateUser(updateUser);
            categoryRepository.save(category);
        });

        return "success";
    }

    private CategoryDto buildCategoryDto(Category category) {
        return new CategoryDto(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getUpdateUser(),
                category.getUpdateDate()
        );
    }

    private void validateFields(CategoryDto categoryDto) {
        String categoryName = categoryDto.getCategoryName();
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("categoryName must not be null or blank");
        }
    }

    private void validateIfAlreadyExists(CategoryDto categoryDto) {
        String categoryName = categoryDto.getCategoryName();
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
