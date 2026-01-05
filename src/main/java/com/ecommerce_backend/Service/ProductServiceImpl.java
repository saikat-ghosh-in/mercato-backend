package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Category;
import com.ecommerce_backend.Entity.Product;
import com.ecommerce_backend.ExceptionHandler.ResourceAlreadyExistsException;
import com.ecommerce_backend.ExceptionHandler.ResourceNotFoundException;
import com.ecommerce_backend.Payloads.ProductDto;
import com.ecommerce_backend.Payloads.ProductResponse;
import com.ecommerce_backend.Repository.ProductRepository;
import com.ecommerce_backend.Utils.FileService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Value("${images.products.folder}")
    private String productsImageFolder;
    @Value("${images.products.placeholder}")
    private String placeholderImageName;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private FileService fileService;


    @Override
    @Transactional
    public ProductDto addProduct(Long categoryId, ProductDto productDto) {

        validateIfAlreadyExists(productDto); // throws
        Category category = categoryService.getCategoryById(categoryId); // throws

        Product product = new Product();
        product.setProductName(productDto.getProductName());
        product.setImagePath(Paths.get(productsImageFolder, placeholderImageName).toString());
        product.setDescription(productDto.getDescription());
        product.setQuantity(productDto.getQuantity());
        product.setRetailPrice(productDto.getRetailPrice());
        product.setDiscount(productDto.getDiscount());
        product.setSpecialPrice(getDiscountedPrice(productDto.getRetailPrice(), productDto.getDiscount()));
        product.setCategory(category);

        Product saved = productRepository.save(product);
        return modelMapper.map(saved, ProductDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder) {
        Sort sort = sortingOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> productsPage = productRepository.findAll(pageDetails);
        List<Product> products = productsPage.getContent();
        List<ProductDto> productDtoList = products.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .toList();

        return ProductResponse.builder()
                .content(productDtoList)
                .pageNumber(productsPage.getNumber())
                .pageSize(productsPage.getSize())
                .totalElements(productsPage.getTotalElements())
                .totalPages(productsPage.getTotalPages())
                .lastPage(productsPage.isLast())
                .build();
    }

    @Override
    public ProductDto getProduct(Long productId) {
        Product product = getProductById(productId);
        return modelMapper.map(product, ProductDto.class);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {

        Product product = getProductById(productDto.getProductId()); // throws
        Category category = categoryService.getCategoryById(productDto.getCategoryId()); // throws

        product.setProductName(productDto.getProductName());
        product.setDescription(productDto.getDescription());
        product.setQuantity(productDto.getQuantity());
        product.setRetailPrice(productDto.getRetailPrice());
        product.setDiscount(productDto.getDiscount());
        product.setSpecialPrice(getDiscountedPrice(productDto.getRetailPrice(), productDto.getDiscount()));
        product.setCategory(category);

        Product saved = productRepository.save(product);
        return modelMapper.map(saved, ProductDto.class);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long productId) {
        Product product = getProductById(productId);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductDto uploadProductImage(Long productId, MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty");
        }
        long maxSize = 100 * 1024; // 100 KB
        if (image.getSize() > maxSize) {
            throw new IllegalArgumentException("Image size must not exceed 100 KB");
        }

        Product product = getProductById(productId);

        String imageFilePath = fileService.uploadProductImage(productsImageFolder, image, productId);
        product.setImagePath(imageFilePath);

        Product saved = productRepository.save(product);
        return modelMapper.map(saved, ProductDto.class);
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder) {

        Category category = categoryService.getCategoryById(categoryId);

        Sort sort = sortingOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> productsPage = productRepository.findByCategory(pageDetails, category);
        List<Product> products = productsPage.getContent();
        List<ProductDto> productDtoList = products.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .toList();

        return ProductResponse.builder()
                .content(productDtoList)
                .pageNumber(productsPage.getNumber())
                .pageSize(productsPage.getSize())
                .totalElements(productsPage.getTotalElements())
                .totalPages(productsPage.getTotalPages())
                .lastPage(productsPage.isLast())
                .build();
    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder) {
        Sort sort = sortingOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> productsPage = productRepository.findByKeyword(keyword, pageDetails);
        List<Product> products = productsPage.getContent();
        List<ProductDto> productDtoList = products.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .toList();

        return ProductResponse.builder()
                .content(productDtoList)
                .pageNumber(productsPage.getNumber())
                .pageSize(productsPage.getSize())
                .totalElements(productsPage.getTotalElements())
                .totalPages(productsPage.getTotalPages())
                .lastPage(productsPage.isLast())
                .build();
    }

    private double getDiscountedPrice(double retailPrice, double discount) {
        return retailPrice - ((discount * 0.01) * retailPrice);
    }

    private Product getProductById(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null!");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
    }

    private void validateIfAlreadyExists(ProductDto productDto) {
        Long productId = productDto.getProductId();
        if (productRepository.existsById(productId)) {
            throw new ResourceAlreadyExistsException("Product", "productId", productId);
        }
    }
}
