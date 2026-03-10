package com.mercato.Service;

import com.mercato.Configuration.AppConstants;
import com.mercato.Entity.Category;
import com.mercato.Entity.EcommUser;
import com.mercato.Entity.Product;
import com.mercato.Entity.SupplyType;
import com.mercato.Entity.cart.CartReservation;
import com.mercato.Entity.fulfillment.OrderReservation;
import com.mercato.ExceptionHandler.ForbiddenOperationException;
import com.mercato.ExceptionHandler.ResourceAlreadyExistsException;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Mapper.ProductMapper;
import com.mercato.Payloads.Request.ProductFilterRequestDTO;
import com.mercato.Payloads.Request.ProductRequestDTO;
import com.mercato.Payloads.Request.ProductSupplyUpdateRequestDTO;
import com.mercato.Payloads.Request.SellerProductFilterRequestDTO;
import com.mercato.Payloads.Response.ProductResponseDTO;
import com.mercato.Payloads.Response.ProductResponse;
import com.mercato.Payloads.Response.ProductSupplyUpdateResponseDTO;
import com.mercato.Repository.CartReservationRepository;
import com.mercato.Repository.CategoryRepository;
import com.mercato.Repository.OrderReservationRepository;
import com.mercato.Repository.ProductRepository;
import com.mercato.Utils.AuthUtil;
import com.mercato.Utils.FileService;
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
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Value("${images.products.folder}")
    private String productsImageFolder;

    @Value("${images.products.placeholder.url}")
    private String placeholderImageUrl;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileService fileService;
    private final AuthUtil authUtil;
    private final ProductMapper productMapper;
    private final OrderReservationRepository orderReservationRepository;
    private final CartReservationRepository cartReservationRepository;

    @Override
    @Transactional
    public ProductResponseDTO addProduct(String categoryId, ProductRequestDTO productRequestDTO) {

        Category category = getCategoryByCategoryId(categoryId);
        EcommUser seller = authUtil.getLoggedInUser();
        validateIfAlreadyExists(productRequestDTO.getProductName(), seller.getUserId());

        Product product = new Product();
        product.setProductName(productRequestDTO.getProductName());
        product.setActive(productRequestDTO.isActive());
        product.setImagePath(placeholderImageUrl);
        product.setDescription(productRequestDTO.getDescription());
        product.setRetailPrice(productRequestDTO.getRetailPrice());
        product.setDiscountPercent(productRequestDTO.getDiscountPercent());
        product.setCategory(category);
        product.setSeller(seller);

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProducts(ProductFilterRequestDTO filter) {
        Sort sort = buildSort(filter.getSortBy(), filter.getSortingOrder());
        Specification<Product> spec = buildSpec(
                null, filter.getCategory(), filter.getSeller(),
                filter.getKeyword(), filter.getMinPrice(), filter.getMaxPrice(), filter.isInStock()
        );

        long totalElements = productRepository.count(spec);
        int totalPages = (int) Math.ceil((double) totalElements / filter.getPageSize());

        int pageNumber = filter.getPageNumber();
        if (totalPages == 0) {
            pageNumber = 0;
        } else if (pageNumber >= totalPages) {
            pageNumber = totalPages - 1;
        }

        Pageable pageable = PageRequest.of(pageNumber, filter.getPageSize(), sort);
        Page<Product> productsPage = productRepository.findAll(spec, pageable);

        return new ProductResponse(
                productsPage.getContent().stream().map(productMapper::toDto).toList(),
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getSellerProducts(SellerProductFilterRequestDTO filter) {
        EcommUser seller = authUtil.getLoggedInUser();
        Sort sort = buildSort(filter.getSortBy(), filter.getSortingOrder());
        Specification<Product> spec = buildSpec(
                seller.getUserId(), filter.getCategory(), null,
                filter.getKeyword(), null, null, false
        );

        return productRepository.findAll(spec, sort)
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProduct(String productId) {
        if (productId == null)
            throw new IllegalArgumentException("productId must not be null!");
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        return productMapper.toDto(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(String productId, String categoryId,
                                            ProductRequestDTO productRequestDTO) {
        EcommUser seller = authUtil.getLoggedInUser();
        Product product = getProductByIdForUpdate(productId);
        validateAuthority(product, seller);

        if (productRequestDTO.getProductName() != null
                && !productRequestDTO.getProductName().equals(product.getProductName())) {
            validateIfAlreadyExists(productRequestDTO.getProductName(), seller.getUserId());
        }

        Category category = getCategoryByCategoryId(categoryId);
        product.setCategory(category);

        product.setProductName(productRequestDTO.getProductName());
        product.setActive(productRequestDTO.isActive());
        product.setDescription(productRequestDTO.getDescription());
        product.setRetailPrice(productRequestDTO.getRetailPrice());
        product.setDiscountPercent(productRequestDTO.getDiscountPercent());

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(String productId) {
        EcommUser seller = authUtil.getLoggedInUser();
        Product product = getProductByIdForUpdate(productId);
        validateAuthority(product, seller);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO uploadProductImage(String productId, MultipartFile image) throws IOException {
        if (image == null || image.isEmpty())
            throw new IllegalArgumentException("Image file must not be empty");

        long maxSize = 100 * 1024; // 100 KB
        if (image.getSize() > maxSize)
            throw new IllegalArgumentException("Image size must not exceed 100 KB");

        EcommUser seller = authUtil.getLoggedInUser();
        Product product = getProductByIdForUpdate(productId);
        validateAuthority(product, seller);

        String imageFilePath = fileService.uploadProductImage(productsImageFolder, image, productId);
        product.setImagePath(imageFilePath);

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    public List<ProductSupplyUpdateResponseDTO> updateProductInventory(List<ProductSupplyUpdateRequestDTO> productSupplyUpdateRequestDTOs) {

        List<ProductSupplyUpdateResponseDTO> responses = new ArrayList<>();
        EcommUser seller = authUtil.getLoggedInUser();

        productSupplyUpdateRequestDTOs.forEach(dto -> {
            boolean isError = false;
            String errorMessage = null;
            try {
                Product product = getProductByIdForUpdate(dto.getProductId());
                validateAuthority(product, seller);

                if (SupplyType.ABSOLUTE.equals(dto.getSupplyType())) {
                    forceSetInventory(product, dto.getQuantity());
                } else {
                    product.adjustInventory(dto.getQuantity());
                    productRepository.save(product);
                }
            } catch (Exception ex) {
                isError = true;
                errorMessage = ex.getMessage();
            } finally {
                responses.add(
                        new ProductSupplyUpdateResponseDTO(
                                dto.getProductId(),
                                dto.getSupplyType(),
                                dto.getQuantity(),
                                isError,
                                errorMessage)
                );
            }
        });

        return responses;
    }

    @Override
    @Transactional
    public String addDummyProducts() {
        EcommUser admin = authUtil.getLoggedInUser();
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty())
            throw new RuntimeException("No categories found");

        List<Product> products = new ArrayList<>();

        for (int i = 1; i <= 30; i++) {
            Category category = categories.get(i % categories.size());
            Product p = new Product();
            p.setProductName("Dummy Product " + i);
            p.setActive(true);
            p.setDescription("Dummy description for product " + i);
            p.setImagePath(placeholderImageUrl);
            p.setPhysicalQty(10 + i);
            p.setRetailPrice(new BigDecimal(500 + (i * 25)));
            p.setDiscountPercent(new BigDecimal(i % 20));
            p.setCategory(category);
            p.setSeller(admin);
            products.add(p);
        }

        productRepository.saveAll(products);
        return "success";
    }


    @Transactional
    private void forceSetInventory(Product product, int newPhysicalQty) {
        if (newPhysicalQty < 0)
            throw new IllegalArgumentException("Physical qty cannot be negative");

        int excess = product.getReservedQty() - newPhysicalQty;

        if (excess > 0) {
            excess = releaseCartReservations(product, excess);
        }

        if (excess > 0) {
            releaseOrderReservations(product, excess);
        }

        product.setInventoryAbsolute(newPhysicalQty);
        productRepository.save(product);
    }

    private int releaseCartReservations(Product product, int excess) {
        List<CartReservation> cartReservations = cartReservationRepository
                .findAllByProductIdOrderByCreatedAtDesc(product.getProductId());

        for (CartReservation reservation : cartReservations) {
            if (excess <= 0) break;

            int toRelease = Math.min(reservation.getReservedQty(), excess);
            product.decreaseReservedQty(toRelease);
            excess -= toRelease;

            if (toRelease == reservation.getReservedQty()) {
                cartReservationRepository.delete(reservation);
            } else {
                reservation.updateReservedQuantity(reservation.getReservedQty() - toRelease);
                cartReservationRepository.save(reservation);
            }
        }

        return excess;
    }

    private void releaseOrderReservations(Product product, int excess) {
        List<OrderReservation> orderReservations = orderReservationRepository
                .findAllByProductIdOrderByCreatedAtDesc(product.getProductId());

        for (OrderReservation reservation : orderReservations) {
            if (excess <= 0) break;

            int toRelease = Math.min(reservation.getReservedQty(), excess);
            product.decreaseReservedQty(toRelease);
            excess -= toRelease;

            if (toRelease == reservation.getReservedQty()) {
                orderReservationRepository.delete(reservation);
            } else {
                reservation.setReservedQty(reservation.getReservedQty() - toRelease);
                orderReservationRepository.save(reservation);
            }
        }

    }


    @Transactional
    private Product getProductByIdForUpdate(String productId) {
        if (productId == null)
            throw new IllegalArgumentException("productId must not be null!");
        return productRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
    }

    public Category getCategoryByCategoryId(String categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("categoryId must not be null");
        }
        return categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
    }

    private void validateIfAlreadyExists(String productName, String sellerId) {
        if (productRepository.existsByProductNameAndSeller_UserId(productName, sellerId))
            throw new ResourceAlreadyExistsException("Product", "productName", productName);
    }

    private void validateAuthority(Product product, EcommUser user) {
        if (!user.isAdmin() && !user.getUserId().equals(product.getSeller().getUserId())) {
            throw new ForbiddenOperationException("You are not authorized to perform this action.");
        }
    }

    private Sort buildSort(String sortBy, String sortingOrder) {
        String validSortBy = isSortByAllowed(sortBy)
                ? sortBy
                : AppConstants.SORT_PRODUCTS_BY;
        return "desc".equalsIgnoreCase(sortingOrder)
                ? Sort.by(validSortBy).descending()
                : Sort.by(validSortBy).ascending();
    }

    private Specification<Product> buildSpec(String sellerId, String category, String seller,
                                             String keyword, BigDecimal minPrice,
                                             BigDecimal maxPrice, boolean inStock) {
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (sellerId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("seller").get("userId"), sellerId)
            );
        }

        if (category != null && !category.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("categoryName"), category)
            );
        }

        if (seller != null && !seller.isBlank()) {
            List<String> sellers = Arrays.asList(seller.split(","));
            spec = spec.and((root, query, cb) ->
                    root.get("seller").get("sellerDisplayName").in(sellers)
            );
        }

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                String pattern = "%" + keyword.toLowerCase() + "%";
                return cb.or(
                        cb.like(cb.lower(root.get("productName")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                );
            });
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice)
            );
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice)
            );
        }

        if (inStock) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThan(
                            cb.diff(root.get("physicalQty"), root.get("reservedQty")), 0
                    )
            );
        }

        if (sellerId == null) {
            spec = spec.and((root, query, cb) ->
                    cb.isTrue(root.get("active"))
            );
        }

        return spec;
    }

    private boolean isSortByAllowed(String sortBy) {
        return AppConstants.ALLOWED_SORT_PRODUCT_FIELDS.contains(sortBy);
    }
}
