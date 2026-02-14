package com.ecommerce_backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Entity
@Table(name = "ecomm_cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "cart_item_seq"
    )
    @SequenceGenerator(
            name = "cart_item_seq",
            sequenceName = "cart_item_seq",
            initialValue = 60000001,
            allocationSize = 10
    )
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Min(1)
    private Integer quantity;


    @Transient
    public BigDecimal getItemPrice() {
        return product.getSellingPrice();
    }

    @Transient
    public BigDecimal getLineTotal() {
        if (quantity == null) {
            return BigDecimal.ZERO;
        }
        return getItemPrice()
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void increaseQuantity(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        product.adjustInventory(-qty);
        this.quantity += qty;
    }

    public void updateQuantity(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        product.adjustInventory(this.quantity - qty);
        this.quantity = qty;
    }

    public void decreaseQuantity(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (this.quantity < qty) {
            throw new IllegalStateException("Cannot reduce quantity below zero");
        }
        product.adjustInventory(qty);
        this.quantity -= qty;
    }
}
