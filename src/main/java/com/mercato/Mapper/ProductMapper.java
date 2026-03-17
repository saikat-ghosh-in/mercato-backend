package com.mercato.Mapper;

import com.mercato.Entity.Product;
import com.mercato.Payloads.Response.ProductResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    @Value("${images.base.url}")
    private String imageBaseUrl;

    @Value("${images.products.placeholder.url}")
    private String placeholderImageUrl;

    public ProductResponseDTO toDto(Product product) {
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

    private String constructImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty())
            return placeholderImageUrl;
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://"))
            return imagePath;
        return imageBaseUrl.endsWith("/")
                ? imageBaseUrl + imagePath
                : imageBaseUrl + "/" + imagePath;
    }
}
