package com.mercato.Mapper;

import com.mercato.Entity.Product;
import com.mercato.Payloads.Response.ProductResponseDTO;
import org.springframework.beans.factory.annotation.Value;

public class ProductMapper {

    @Value("${images.base.url}")
    private static String imageBaseUrl;

    @Value("${images.products.placeholder.url}")
    private static String placeholderImageUrl;

    public static ProductResponseDTO toDto(Product product) {
        return new ProductResponseDTO(
                product.getProductId(),
                product.getProductName(),
                product.isActive(),
                product.getCategory().getCategoryId(),
                product.getCategory().getCategoryName(),
                constructImageUrl(product.getImagePath()),
                product.getDescription(),
                product.getPhysicalQty(),
                product.getReservedQty(),
                product.getAvailableQty(),
                product.getRetailPrice(),
                product.getDiscountPercent(),
                product.getSellingPrice(),
                product.getSeller().getSellerDisplayName(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private static String constructImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty() || imagePath.equals(placeholderImageUrl))
            return placeholderImageUrl;
        return imageBaseUrl.endsWith("/")
                ? imageBaseUrl + imagePath
                : imageBaseUrl + "/" + imagePath;
    }
}
