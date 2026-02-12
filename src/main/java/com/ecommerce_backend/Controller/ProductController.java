package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Configuration.AppConstants;
import com.ecommerce_backend.Payloads.Response.ProductDto;
import com.ecommerce_backend.Payloads.Response.ProductResponse;
import com.ecommerce_backend.Service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDto> addProduct(@RequestBody ProductDto productDto,
                                                 @PathVariable Long categoryId) {
        ProductDto savedProductDto = productService.addProduct(categoryId, productDto);
        return new ResponseEntity<>(savedProductDto, HttpStatus.CREATED);
    }

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(@RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                          @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                          @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                          @RequestParam(defaultValue = AppConstants.SORTING_ORDER) String sortingOrder,
                                                          @RequestParam(required = false) String category,
                                                          @RequestParam(required = false) String keyword) {
        ProductResponse productResponse = productService.getProducts(pageNumber, pageSize, sortBy, sortingOrder, category, keyword);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/public/products/{productId}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long productId) {
        ProductDto productDto = productService.getProduct(productId);
        return new ResponseEntity<>(productDto, HttpStatus.OK);
    }

    @PutMapping("/admin/products/update")
    public ResponseEntity<ProductDto> updateProduct(@RequestBody ProductDto productDto) {
        ProductDto updatedProductDto = productService.updateProduct(productDto);
        return new ResponseEntity<>(updatedProductDto, HttpStatus.OK);
    }

    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return new ResponseEntity<>("Product deleted successfully", HttpStatus.OK);
    }

    @PutMapping("/admin/products/{productId}/uploadImage")
    public ResponseEntity<ProductDto> uploadProductImage(@PathVariable Long productId,
                                                         @RequestParam("image") MultipartFile image) throws IOException {
        ProductDto productDto = productService.uploadProductImage(productId, image);
        return new ResponseEntity<>(productDto, HttpStatus.OK);
    }

    @PostMapping("/admin/products/{productId}/newQuantity/{newQuantity}")
    public ResponseEntity<ProductDto> getProductsByKeyword(@PathVariable Long productId,
                                                                @PathVariable Integer newQuantity) {
        ProductDto productDto = productService.updateProductInventory(productId, newQuantity);
        return new ResponseEntity<>(productDto, HttpStatus.OK);
    }

    @GetMapping("/addDummyProducts")
    public ResponseEntity<String> addDummyProducts() {
        return new ResponseEntity<>(productService.addDummyProducts(), HttpStatus.CREATED);
    }
}
