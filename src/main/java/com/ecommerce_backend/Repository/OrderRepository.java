package com.ecommerce_backend.Repository;

import com.ecommerce_backend.Entity.Fulfillment.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByCustomerEmail(String customerEmail);

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByOrderNumberAndCustomerEmail(String orderNumber, String email);
}