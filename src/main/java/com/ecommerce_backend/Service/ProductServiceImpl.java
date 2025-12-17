package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Category;
import com.ecommerce_backend.Entity.Product;
import com.ecommerce_backend.ExceptionHandler.ResourceAlreadyExistsException;
import com.ecommerce_backend.Payloads.CategoryDto;
import com.ecommerce_backend.Payloads.ProductDto;
import com.ecommerce_backend.Repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ModelMapper modelMapper;


    @Override
    public List<ProductDto> getProducts() {

        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .toList();
    }

    @Override
    public ProductDto addProduct(ProductDto productDto) {
        String productId = productDto.getProductId();
        String gtin = productDto.getGtin();
        if (productRepository.existsById(productId)) {
            throw new ResourceAlreadyExistsException("Product", "productId", productId);
        }
        if (productRepository.existsByGtin(gtin)) {
            throw new ResourceAlreadyExistsException("Product", "gtin", gtin);
        }

        Product product = modelMapper.map(productDto, Product.class);
        productRepository.save(product);

        Product savedProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("Creation failed for: " + productId));

        return modelMapper.map(savedProduct, ProductDto.class);
    }
}
