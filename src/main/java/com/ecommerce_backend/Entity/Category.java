package com.ecommerce_backend.Entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Data
@Entity
@Table(name = "ecomm_categories")
public class Category {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "category_seq"
    )
    @SequenceGenerator(
            name = "category_seq",
            sequenceName = "category_seq",
            allocationSize = 10
    )
    private Long categoryId;

    @NotBlank
    @Size(min = 4, message = "Category name must be more than 3 characters")
    private String categoryName;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;

    private String updateUser;

    @UpdateTimestamp
    @Column(name = "update_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant updateDate;
}
