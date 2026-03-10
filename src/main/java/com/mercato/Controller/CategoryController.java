package com.mercato.Controller;

import com.mercato.Configuration.AppConstants;
import com.mercato.Payloads.Request.CategoryRequestDTO;
import com.mercato.Payloads.Response.CategoryResponseDTO;
import com.mercato.Service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/admin/categories/create")
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody CategoryRequestDTO categoryRequestDTO) {
        CategoryResponseDTO savedCategoryResponseDTO = categoryService.createCategory(categoryRequestDTO);
        return new ResponseEntity<>(savedCategoryResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/public/categories")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(
            @RequestParam(defaultValue = AppConstants.SORT_CATEGORIES_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORTING_ORDER) String sortingOrder
    ) {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories(sortBy, sortingOrder);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @GetMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable String categoryId) {
        CategoryResponseDTO categoryResponseDTO = categoryService.getCategory(categoryId);
        return new ResponseEntity<>(categoryResponseDTO, HttpStatus.OK);
    }

    @PutMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable String categoryId,
                                                              @RequestBody CategoryRequestDTO categoryRequestDTO) {
        CategoryResponseDTO updatedCategoryResponseDTO = categoryService.updateCategory(categoryId, categoryRequestDTO);
        return new ResponseEntity<>(updatedCategoryResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable String categoryId) {
        categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>("Category deleted successfully", HttpStatus.OK);
    }

    @GetMapping("/admin/addDummyCategories")
    public ResponseEntity<String> addDummyCategories() {
        return new ResponseEntity<>(categoryService.addDummyCategories(), HttpStatus.CREATED);
    }
}
