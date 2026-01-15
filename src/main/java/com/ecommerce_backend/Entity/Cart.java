package com.ecommerce_backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ecomm_carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "cart_seq"
    )
    @SequenceGenerator(
            name = "cart_seq",
            sequenceName = "cart_seq",
            allocationSize = 10
    )
    private Long cartId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private EcommUser user;

    @OneToMany(
            mappedBy = "cart",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true
    )
    private List<CartItem> cartItems = new ArrayList<>();

    @DecimalMin(value = "0.00", message = "Discount must be at least 0%")
    @DecimalMax(value = "99.99", message = "Discount cannot exceed 99.99%")
    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Transient
    public BigDecimal getSubtotal() {
        return cartItems.stream()
                .map(CartItem::getItemPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getDiscountAmount() {
        BigDecimal discountRate = discountPercent.movePointLeft(2); // 10 → 0.10

        return getSubtotal()
                .multiply(discountRate)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transient
    public BigDecimal getTotalPayable() {
        return getSubtotal()
                .subtract(getDiscountAmount())
                .max(BigDecimal.ZERO);
    }
}

