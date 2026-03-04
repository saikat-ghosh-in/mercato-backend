package com.ecommerce_backend.Entity;

import com.ecommerce_backend.ExceptionHandler.InsufficientInventoryException;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "ecomm_products",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_product_id", columnNames = "product_id")
        },
        indexes = {
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_seller",   columnList = "seller_fk"),
                @Index(name = "idx_product_active",   columnList = "active"),
        }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(name = "product_seq", sequenceName = "product_seq", initialValue = 30000001, allocationSize = 10)
    private Long id;

    @Column(name = "product_id", nullable = false, updatable = false, length = 30)
    private String productId;

    @Version
    @Column(nullable = false)
    private Long version;

    @NotBlank
    @Size(min = 4, message = "Product name must be at least 4 characters")
    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String imagePath;

    @NotBlank
    @Size(min = 7, message = "Product description must be at least 7 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @NotNull(message = "Retail price is required")
    @DecimalMin(value = "0.01", message = "Retail price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal retailPrice;

    @DecimalMin(value = "0.00", message = "Discount must be at least 0%")
    @DecimalMax(value = "99.99", message = "Discount cannot exceed 99.99%")
    @Digits(integer = 2, fraction = 2, message = "Invalid discount format")
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @NotNull(message = "Category is required")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_fk", referencedColumnName = "id")
    private EcommUser seller;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;


    @PrePersist
    private void prePersist() {
        if (this.productId == null) {
            String datePart = LocalDate.now().toString().replace("-", "");
            String randomPart = UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 6).toUpperCase();
            this.productId = "PDT-" + datePart + "-" + randomPart;
        }
    }

    @Transient
    public BigDecimal getSellingPrice() {
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return retailPrice.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal discountRate = discountPercent.movePointLeft(2);
        return retailPrice
                .multiply(BigDecimal.ONE.subtract(discountRate))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void adjustInventory(int delta) {
        int newQuantity = this.quantity + delta;
        if (newQuantity < 0) {
            throw new InsufficientInventoryException(this.productName, this.quantity);
        }
        this.quantity = newQuantity;
    }

    public void setInventoryAbsolute(int newQuantity) {
        if (newQuantity < 0)
            throw new IllegalArgumentException("Inventory cannot be negative");
        this.quantity = newQuantity;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}