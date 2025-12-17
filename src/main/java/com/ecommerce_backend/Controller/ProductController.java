package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Entity.Product;
import com.ecommerce_backend.Payloads.CategoryDto;
import com.ecommerce_backend.Payloads.ProductDto;
import com.ecommerce_backend.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        List<ProductDto> products = productService.getProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<ProductDto> addProduct(@RequestBody ProductDto productDto) {
        ProductDto savedProductDto = productService.addProduct(productDto);
        return new ResponseEntity<>(savedProductDto, HttpStatus.CREATED);
    }
}
