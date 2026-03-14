package com.mercato.Repository;

import com.mercato.Entity.cart.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"cartItems", "cartItems.product"})
    Optional<Cart> findByUser_UserId(String userId);

    @EntityGraph(attributePaths = {"cartItems", "cartItems.product"})
    Optional<Cart> findByGuestToken(String guestToken);

    Optional<Cart> findByCartId(String cartId);

    @Modifying
    @Query("""
            DELETE FROM Cart c
            WHERE c.guestToken IS NOT NULL
            AND c.updatedAt < :cutoff
            """)
    void deleteStaleGuestCarts(Instant cutoff);
}