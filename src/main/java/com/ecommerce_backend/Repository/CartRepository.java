package com.ecommerce_backend.Repository;

import com.ecommerce_backend.Entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCartId(String cartId);

    Optional<Cart> findByUser_Id(Long userId);

    @Query("""
                SELECT c FROM Cart c
                LEFT JOIN FETCH c.cartItems ci
                LEFT JOIN FETCH ci.product
                WHERE c.user.userId = :userId
            """)
    Optional<Cart> findCartWithItemsByUserId(String userId);
}