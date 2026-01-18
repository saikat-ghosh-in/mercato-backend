package com.ecommerce_backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "ecomm_cart_items")
@Data
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
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
}
