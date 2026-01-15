package com.ecommerce_backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Data
@Entity
@Table(name = "ecomm_products")
public class Product {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "product_seq"
    )
    @SequenceGenerator(
            name = "product_seq",
            sequenceName = "product_seq",
            allocationSize = 10
    )
    private Long productId;


    @NotBlank
    @Size(min = 4, message = "Product name must be more than 3 characters")
    @Column(nullable = false)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String imagePath;

    @NotBlank
    @Size(min = 7, message = "Product description must be more than 6 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(nullable = false)
    private Integer quantity = 0;

    @NotNull(message = "Retail price is required")
    @DecimalMin(value = "0.00", message = "Retail price cannot be negative")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal retailPrice;

    @DecimalMin(value = "0.00", message = "Discount must be at least 0%")
    @DecimalMax(value = "99.99", message = "Discount cannot exceed 99.99%")
    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @NotNull(message = "Category is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private EcommUser user;

    @UpdateTimestamp
    @Column(name = "update_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant updateDate;

    @Transient
    public BigDecimal getSellingPrice() {
        BigDecimal discountRate = discountPercent.movePointLeft(2);
        return retailPrice
                .multiply(BigDecimal.ONE.subtract(discountRate))
                .setScale(2, RoundingMode.HALF_UP);
    }
}