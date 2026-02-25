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
    public boolean existsByCategoryName(String categoryName) {
        return categoryRepository.existsByCategoryName(categoryName);
    }

    @Override
    @Transactional
    public String addDummyCategories() {
        Map<String, String> categoryMap = new HashMap<>();

        categoryMap.put("Mens T-Shirts", "admin");
        categoryMap.put("Smartphones", "admin");
        categoryMap.put("Apparel", "admin");
        categoryMap.put("Home Appliances", "admin");
        categoryMap.put("Toys", "admin");
        categoryMap.put("Furniture", "admin");
        categoryMap.put("Books", "admin");
        categoryMap.put("Sports Equipment", "admin");
        categoryMap.put("Beauty Products", "admin");
        categoryMap.put("Automotive", "admin");
        categoryMap.put("Outdoor Gear", "admin");
        categoryMap.put("Electronics", "admin");
        categoryMap.put("Kitchen Appliances", "admin");
        categoryMap.put("Baby Products", "admin");
        categoryMap.put("Health & Fitness", "admin");
        categoryMap.put("Garden & Outdoor", "admin");
        categoryMap.put("Pet Supplies", "admin");
        categoryMap.put("Office Supplies", "admin");
        categoryMap.put("Jewelry & Watches", "admin");
        categoryMap.put("Travel & Luggage", "admin");
        categoryMap.put("Musical Instruments", "admin");
        categoryMap.put("Crafts & Hobbies", "admin");
        categoryMap.put("Collectibles & Memorabilia", "admin");
        categoryMap.put("Art & Decor", "admin");
        categoryMap.put("Food & Beverages", "admin");
        categoryMap.put("Stationery & Gift Wrapping", "admin");
        categoryMap.put("Electrical & Lighting", "admin");
        categoryMap.put("DIY & Tools", "admin");
        categoryMap.put("Party Supplies", "admin");
        categoryMap.put("Educational Toys", "admin");

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
