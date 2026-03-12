package com.mercato.Mapper;

import com.mercato.Entity.fulfillment.OrderLine;
import com.mercato.Entity.fulfillment.StateTransition;
import com.mercato.Payloads.Response.OrderLineResponseDTO;
import com.mercato.Payloads.Response.StateTransitionResponseDTO;

import java.util.Comparator;
import java.util.List;

public class OrderLineMapper {

    public static OrderLineResponseDTO toDto(OrderLine orderLine) {
        List<StateTransitionResponseDTO> stateTransitionDTOs = orderLine.getStateTransitions()
                .stream()
                .sorted(Comparator.comparing(StateTransition::getOccurredAt).reversed())
                .map(t -> new StateTransitionResponseDTO(
                        t.getFromStatus().toString(),
                        t.getToStatus().toString(),
                        t.getAction().toString(),
                        t.getTriggeredBy().toString(),
                        t.getQtyAffected(),
                        t.getReason(),
                        t.getOccurredAt()
                ))
                .toList();

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
                stateTransitionDTOs,
                orderLine.getCreatedAt(),
                orderLine.getUpdatedAt()
        );
    }
}
