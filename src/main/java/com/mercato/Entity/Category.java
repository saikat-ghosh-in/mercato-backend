package com.mercato.Entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "ecomm_categories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_category_id", columnNames = "category_id")
        }
)
public class Category {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "category_seq"
    )
    @SequenceGenerator(
            name = "category_seq",
            sequenceName = "category_seq",
            initialValue = 40000001,
            allocationSize = 10
    )
    private Long id;

    @Column(name = "category_id", nullable = false, updatable = false, length = 30)
    private String categoryId;

    @NotBlank
    @Size(min = 4, message = "Category name must be more than 3 characters")
    private String categoryName;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    private void prePersist() {
        if (this.categoryId == null) {
            String datePart = LocalDate.now().toString().replace("-", "");
            String randomPart = UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 6).toUpperCase();
            this.categoryId = "CAT-" + datePart + "-" + randomPart;
        }
    }
}
