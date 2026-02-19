package com.ecommerce_backend.Entity.Fulfillment;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(
        name = "ecomm_order_snapshot",
        indexes = {
                @Index(name = "idx_order_number", columnList = "order_number"),
                @Index(name = "idx_order_status", columnList = "order_status"),
                @Index(name = "idx_order_customer_email", columnList = "customer_email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "order_seq"
    )
    @SequenceGenerator(
            name = "order_seq",
            sequenceName = "order_seq",
            initialValue = 80000001,
            allocationSize = 1
    )
    @EqualsAndHashCode.Include
    private Long orderId;

    @Size(max = 30)
    @Column(
            name = "order_number",
            nullable = false,
            unique = true,
            length = 30
    )
    private String orderNumber;

    @Column(name = "customer_name", nullable = false, updatable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false, updatable = false)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false, length = 15)
    private String recipientPhone;

    @Column(nullable = false, updatable = false)
    private String deliveryAddressLine1;

    @Column(updatable = false)
    private String deliveryAddressLine2;

    @Column(nullable = false, updatable = false)
    private String deliveryCity;

    @Column(nullable = false, updatable = false)
    private String deliveryState;

    @Column(nullable = false, updatable = false)
    private String deliveryPincode;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderLine> orderLines = new ArrayList<>();

    @Column(length = 3, nullable = false, updatable = false)
    private String currency; // INR

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal charges;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal totalAmount;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("occurredAt DESC")
    private List<StateTransition> stateTransitions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    private Instant createDate;

    @UpdateTimestamp
    @Column(name = "update_date")
    private Instant updateDate;


    @PrePersist
    public void generateOrderNumber() {
        if (this.orderNumber == null) {
            String datePart = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            this.orderNumber = String.format("ORD-%s-%s", datePart, UUID.randomUUID().toString().substring(0, 6));
        }
    }

    public void attachPayment(Payment payment) {
        if (this.orderStatus != OrderStatus.CREATED) {
            throw new IllegalStateException("Cannot attach payment after order is confirmed");
        }
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }

        this.payment = payment;
        payment.setOrder(this);
    }

    public void addOrderLine(OrderLine orderLine) {
        this.orderLines.add(orderLine);
        orderLine.setOrder(this);
    }

    public void addStateTransition(StateTransition stateTransition) {
        this.stateTransitions.add(stateTransition);
        stateTransition.setOrder(this);
    }

    @Transient
    public Map<OrderStatus, List<StateTransition>> getStateTransitionsByStatus() {
        return stateTransitions == null
                ? Map.of()
                : stateTransitions.stream()
                .collect(Collectors.groupingBy(StateTransition::getStatus));
    }
}