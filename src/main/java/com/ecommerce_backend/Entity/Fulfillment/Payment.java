package com.ecommerce_backend.Entity.Fulfillment;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_order", columnList = "order_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_gateway_ref", columnList = "gateway_reference")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Payment {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "payment_seq"
    )
    @SequenceGenerator(
            name = "payment_seq",
            sequenceName = "payment_seq",
            initialValue = 200000001,
            allocationSize = 10
    )
    @EqualsAndHashCode.Include
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 3, nullable = false)
    private String currency; // INR

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "gateway_reference", length = 100, unique = true)
    private String gatewayReference;

    @Column(name = "gateway_name", length = 50)
    private String gatewayName;

    @Column(name = "gateway_response_message")
    private String gatewayResponseMessage;

    private Instant initiatedAt;
    private Instant completedAt;
}