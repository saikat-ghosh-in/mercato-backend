package com.mercato.Repository;

import com.mercato.Entity.fulfillment.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.orderLines ol
            LEFT JOIN FETCH ol.stateTransitions
            WHERE o.orderId = :orderId
            """)
    Optional<Order> findByOrderIdWithLines(String orderId);

    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.orderLines ol
            LEFT JOIN FETCH ol.stateTransitions
            WHERE o.customerEmail = :email
            """)
    List<Order> findAllByCustomerEmailWithLines(String email);

    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.orderLines ol
            LEFT JOIN FETCH ol.stateTransitions
            WHERE o.orderId = :orderId
            AND o.customerEmail = :email
            """)
    Optional<Order> findByOrderIdAndCustomerEmailWithLines(@Param("orderId") String orderId,
                                                           @Param("email") String email
    );
}