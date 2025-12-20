package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.*;
import com.ecommerce_backend.ExceptionHandler.*;
import com.ecommerce_backend.Payloads.CategoryDto;
import com.ecommerce_backend.Payloads.CategoryResponse;
import com.ecommerce_backend.Repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private CategoryRepository categoryRepository;


    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        validateFields(categoryDto); // throws
        validateIfAlreadyExists(categoryDto); // throws

        Category category = new Category();
        category.setCategoryId(categoryDto.getCategoryId());
        category.setName(categoryDto.getName());
        category.setUpdateUser(categoryDto.getUpdateUser());

        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder) {
        Sort sort = sortingOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);

        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
        List<Category> categories = categoryPage.getContent();
        List<CategoryDto> categoryDtoList = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDto.class))
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
    public CategoryDto getCategory(String categoryId) {
        Category category = getCategoryById(categoryId);
        return modelMapper.map(category, CategoryDto.class);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(CategoryDto categoryDto) {
        validateFields(categoryDto);

        String name = categoryDto.getName();
        Category categoryByName = categoryRepository.findByName(name);
        if (categoryByName != null && !categoryByName.getCategoryId().equals(categoryDto.getCategoryId())) {
            throw new ResourceAlreadyExistsException("Category", "name", name);
        }

        Category category = getCategoryById(categoryDto.getCategoryId()); // throws
        category.setName(name);
        category.setUpdateUser(categoryDto.getUpdateUser());

        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDto.class);
    }

    @Override
    @Transactional
    public void deleteCategory(String categoryId) {
        Category category = getCategoryById(categoryId); // throws
        categoryRepository.delete(category);
    }

    @Override
    public Category getCategoryById(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            throw new IllegalArgumentException("categoryId must not be null or blank");
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
    }

    private void validateFields(CategoryDto categoryDto) {
        String categoryId = categoryDto.getCategoryId();
        String name = categoryDto.getName();
        if (categoryId == null || categoryId.isBlank()) {
            throw new IllegalArgumentException("categoryId must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
    }

    private void validateIfAlreadyExists(CategoryDto categoryDto) {
        String categoryId = categoryDto.getCategoryId();
        String name = categoryDto.getName();
        if (categoryRepository.existsById(categoryId)) {
            throw new ResourceAlreadyExistsException("Category", "categoryId", categoryId);
        } else if (categoryRepository.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Category", "name", name);
        }
    }
}
