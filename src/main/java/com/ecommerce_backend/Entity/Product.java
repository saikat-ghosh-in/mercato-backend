package com.ecommerce_backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
@Entity
@Table(name = "ecomm_product_snapshot")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;

    @NotBlank
    @Size(min = 4, message = "Product name must be more than 3 characters")
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String imagePath;

    @NotBlank
    @Size(min = 7, message = "Product description must be more than 6 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity = 0;

    @PositiveOrZero(message = "Retail price cannot be negative")
    private double retailPrice;

    @DecimalMin(value = "0.0", message = "Discount must be at least 0%")
    @DecimalMax(value = "99.0", message = "Discount cannot exceed 99%")
    private double discount = 0.0;

    @PositiveOrZero(message = "Special price cannot be negative")
    private double specialPrice;

    @NotNull(message = "Category is required")
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

//    @NotNull
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private EcommUser user;

    @UpdateTimestamp
    @Column(name = "update_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant updateDate;
}