package com.mercato.Repository;

import com.mercato.Entity.fulfillment.OrderLine;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
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
    List<OrderLine> findAllBySeller(@Param("sellerEmail") String sellerEmail);

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
    
    @Query("""
            SELECT COALESCE(SUM(ol.unitPrice * ol.shippedQty), 0)
            FROM OrderLine ol
            WHERE ol.sellerEmail = :sellerEmail
            AND ol.shippedQty > 0
            """)
    BigDecimal findTotalRevenueBySeller(@Param("sellerEmail") String sellerEmail);

    @Query("""
            SELECT COALESCE(SUM(ol.unitPrice * ol.shippedQty), 0)
            FROM OrderLine ol
            WHERE ol.sellerEmail = :sellerEmail
            AND ol.shippedQty > 0
            AND ol.updatedAt >= :start AND ol.updatedAt < :end
            """)
    BigDecimal findRevenueBySellerBetween(@Param("sellerEmail") String sellerEmail,
                                               @Param("start") Instant start,
                                               @Param("end") Instant end);
    
    @Query("""
            SELECT COUNT(DISTINCT ol.order.id)
            FROM OrderLine ol
            WHERE ol.sellerEmail = :sellerEmail
            """)
    long countDistinctOrdersBySeller(@Param("sellerEmail") String sellerEmail);

    @Query("""
            SELECT COUNT(ol)
            FROM OrderLine ol
            WHERE ol.sellerEmail = :sellerEmail
            """)
    long countOrderLinesBySeller(@Param("sellerEmail") String sellerEmail);

    @Query("""
            SELECT COALESCE(AVG(orderTotal), 0)
            FROM (
                SELECT SUM(ol.unitPrice * ol.shippedQty) AS orderTotal
                FROM OrderLine ol
                WHERE ol.sellerEmail = :sellerEmail
                AND ol.shippedQty > 0
                GROUP BY ol.order.id
            )
            """)
    BigDecimal findAverageOrderValueBySeller(@Param("sellerEmail") String sellerEmail);
    
    @Query("""
            SELECT ol.productId, ol.productName,
                   SUM(ol.shippedQty) AS totalShippedQty,
                   SUM(ol.unitPrice * ol.shippedQty) AS totalRevenue
            FROM OrderLine ol
            WHERE ol.sellerEmail = :sellerEmail
            AND ol.shippedQty > 0
            GROUP BY ol.productId, ol.productName
            ORDER BY totalShippedQty DESC
            """)
    List<Object[]> findTopSellingProductsBySeller(@Param("sellerEmail") String sellerEmail);
    
    @Query("""
            SELECT ol.orderLineStatus, COUNT(ol)
            FROM OrderLine ol
            WHERE ol.sellerEmail = :sellerEmail
            GROUP BY ol.orderLineStatus
            """)
    List<Object[]> findOrderLineStatusBreakdownBySeller(@Param("sellerEmail") String sellerEmail);
}