package com.ecommerce_backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "ecomm_carts")
@Getter
@Setter
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
            initialValue = 70000001,
            allocationSize = 10
    )
    private Long cartId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private EcommUser user;

    @OneToMany(
            mappedBy = "cart",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true
    )
    private List<CartItem> cartItems = new ArrayList<>();


    @Transient
    public BigDecimal getSubtotal() {
        return cartItems.stream()
                .map(cartItem ->
                        Optional.ofNullable(cartItem.getLineTotal())
                                .orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transient
    public void addProduct(Product product, int quantity) {
        validateQuantity(quantity);
        findItemByProductId(product.getProductId())
                .ifPresentOrElse(
                        cartItem -> cartItem.increaseQuantity(quantity),
                        () -> addCartItem(
                                CartItem.builder()
                                        .cart(this)
                                        .product(product)
                                        .quantity(quantity)
                                        .build()
                        )
                );
    }

    @Transient
    public void updateProductQuantity(Long productId, int quantity) {
        CartItem cartItem = findItemByProductId(productId)
                .orElseThrow(() ->
                        new IllegalStateException("Product not in cart"));
        if (quantity <= 0) {
            removeCartItem(cartItem);
            return;
        }
        cartItem.updateQuantity(quantity);
    }

    @Transient
    public void removeProduct(Long productId) {
        findItemByProductId(productId)
                .ifPresent(this::removeCartItem);
    }

    public void addCartItem(CartItem cartItem) {
        if (cartItem == null) return;
        cartItems.add(cartItem);
        cartItem.setCart(this);
    }

    public void removeCartItem(CartItem cartItem) {
        if (cartItem == null) return;
        cartItems.remove(cartItem);
        cartItem.setCart(null);
    }

    @Transient
    public Optional<CartItem> findItemByProductId(Long productId) {
        return cartItems.stream()
                .filter(cartItem -> cartItem.getProduct()
                        .getProductId()
                        .equals(productId))
                .findFirst();
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }
}