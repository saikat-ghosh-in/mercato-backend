package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Product;
import com.ecommerce_backend.Payloads.ProductDto;
import com.ecommerce_backend.Payloads.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {

    ProductDto addProduct(Long categoryId, ProductDto productDto);

    ProductResponse getProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder);

    ProductDto getProduct(Long productId);

    ProductDto updateProduct(ProductDto productDto);

    void deleteProduct(Long productId);

    ProductDto uploadProductImage(Long productId, MultipartFile image) throws IOException;

    ProductResponse getProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder);

    ProductResponse getProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder);

    Product getProductById(Long productId);

    void updateProductInventory(Long productId, Integer newQuantity);

    String addDummyProducts();
}
