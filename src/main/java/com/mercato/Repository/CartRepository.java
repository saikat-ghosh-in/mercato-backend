package com.mercato.Repository;

import com.mercato.Entity.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCartId(String cartId);

    @Query("""
            SELECT c FROM Cart c
            LEFT JOIN FETCH c.cartItems ci
            LEFT JOIN FETCH ci.product
            LEFT JOIN FETCH c.charges
            WHERE c.user.userId = :userId
            """)
    Optional<Cart> findCartWithItemsByUserId(String userId);

    @Query("""
            SELECT c FROM Cart c
            LEFT JOIN FETCH c.cartItems ci
            LEFT JOIN FETCH ci.product
            LEFT JOIN FETCH c.charges
            WHERE c.guestToken = :guestToken
            """)
    Optional<Cart> findCartWithItemsByGuestToken(String guestToken);

    @Modifying
    @Query("""
            DELETE FROM Cart c
            WHERE c.guestToken IS NOT NULL
            AND c.updatedAt < :cutoff
            """)
    void deleteStaleGuestCarts(Instant cutoff);
}