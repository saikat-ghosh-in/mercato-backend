package com.ecommerce_backend.Entity.Fulfillment;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "order_lines_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_order_line_order_line_number", columnNames = {"order_fk", "order_line_number"})
        },
        indexes = {
                @Index(name = "idx_order_line_order_fk", columnList = "order_fk"),
                @Index(name = "idx_order_line_product_id", columnList = "product_id"),
                @Index(name = "idx_order_line_status", columnList = "order_line_status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderLine {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "order_line_seq"
    )
    @SequenceGenerator(
            name = "order_line_seq",
            sequenceName = "order_line_seq",
            initialValue = 90000001,
            allocationSize = 1
    )
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Min(1)
    @Column(name = "order_line_number", nullable = false, updatable = false)
    private Integer orderLineNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_fk", referencedColumnName = "id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false, updatable = false)
    private String productId;

    @Column(nullable = false, updatable = false)
    private String productName;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false, updatable = false)
    @Min(1)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal lineTotal;

    @Column(updatable = false, length = 100)
    private String sellerName;

    @Column(updatable = false, length = 100)
    private String sellerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderLineStatus orderLineStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}