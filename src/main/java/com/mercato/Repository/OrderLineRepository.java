package com.mercato.Repository;

import com.mercato.Entity.fulfillment.OrderLine;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ol FROM OrderLine ol
            LEFT JOIN FETCH ol.order
            LEFT JOIN FETCH ol.stateTransitions
            WHERE ol.fulfillmentId = :fulfillmentId
            AND ol.orderLineNumber = :orderLineNumber
            """)
    Optional<OrderLine> findByFulfillmentIdAndOrderLineNumberForUpdate(
            @Param("fulfillmentId") String fulfillmentId,
            @Param("orderLineNumber") int orderLineNumber
    );

    @Query("""
            SELECT ol FROM OrderLine ol
            LEFT JOIN FETCH ol.stateTransitions
            LEFT JOIN FETCH ol.order
            WHERE ol.sellerEmail = :sellerEmail
            """)
    List<OrderLine> findAllBySellerEmail(@Param("sellerEmail") String sellerEmail);

    @Query("""
            SELECT ol FROM OrderLine ol
            LEFT JOIN FETCH ol.stateTransitions
            LEFT JOIN FETCH ol.order
            WHERE ol.fulfillmentId = :fulfillmentId
            AND ol.sellerEmail = :sellerEmail
            """)
    List<OrderLine> findAllByFulfillmentIdAndSellerEmail(
            @Param("fulfillmentId") String fulfillmentId,
            @Param("sellerEmail") String sellerEmail
    );
}