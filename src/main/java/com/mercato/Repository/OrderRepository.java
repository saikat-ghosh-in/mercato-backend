package com.mercato.Repository;

import com.mercato.Entity.fulfillment.Order;
import com.mercato.Payloads.Response.OrderSummaryDTO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"orderLines", "orderLines.stateTransitions"})
    Optional<Order> findByOrderId(String orderId);

    @EntityGraph(attributePaths = {"orderLines", "orderLines.stateTransitions"})
    Optional<Order> findByOrderIdAndCustomerEmail(String orderId, String email);

    @EntityGraph(attributePaths = {"orderLines", "orderLines.stateTransitions"})
    List<Order> findByCustomerEmail(String email);

    @Query("""
            SELECT new com.mercato.Payloads.Response.OrderSummaryDTO(
                o.orderId, o.orderStatus, o.totalAmount, o.createdAt
            )
            FROM Order o
            WHERE o.customerEmail = :email
            ORDER BY o.createdAt DESC
            """)
    List<OrderSummaryDTO> findOrderSummariesByCustomerEmail(@Param("email") String email);
}