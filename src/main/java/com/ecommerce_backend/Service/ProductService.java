package com.ecommerce_backend.Service;

import com.ecommerce_backend.Payloads.ProductDto;

import java.util.List;

public interface ProductService {

    ProductDto addProduct(ProductDto productDto);

    List<ProductDto> getProducts();

    ProductDto getProduct(String productId);

    ProductDto updateProduct(ProductDto productDto);

    void deleteProduct(String productId);
}
