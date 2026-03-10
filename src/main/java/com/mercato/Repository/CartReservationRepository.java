package com.mercato.Repository;

import com.mercato.Entity.cart.CartReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CartReservationRepository extends JpaRepository<CartReservation, Long> {

    Optional<CartReservation> findByCartItem_CartItemId(Long cartItemId);

    @Query("""
            SELECT r FROM CartReservation r
            LEFT JOIN FETCH r.product
            LEFT JOIN FETCH r.cartItem
            WHERE r.expiresAt < :now
            """)
    List<CartReservation> findAllExpired(Instant now);

    @Query("""
            SELECT r FROM CartReservation r
            LEFT JOIN FETCH r.product
            WHERE r.cartItem.cartItemId IN :cartItemIds
            """)
    List<CartReservation> findAllByCartItemIds(List<Long> cartItemIds);

    @Query("""
            SELECT cr FROM CartReservation cr
            LEFT JOIN FETCH cr.cartItem
            WHERE cr.product.productId = :productId
            ORDER BY cr.createdAt DESC
            """)
    List<CartReservation> findAllByProductIdOrderByCreatedAtDesc(@Param("productId") String productId);
}
