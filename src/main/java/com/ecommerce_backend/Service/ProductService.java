package com.ecommerce_backend.Service;

import com.ecommerce_backend.Payloads.ProductDto;
import com.ecommerce_backend.Payloads.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {

    ProductDto addProduct(ProductDto productDto);

    ProductResponse getProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder);

    ProductDto getProduct(String productId);

    ProductDto updateProduct(ProductDto productDto);

    void deleteProduct(String productId);

    ProductDto uploadProductImage(String productId, MultipartFile image) throws IOException;

    ProductResponse getProductsByCategory(String categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder);

    ProductResponse getProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder);
}
