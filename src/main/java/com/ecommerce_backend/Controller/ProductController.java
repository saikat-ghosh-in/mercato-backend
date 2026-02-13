package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Configuration.AppConstants;
import com.ecommerce_backend.Payloads.Request.ProductRequestDTO;
import com.ecommerce_backend.Payloads.Request.ProductSupplyUpdateRequestDTO;
import com.ecommerce_backend.Payloads.Response.ProductResponseDTO;
import com.ecommerce_backend.Payloads.Response.ProductResponse;
import com.ecommerce_backend.Payloads.Response.ProductSupplyUpdateResponseDTO;
import com.ecommerce_backend.Service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/users/categories/{categoryId}/product")
    public ResponseEntity<ProductResponseDTO> addProduct(@RequestBody ProductRequestDTO productRequestDTO,
                                                         @PathVariable Long categoryId) {
        ProductResponseDTO savedProductResponseDTO = productService.addProduct(categoryId, productRequestDTO);
        return new ResponseEntity<>(savedProductResponseDTO, HttpStatus.CREATED);
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
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable Long productId) {
        ProductResponseDTO productResponseDTO = productService.getProduct(productId);
        return new ResponseEntity<>(productResponseDTO, HttpStatus.OK);
    }

    @PutMapping("/users/products/{productId}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long productId,
                                                            @RequestParam(required = false) Long categoryId,
                                                            @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO updatedProductResponseDTO = productService.updateProduct(productId, categoryId, productRequestDTO);
        return new ResponseEntity<>(updatedProductResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/users/products/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return new ResponseEntity<>("Product deleted successfully", HttpStatus.OK);
    }

    @PutMapping("/users/products/{productId}/uploadImage")
    public ResponseEntity<ProductResponseDTO> uploadProductImage(@PathVariable Long productId,
                                                                 @RequestParam("image") MultipartFile image) throws IOException {
        ProductResponseDTO productResponseDTO = productService.uploadProductImage(productId, image);
        return new ResponseEntity<>(productResponseDTO, HttpStatus.OK);
    }

    @PostMapping("/users/products/supplyUpdate")
    public ResponseEntity<List<ProductSupplyUpdateResponseDTO>> productSupplyUpdate(@RequestBody List<ProductSupplyUpdateRequestDTO> productSupplyUpdateRequestDTOs) {
        List<ProductSupplyUpdateResponseDTO> productSupplyUpdateResponseDTOs = productService.updateProductInventory(productSupplyUpdateRequestDTOs);
        return new ResponseEntity<>(productSupplyUpdateResponseDTOs, HttpStatus.OK);
    }

    @GetMapping("/addDummyProducts")
    public ResponseEntity<String> addDummyProducts() {
        return new ResponseEntity<>(productService.addDummyProducts(), HttpStatus.CREATED);
    }
}
