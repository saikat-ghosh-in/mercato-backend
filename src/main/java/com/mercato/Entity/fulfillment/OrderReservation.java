package com.mercato.Entity.fulfillment;

import com.mercato.Entity.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "ecomm_order_reservations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_order_reservation_orderline",
                        columnNames = "order_line_fk"
                )
        },
        indexes = {
                @Index(name = "idx_order_reservation_order", columnList = "order_fk"),
                @Index(name = "idx_order_reservation_product", columnList = "product_fk")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_reservation_seq")
    @SequenceGenerator(name = "order_reservation_seq", sequenceName = "order_reservation_seq", initialValue = 85000001, allocationSize = 10)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_fk", referencedColumnName = "id", nullable = false)
    private Order order;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_line_fk", referencedColumnName = "id", nullable = false)
    private OrderLine orderLine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_fk", referencedColumnName = "id", nullable = false)
    private Product product;

    @Column(name = "reserved_qty", nullable = false)
    private int reservedQty;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
