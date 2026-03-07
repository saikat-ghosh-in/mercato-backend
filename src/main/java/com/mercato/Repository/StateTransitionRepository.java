package com.mercato.Repository;

import com.mercato.Entity.fulfillment.StateTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StateTransitionRepository extends JpaRepository<StateTransition, Long> {

    List<StateTransition> findByOrderLine_IdOrderByOccurredAtDesc(Long orderLineId);

    @Query("""
            SELECT st FROM StateTransition st
            LEFT JOIN FETCH st.orderLine ol
            WHERE ol.order.orderId = :orderId
            ORDER BY st.occurredAt DESC
            """)
    List<StateTransition> findAllByOrderId(String orderId);

    @Query("""
            SELECT st FROM StateTransition st
            WHERE st.orderLine.id = :orderLineId
            ORDER BY st.occurredAt DESC
            LIMIT 1
            """)
    java.util.Optional<StateTransition> findLatestByOrderLineId(Long orderLineId);
}
