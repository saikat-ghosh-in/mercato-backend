package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.EcommUser;
import com.ecommerce_backend.Entity.Product;
import com.ecommerce_backend.Payloads.Response.ProductDto;
import com.ecommerce_backend.Payloads.Response.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {

    ProductDto addProduct(Long categoryId, ProductDto productDto);

    ProductResponse getProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder, String categoryName, String keyword);

    ProductDto getProduct(Long productId);

    ProductDto updateProduct(ProductDto productDto);

    void deleteProduct(Long productId);

    ProductDto uploadProductImage(Long productId, MultipartFile image) throws IOException;

    Product getProductByIdForUpdate(Long productId);

    ProductDto updateProductInventory(Long productId, Integer newQuantity);

    void sourceProduct(Long productId, Integer requestedQuantity);

    String addDummyProducts();

    EcommUser getSeller(Product product);
}
