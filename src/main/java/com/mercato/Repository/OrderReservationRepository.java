package com.mercato.Repository;


import com.mercato.Entity.fulfillment.OrderReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderReservationRepository extends JpaRepository<OrderReservation, Long> {

    Optional<OrderReservation> findByOrderLine_Id(Long orderLineId);

    @Query("""
            SELECT or FROM OrderReservation or
            LEFT JOIN FETCH or.orderLine
            WHERE or.product.productId = :productId
            ORDER BY or.createdAt DESC
            """)
    List<OrderReservation> findAllByProductIdOrderByCreatedAtDesc(@Param("productId") String productId);
}
