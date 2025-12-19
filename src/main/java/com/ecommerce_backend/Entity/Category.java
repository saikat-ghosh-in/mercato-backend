package com.ecommerce_backend.Entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Entity
@Table(name = "ecomm_category_snapshot")
public class Category {

    @Id
    private String categoryId;
    private String name;
    private String updateUser;
    @Column(
            name = "update_date",
            insertable = false,
            updatable = false
    )
    private Instant updateDate;

    @OneToMany(mappedBy = "category")
    private List<Product> products;
}
