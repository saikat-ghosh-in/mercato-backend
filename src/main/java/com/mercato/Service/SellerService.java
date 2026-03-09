package com.mercato.Service;

import com.mercato.Payloads.Response.FulfillmentOrderResponseDTO;

import java.util.List;

public interface SellerService {

    List<FulfillmentOrderResponseDTO> getAllFulfillmentOrders();

    FulfillmentOrderResponseDTO getFulfillmentOrder(String fulfillmentId);
}
