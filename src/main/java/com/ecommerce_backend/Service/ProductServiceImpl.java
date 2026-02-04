package com.ecommerce_backend.Service;

import com.ecommerce_backend.Configuration.AppConstants;
import com.ecommerce_backend.Entity.Category;
import com.ecommerce_backend.Entity.EcommUser;
import com.ecommerce_backend.Entity.Product;
import com.ecommerce_backend.ExceptionHandler.ResourceAlreadyExistsException;
import com.ecommerce_backend.ExceptionHandler.ResourceNotFoundException;
import com.ecommerce_backend.Payloads.ProductDto;
import com.ecommerce_backend.Payloads.ProductResponse;
import com.ecommerce_backend.Repository.CategoryRepository;
import com.ecommerce_backend.Repository.ProductRepository;
import com.ecommerce_backend.Security.services.UserDetailsServiceImpl;
import com.ecommerce_backend.Utils.FileService;
import lombok.RequiredArgsConstructor;
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
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Value("${images.base.url}")
    private String imageBaseUrl;

    @Value("${images.products.folder}")
    private String productsImageFolder;

    @Value("${images.products.placeholder}")
    private String placeholderImageName;

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final FileService fileService;


    @Override
    @Transactional
    public ProductDto addProduct(Long categoryId, ProductDto productDto) {

        validateIfAlreadyExists(productDto); // throws
        Category category = categoryService.getCategoryById(categoryId); // throws

        Product product = new Product();
        product.setProductName(productDto.getProductName());
        product.setActive(productDto.isActive());
        product.setImagePath(AppConstants.PRODUCT_IMAGE_PATH_PREFIX + placeholderImageName);
        product.setDescription(productDto.getDescription());
        product.setQuantity(productDto.getQuantity());
        product.setRetailPrice(productDto.getRetailPrice());
        product.setDiscountPercent(productDto.getDiscountPercent());
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return getProductDto(savedProduct);
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
                .map(this::getProductDto)
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
        return getProductDto(product);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {

        Product product = getProductById(productDto.getProductId()); // throws
        Category category = categoryService.getCategoryById(productDto.getCategoryId()); // throws

        product.setProductName(productDto.getProductName());
        product.setActive(productDto.isActive());
        product.setDescription(productDto.getDescription());
        product.setQuantity(productDto.getQuantity());
        product.setRetailPrice(productDto.getRetailPrice());
        product.setDiscountPercent(productDto.getDiscountPercent());
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return getProductDto(savedProduct);
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

        Product savedProduct = productRepository.save(product);
        return getProductDto(savedProduct);
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
                .map(this::getProductDto)
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
                .map(this::getProductDto)
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
    public Product getProductById(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null!");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
    }

    @Override
    @Transactional
    public ProductDto updateProductInventory(Long productId, Integer newQuantity) {

        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("Quantity must be a non-negative Integer.");
        }

        Product product = getProductById(productId);
        product.setQuantity(newQuantity);
        Product savedProduct = productRepository.save(product);
        return getProductDto(savedProduct);
    }

    @Override
    @Transactional
    public String addDummyProducts() {

        EcommUser seller = userDetailsService.getEcommUserByUsername("seller1"); // throws

        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw new RuntimeException("No categories found");
        }

        List<Product> products = new ArrayList<>();

        for (int i = 1; i <= 40; i++) {
            Category category = categories.get(i % categories.size());

            Product p = new Product();
            p.setProductName("Dummy Product " + i);
            p.setActive(true);
            p.setDescription("This is a dummy description for product " + i);
            p.setImagePath(AppConstants.PRODUCT_IMAGE_PATH_PREFIX + placeholderImageName);
            p.setQuantity(10 + i);
            p.setRetailPrice(new BigDecimal(500 + (i * 25)));
            p.setDiscountPercent(new BigDecimal(i % 20)); // 0–19%
            p.setCategory(category);
            p.setUser(seller);

            products.add(p);
        }

        productRepository.saveAll(products);
        return "success";
    }

    @Override
    public EcommUser getSeller(Product product) {
        return userDetailsService.getEcommUserByUsername("seller1");
    }

    private ProductDto getProductDto(Product product) {
        return new ProductDto(
                product.getProductId(),
                product.getProductName(),
                product.isActive(),
                product.getCategory().getCategoryId(),
                constructImageUrl(product.getImagePath()),
                product.getDescription(),
                product.getQuantity(),
                product.getRetailPrice(),
                product.getDiscountPercent(),
                product.getSellingPrice(),
                product.getUpdateDate()
        );
    }

    private String constructImageUrl(String imagePath) {
        return imageBaseUrl.endsWith("/") ? imageBaseUrl + imagePath : imageBaseUrl + "/" + imagePath;
    }

    private void validateIfAlreadyExists(ProductDto productDto) {
        String productName = productDto.getProductName();
        if (productRepository.existsByProductName(productName)) {
            throw new ResourceAlreadyExistsException("Product", "productName", productName);
        }
    }
}
