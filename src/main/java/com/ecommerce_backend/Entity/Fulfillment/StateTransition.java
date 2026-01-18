package com.ecommerce_backend.Entity.Fulfillment;

import com.ecommerce_backend.Entity.EcommUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "order_state_transitions",
        indexes = {
                @Index(name = "idx_transition_order", columnList = "order_id"),
                @Index(name = "idx_transition_status", columnList = "status"),
                @Index(name = "idx_transition_time", columnList = "occurred_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StateTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long stateTransitionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @Min(1)
    @Column(name = "order_line_number", nullable = false)
    private Integer orderLineNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(updatable = false)
    private String sellerName;

    @Column(updatable = false)
    private String sellerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransitionActorType actorType;

    private String note;
}
