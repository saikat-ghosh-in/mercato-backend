package com.ecommerce_backend.Service;

import com.ecommerce_backend.Configuration.AppConstants;
import com.ecommerce_backend.Entity.Category;
import com.ecommerce_backend.Entity.EcommUser;
import com.ecommerce_backend.Entity.Product;
import com.ecommerce_backend.Entity.SupplyType;
import com.ecommerce_backend.ExceptionHandler.ForbiddenOperationException;
import com.ecommerce_backend.ExceptionHandler.ResourceAlreadyExistsException;
import com.ecommerce_backend.ExceptionHandler.ResourceNotFoundException;
import com.ecommerce_backend.Payloads.Request.ProductRequestDTO;
import com.ecommerce_backend.Payloads.Request.ProductSupplyUpdateRequestDTO;
import com.ecommerce_backend.Payloads.Response.ProductResponseDTO;
import com.ecommerce_backend.Payloads.Response.ProductResponse;
import com.ecommerce_backend.Payloads.Response.ProductSupplyUpdateResponseDTO;
import com.ecommerce_backend.Repository.CategoryRepository;
import com.ecommerce_backend.Repository.ProductRepository;
import com.ecommerce_backend.Utils.AuthUtil;
import com.ecommerce_backend.Utils.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Value("${images.base.url}")
    private String imageBaseUrl;

    @Value("${images.products.folder}")
    private String productsImageFolder;

    @Value("${images.products.placeholder.url}")
    private String placeholderImageUrl;

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final FileService fileService;
    private final AuthUtil authUtil;


    @Override
    @Transactional
    public ProductResponseDTO addProduct(String categoryId, ProductRequestDTO productRequestDTO) {

        validateIfAlreadyExists(productRequestDTO.getProductName()); // throws
        Category category = categoryService.getCategoryByCategoryId(categoryId); // throws
        EcommUser user = authUtil.getLoggedInUser();
        if (user == null || !user.isSeller() || !user.isAdmin()) {
            throw new ForbiddenOperationException("You are not authorized to perform this action.");
        }

        Product product = new Product();
        product.setProductName(productRequestDTO.getProductName());
        product.setActive(productRequestDTO.isActive());
        product.setImagePath(placeholderImageUrl);
        product.setDescription(productRequestDTO.getDescription());
        product.setQuantity(productRequestDTO.getQuantity());
        product.setRetailPrice(productRequestDTO.getRetailPrice());
        product.setDiscountPercent(productRequestDTO.getDiscountPercent());
        product.setCategory(category);
        product.setSeller(user);

        Product savedProduct = productRepository.save(product);
        return buildProductDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder, String categoryName, String keyword) {

        Sort sort = Sort.unsorted();
        boolean sortBySellingPrice = "sellingPrice".equalsIgnoreCase(sortBy);
        if(isSortByAllowed(sortBy)) {
            if (!sortBySellingPrice) {
                sort = "desc".equalsIgnoreCase(sortingOrder)
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending();
            }
        }


        Specification<Product> spec = Specification.unrestricted();

        /* keyword search: productName OR description (case-insensitive) */
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                String pattern = "%" + keyword.toLowerCase() + "%";

                return cb.or(
                        cb.like(cb.lower(root.get("productName")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                );
            });
        }

        /* add category filter */
        if (categoryName != null && !categoryName.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("categoryName"), categoryName)
            );
        }

        /* if request pageNumber >= totalPages, return last page */
        long totalElements = productRepository.count(spec);
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        if (totalPages == 0) {
            pageNumber = 0;
        } else if (pageNumber >= totalPages) {
            pageNumber = totalPages - 1;
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> productsPage = productRepository.findAll(spec, pageable);
        List<Product> products = productsPage.getContent();

        /* if sortBy=sellingPrice */
        if (sortBySellingPrice) {
            Comparator<Product> comparator = Comparator.comparing(Product::getSellingPrice);
            if ("desc".equalsIgnoreCase(sortingOrder)) {
                comparator = comparator.reversed();
            }

            products = products.stream()
                    .sorted(comparator)
                    .toList();
        }

        List<ProductResponseDTO> productResponseDTOList = products.stream()
                .map(this::buildProductDto)
                .toList();

        return ProductResponse.builder()
                .content(productResponseDTOList)
                .pageNumber(productsPage.getNumber())
                .pageSize(productsPage.getSize())
                .totalElements(productsPage.getTotalElements())
                .totalPages(productsPage.getTotalPages())
                .lastPage(productsPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProduct(String productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null!");
        }
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        return buildProductDto(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(String productId, String categoryId, ProductRequestDTO productRequestDTO) {
        EcommUser user = authUtil.getLoggedInUser();
        Product product = getProductByIdForUpdate(productId); // throws

        if (!user.isAdmin() && !user.getUserId().equals(product.getSeller().getUserId())) {
            throw new ForbiddenOperationException("You are not authorized to perform this action.");
        }

        if (productRequestDTO.getProductName() != null && !productRequestDTO.getProductName().equals(product.getProductName())) {
            validateIfAlreadyExists(productRequestDTO.getProductName()); // throws
        }

        if (categoryId != null)
            product.setCategory(categoryService.getCategoryByCategoryId(categoryId)); // throws if category not found

        product.setProductName(productRequestDTO.getProductName());
        product.setActive(productRequestDTO.isActive());
        product.setDescription(productRequestDTO.getDescription());
        product.setQuantity(productRequestDTO.getQuantity());
        product.setRetailPrice(productRequestDTO.getRetailPrice());
        product.setDiscountPercent(productRequestDTO.getDiscountPercent());

        Product savedProduct = productRepository.save(product);
        return buildProductDto(savedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(String productId) {
        EcommUser user = authUtil.getLoggedInUser();
        Product product = getProductByIdForUpdate(productId);

        if (!user.isAdmin() && !user.getUserId().equals(product.getSeller().getUserId())) {
            throw new ForbiddenOperationException("You are not authorized to perform this action.");
        }
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO uploadProductImage(String productId, MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty");
        }
        long maxSize = 100 * 1024; // 100 KB
        if (image.getSize() > maxSize) {
            throw new IllegalArgumentException("Image size must not exceed 100 KB");
        }

        EcommUser user = authUtil.getLoggedInUser();
        Product product = getProductByIdForUpdate(productId);

        if (!user.isAdmin() && !user.getUserId().equals(product.getSeller().getUserId())) {
            throw new ForbiddenOperationException("You are not authorized to perform this action.");
        }

        String imageFilePath = fileService.uploadProductImage(productsImageFolder, image, productId);
        product.setImagePath(imageFilePath);

        Product savedProduct = productRepository.save(product);
        return buildProductDto(savedProduct);
    }

    @Override
    public Product getProductByIdForUpdate(String productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null!");
        }
        return productRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
    }

    @Override
    @Transactional
    public List<ProductSupplyUpdateResponseDTO> updateProductInventory(List<ProductSupplyUpdateRequestDTO> productSupplyUpdateRequestDTOs) {
        List<ProductSupplyUpdateResponseDTO> productSupplyUpdateResponseDTOs = new ArrayList<>();

        productSupplyUpdateRequestDTOs.forEach(productSupplyUpdateRequestDTO -> {
            ProductSupplyUpdateResponseDTO productSupplyUpdateResponseDTO = ProductSupplyUpdateResponseDTO.builder()
                    .productId(productSupplyUpdateRequestDTO.getProductId())
                    .supplyType(productSupplyUpdateRequestDTO.getSupplyType())
                    .quantity(productSupplyUpdateRequestDTO.getQuantity())
                    .error(false)
                    .build();
            try {
                Integer quantity = productSupplyUpdateRequestDTO.getQuantity();
                Product product = getProductByIdForUpdate(productSupplyUpdateRequestDTO.getProductId());

                if (SupplyType.ABSOLUTE.equals(productSupplyUpdateRequestDTO.getSupplyType())) {
                    product.setInventoryAbsolute(quantity);
                } else {
                    product.adjustInventory(quantity);
                }
            } catch (Exception ex) {
                productSupplyUpdateResponseDTO.setError(true);
                productSupplyUpdateResponseDTO.setErrorMessage(ex.getMessage());
            } finally {
                productSupplyUpdateResponseDTOs.add(productSupplyUpdateResponseDTO);
            }
        });
        return productSupplyUpdateResponseDTOs;
    }

    @Override
    @Transactional
    public String addDummyProducts() {
        EcommUser user = authUtil.getLoggedInUser();
        if (!user.isAdmin()) {
            throw new ForbiddenOperationException("You are not authorized to perform this action.");
        }

        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw new RuntimeException("No categories found");
        }

        List<Product> products = new ArrayList<>();

        for (int i = 1; i <= 30; i++) {
            Category category = categories.get(i % categories.size());

            Product p = new Product();
            p.setProductName("Dummy Product " + i);
            p.setActive(true);
            p.setDescription("Dummy description for product " + i);
            p.setImagePath(placeholderImageUrl);
            p.setQuantity(10 + i);
            p.setRetailPrice(new BigDecimal(500 + (i * 25)));
            p.setDiscountPercent(new BigDecimal(i % 20)); // 0–19%
            p.setCategory(category);
            p.setSeller(user);

            products.add(p);
        }

        productRepository.saveAll(products);
        return "success";
    }


    private ProductResponseDTO buildProductDto(Product product) {
        return new ProductResponseDTO(
                product.getProductId(),
                product.getProductName(),
                product.isActive(),
                product.getCategory().getCategoryId(),
                product.getCategory().getCategoryName(),
                constructImageUrl(product.getImagePath()),
                product.getDescription(),
                product.getQuantity(),
                product.getRetailPrice(),
                product.getDiscountPercent(),
                product.getSellingPrice(),
                product.getSeller().getSellerDisplayName(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private String constructImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty() || imagePath.equals(placeholderImageUrl)) {
            return placeholderImageUrl;
        }
        return imageBaseUrl.endsWith("/") ? imageBaseUrl + imagePath : imageBaseUrl + "/" + imagePath;
    }

    private void validateIfAlreadyExists(String productName) {
        if (productRepository.existsByProductName(productName)) {
            throw new ResourceAlreadyExistsException("Product", "productName", productName);
        }
    }

    private boolean isSortByAllowed(String sortBy) {
        return AppConstants.ALLOWED_SORT_PRODUCT_FIELDS.contains(sortBy);
    }
}
