package com.ecommerce_backend.Repository;

import com.ecommerce_backend.Entity.Fulfillment.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}