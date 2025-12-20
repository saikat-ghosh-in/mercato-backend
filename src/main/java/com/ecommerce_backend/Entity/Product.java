package com.ecommerce_backend.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "ecomm_product_snapshot")
public class Product {

    @Id
    private String productId;
    private String gtin;
    private String name;
    private String imagePath;
    private Double unitPrice;
    private Double markDown;
    private String updateUser;
    @Column(
            name = "update_date",
            insertable = false,
            updatable = false
    )
    private Instant updateDate;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
