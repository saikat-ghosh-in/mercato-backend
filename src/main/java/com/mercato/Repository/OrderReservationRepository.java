package com.mercato.Repository;


import com.mercato.Entity.fulfillment.OrderReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderReservationRepository extends JpaRepository<OrderReservation, Long> {

    Optional<OrderReservation> findByOrderLine_Id(Long orderLineId);

    List<OrderReservation> findAllByOrder_OrderId(String orderId);

    @Modifying
    @Query("""
            DELETE FROM OrderReservation r
            WHERE r.order.orderId = :orderId
            """)
    void deleteAllByOrderId(String orderId);
}
