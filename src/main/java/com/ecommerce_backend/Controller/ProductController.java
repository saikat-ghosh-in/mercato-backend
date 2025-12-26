package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Configuration.AppConstants;
import com.ecommerce_backend.Payloads.ProductDto;
import com.ecommerce_backend.Payloads.ProductResponse;
import com.ecommerce_backend.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping("/add")
    public ResponseEntity<ProductDto> addProduct(@RequestBody ProductDto productDto) {
        ProductDto savedProductDto = productService.addProduct(productDto);
        return new ResponseEntity<>(savedProductDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ProductResponse> getAllProducts(@RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                          @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                          @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                          @RequestParam(defaultValue = AppConstants.SORTING_ORDER) String sortingOrder) {
        ProductResponse productResponse = productService.getProducts(pageNumber, pageSize, sortBy, sortingOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable String productId) {
        ProductDto productDto = productService.getProduct(productId);
        return new ResponseEntity<>(productDto, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<ProductDto> updateProduct(@RequestBody ProductDto productDto) {
        ProductDto updatedProductDto = productService.updateProduct(productDto);
        return new ResponseEntity<>(updatedProductDto, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return new ResponseEntity<>("Product deleted successfully", HttpStatus.OK);
    }

    @PutMapping("/uploadImage/{productId}")
    public ResponseEntity<ProductDto> uploadProductImage(@PathVariable String productId,
                                                         @RequestParam("image") MultipartFile image) throws IOException {
        ProductDto productDto = productService.uploadProductImage(productId, image);
        return new ResponseEntity<>(productDto, HttpStatus.OK);
    }

    @GetMapping("/searchByCategoryId/{categoryId}")
    public ResponseEntity<ProductResponse> getProductsByCategory(@PathVariable String categoryId,
                                                                 @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                                 @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                 @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                                 @RequestParam(defaultValue = AppConstants.SORTING_ORDER) String sortingOrder) {
        ProductResponse productResponse = productService.getProductsByCategory(categoryId, pageNumber, pageSize, sortBy, sortingOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/searchByKeyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductsByKeyword(@PathVariable String keyword,
                                                                @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                                @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                                @RequestParam(defaultValue = AppConstants.SORTING_ORDER) String sortingOrder) {
        ProductResponse productResponse = productService.getProductsByKeyword(keyword, pageNumber, pageSize, sortBy, sortingOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }
}
