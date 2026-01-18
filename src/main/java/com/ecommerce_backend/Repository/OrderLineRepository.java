package com.ecommerce_backend.Repository;

import com.ecommerce_backend.Entity.Fulfillment.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

}