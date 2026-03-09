package com.mercato.Entity.cart;

import com.mercato.Entity.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "cart_reservations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_reservation_cartitem",
                        columnNames = "cart_item_fk"
                )
        },
        indexes = {
                @Index(name = "idx_cart_reservation_expires_at", columnList = "expires_at"),
                @Index(name = "idx_cart_reservation_product",    columnList = "product_fk")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_reservation_seq")
    @SequenceGenerator(name = "cart_reservation_seq", sequenceName = "cart_reservation_seq", initialValue = 35000001, allocationSize = 10)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_item_fk", referencedColumnName = "cartItemId", nullable = false)
    private CartItem cartItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_fk", referencedColumnName = "id", nullable = false)
    private Product product;

    @Column(name = "reserved_qty", nullable = false)
    private int reservedQty;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public void extendExpiry(int minutes) {
        this.expiresAt = Instant.now().plusSeconds(minutes * 60L);
        this.updatedAt = Instant.now();
    }

    public void updateReservedQuantity(int newQuantity) {
        this.reservedQty = newQuantity;
        this.updatedAt = Instant.now();
    }
}
