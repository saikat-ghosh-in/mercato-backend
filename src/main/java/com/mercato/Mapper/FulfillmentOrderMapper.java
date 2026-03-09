package com.mercato.Mapper;

import com.mercato.Entity.fulfillment.OrderLine;
import com.mercato.Payloads.Response.FulfillmentOrderResponseDTO;

import java.util.List;

public class FulfillmentOrderMapper {

    public static FulfillmentOrderResponseDTO toDto(
            String fulfillmentId,
            List<OrderLine> orderLines
    ) {
        OrderLine first = orderLines.get(0);

        return new FulfillmentOrderResponseDTO(
                fulfillmentId,
                first.getOrder().getOrderId(),
                new FulfillmentOrderResponseDTO.Customer(
                        first.getOrder().getCustomerName(),
                        first.getOrder().getCustomerEmail()
                ),
                new FulfillmentOrderResponseDTO.DeliveryAddress(
                        first.getOrder().getRecipientName(),
                        first.getOrder().getRecipientPhone(),
                        first.getOrder().getDeliveryAddressLine1(),
                        first.getOrder().getDeliveryAddressLine2(),
                        first.getOrder().getDeliveryCity(),
                        first.getOrder().getDeliveryState(),
                        first.getOrder().getDeliveryPincode()
                ),
                orderLines.stream().map(OrderLineMapper::toDto).toList(),
                first.getCreatedAt()
        );
    }
}
