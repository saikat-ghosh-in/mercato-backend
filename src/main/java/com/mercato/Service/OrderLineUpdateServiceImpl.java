package com.mercato.Service;

import com.mercato.Entity.fulfillment.*;
import com.mercato.ExceptionHandler.CustomBadRequestException;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Payloads.Request.OrderLineUpdateRequestDTO;
import com.mercato.Payloads.Response.OrderLineResponseDTO;
import com.mercato.Repository.OrderLineRepository;
import com.mercato.Repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderLineUpdateServiceImpl implements OrderLineUpdateService {

    private final OrderLineRepository orderLineRepository;
    private final OrderRepository orderRepository;
    private final OrderReservationService orderReservationService;

    @Override
    @Transactional
    public OrderLineResponseDTO updateOrderLine(OrderLineUpdateRequestDTO request, TransitionTrigger trigger) {

        if (trigger == TransitionTrigger.CUSTOMER
                && request.getAction() != OrderLineAction.CANCEL) {
            throw new CustomBadRequestException(
                    "Customers can only cancel order lines"
            );
        }

        OrderLine orderLine = orderLineRepository
                .findByFulfillmentIdAndOrderLineNumberForUpdate(
                        request.getFulfillmentId(),
                        request.getOrderLineNumber()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OrderLine", "fulfillmentId", request.getFulfillmentId()
                ));

        if (orderLine.isTerminal()) {
            throw new CustomBadRequestException(
                    "Order line is already in terminal status: " + orderLine.getOrderLineStatus()
            );
        }

        OrderLineStatus fromStatus = orderLine.getOrderLineStatus();
        int qty = request.getQty() != null ? request.getQty() : 0;

        switch (request.getAction()) {
            case ACCEPT -> handleAccept(orderLine, qty);
            case SHIP -> handleShip(orderLine, qty);
            case CANCEL -> handleCancel(orderLine, qty);
            default -> throw new IllegalArgumentException(
                    "Unknown action: " + request.getAction()
            );
        }

        OrderLineStatus toStatus = request.getAction() == OrderLineAction.ACCEPT
                ? OrderLineStatus.PROCESSING
                : orderLine.deriveStatus();
        orderLine.setOrderLineStatus(toStatus);

        orderLine.addStateTransition(
                StateTransition.builder()
                        .orderLine(orderLine)
                        .fromStatus(fromStatus)
                        .toStatus(toStatus)
                        .action(request.getAction())
                        .triggeredBy(trigger)
                        .qtyAffected(request.getAction() == OrderLineAction.ACCEPT ? null : qty)
                        .reason(request.getReason())
                        .build()
        );

        syncOrderStatus(orderLine.getOrder());
        orderLineRepository.save(orderLine);

        return buildOrderLineDto(orderLine);
    }

    private void handleAccept(OrderLine orderLine, int qty) {
        validateProcessingState(orderLine);
        int acceptQty = qty > 0 ? qty : orderLine.getOrderedQty();
        orderLine.accept(acceptQty);
    }

    private void handleShip(OrderLine orderLine, int qty) {
        validateQty(qty, "SHIP", orderLine);
        orderLine.ship(qty);
        orderReservationService.settleQty(orderLine, qty, OrderLineAction.SHIP);
    }

    private void handleCancel(OrderLine orderLine, int qty) {
        validateQty(qty, "CANCEL", orderLine);
        orderLine.cancel(qty);
        orderReservationService.settleQty(orderLine, qty, OrderLineAction.CANCEL);
    }

    private void validateProcessingState(OrderLine orderLine) {
        if (orderLine.getOrderLineStatus() != OrderLineStatus.CONFIRMED
                && orderLine.getOrderLineStatus() != OrderLineStatus.PROCESSING) {
            throw new CustomBadRequestException(
                    "ACCEPT action requires line to be in CONFIRMED or PROCESSING status"
            );
        }
    }

    private void validateQty(int qty, String action, OrderLine orderLine) {
        if (qty <= 0) {
            throw new CustomBadRequestException(
                    action + " action requires qty > 0"
            );
        }
        if (orderLine.getPendingQty() < qty) {
            throw new CustomBadRequestException(
                    MessageFormat.format("Cannot {0} {1} units, only {2} pending",
                            action, qty, orderLine.getPendingQty())
            );
        }
    }

    private void syncOrderStatus(Order order) {
        List<OrderLine> lines = order.getOrderLines();

        boolean allCancelled = lines.stream()
                .allMatch(l -> l.getOrderLineStatus() == OrderLineStatus.CANCELLED);

        boolean allClosed = lines.stream()
                .allMatch(l -> l.getOrderLineStatus() == OrderLineStatus.FULFILLED
                        || l.getOrderLineStatus() == OrderLineStatus.PARTIALLY_FULFILLED
                        || l.getOrderLineStatus() == OrderLineStatus.CANCELLED);

        boolean anyProcessing = lines.stream()
                .anyMatch(l -> l.getOrderLineStatus() == OrderLineStatus.PROCESSING
                        || l.getOrderLineStatus() == OrderLineStatus.PARTIALLY_PROCESSED);

        boolean allConfirmed = lines.stream()
                .allMatch(l -> l.getOrderLineStatus() == OrderLineStatus.CONFIRMED);

        if (allCancelled) {
            order.setOrderStatus(OrderStatus.CANCELLED);
        } else if (allClosed) {
            order.setOrderStatus(OrderStatus.FULFILLMENT_COMPLETE);
        } else if (anyProcessing) {
            order.setOrderStatus(OrderStatus.FULFILLMENT_PROCESSING);
        } else if (allConfirmed) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
        }

        orderRepository.save(order);
    }

    private OrderLineResponseDTO buildOrderLineDto(OrderLine orderLine) {
        return new OrderLineResponseDTO(
                orderLine.getOrder().getOrderId(),
                orderLine.getOrderLineNumber(),
                orderLine.getOrderLineStatus().toString(),
                new OrderLineResponseDTO.ProductDetails(
                        orderLine.getProductId(),
                        orderLine.getProductName(),
                        orderLine.getUnitPrice()
                ),
                new OrderLineResponseDTO.Seller(
                        orderLine.getSellerName(),
                        orderLine.getSellerEmail()
                ),
                orderLine.getOrderedQty(),
                orderLine.getAcceptedQty(),
                orderLine.getShippedQty(),
                orderLine.getCancelledQty(),
                orderLine.getPendingQty(),
                orderLine.getLineTotal(),
                orderLine.getCreatedAt(),
                orderLine.getUpdatedAt()
        );
    }
}