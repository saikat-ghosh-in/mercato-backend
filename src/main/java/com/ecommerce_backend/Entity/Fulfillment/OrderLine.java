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
        name = "order_lines",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_order_line_number",
                        columnNames = {"order_id", "order_line_number"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long orderLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @Min(1)
    @Column(name = "order_line_number", nullable = false, updatable = false)
    private Integer orderLineNumber;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, updatable = false)
    private String productName;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false, updatable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal lineTotal;

    @Column(updatable = false)
    private String sellerName;

    @Column(updatable = false)
    private String sellerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderLineStatus orderLineStatus;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createDate;

    @UpdateTimestamp
    private Instant updateDate;
}