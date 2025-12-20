package com.ecommerce_backend.Service;

import com.ecommerce_backend.Payloads.ProductDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    ProductDto addProduct(ProductDto productDto);

    List<ProductDto> getProducts();

    ProductDto getProduct(String productId);

    ProductDto updateProduct(ProductDto productDto);

    void deleteProduct(String productId);

    ProductDto uploadProductImage(String productId, MultipartFile image) throws IOException;
}
