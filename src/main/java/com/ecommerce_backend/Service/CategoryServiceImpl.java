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
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private CategoryRepository categoryRepository;


    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        String categoryId = categoryDto.getCategoryId();
        String name = categoryDto.getName();
        if (categoryRepository.existsById(categoryId)) {
            throw new ResourceAlreadyExistsException("Category", "categoryId", categoryId);
        } else if (categoryRepository.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Category", "name", name);
        }

        Category category = modelMapper.map(categoryDto, Category.class);
        categoryRepository.save(category);

        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalStateException("Creation failed for: " + categoryId));

        return modelMapper.map(savedCategory, CategoryDto.class);
    }

    @Override
    public CategoryDto getCategoryById(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        return modelMapper.map(category, CategoryDto.class);
    }

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize) {
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
        List<Category> categories = categoryPage.getContent();

        List<CategoryDto> categoryDtoList = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDto.class))
                .toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDtoList);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    @Override
    public CategoryDto updateCategory(CategoryDto newCategoryDto) {
        String name = newCategoryDto.getName();
        if (categoryRepository.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Category", "name", name);
        }
        getCategoryById(newCategoryDto.getCategoryId()); // throws ResourceNotFoundException

        Category newCategory = modelMapper.map(newCategoryDto, Category.class);
        Category savedCategory = categoryRepository.save(newCategory);

        return modelMapper.map(savedCategory, CategoryDto.class);
    }

    @Override
    public String deleteCategory(String categoryId) {
        CategoryDto existingCategoryDto = getCategoryById(categoryId); // throws ResourceNotFoundException
        Category existingCategory = modelMapper.map(existingCategoryDto, Category.class);
        categoryRepository.delete(existingCategory);

        if (categoryRepository.existsById(categoryId)) {
            throw new IllegalStateException("Deletion failed for: " + categoryId);
        }
        return "Category deleted successfully";
    }
}
