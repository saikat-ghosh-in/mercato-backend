package com.mercato.Service;

import com.mercato.Payloads.Response.FulfillmentOrderResponseDTO;

import java.util.List;

public interface FulfillmentService {

    List<FulfillmentOrderResponseDTO> getOpenFulfillmentOrders();

    List<FulfillmentOrderResponseDTO> getClosedFulfillmentOrders();

    FulfillmentOrderResponseDTO getFulfillmentOrder(String fulfillmentId);
}
