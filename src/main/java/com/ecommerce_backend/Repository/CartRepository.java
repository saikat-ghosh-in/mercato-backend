package com.ecommerce_backend.Repository;

import com.ecommerce_backend.Entity.Cart;
import com.ecommerce_backend.Entity.EcommUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser_UserId(Long userId);
}