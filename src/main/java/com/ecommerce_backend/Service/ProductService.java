package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Product;
import com.ecommerce_backend.Payloads.ProductDto;

import java.util.List;

public interface ProductService {

    List<ProductDto> getProducts();
    ProductDto addProduct(ProductDto productDto);
}
