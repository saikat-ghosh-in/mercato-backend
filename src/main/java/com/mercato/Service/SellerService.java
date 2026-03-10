package com.mercato.Service;

import com.mercato.Payloads.Response.FulfillmentOrderResponseDTO;
import com.mercato.Payloads.Response.SellerDashboardStatsDTO;

import java.util.List;

public interface SellerService {

    List<FulfillmentOrderResponseDTO> getAllFulfillmentOrders();

    FulfillmentOrderResponseDTO getFulfillmentOrder(String fulfillmentId);

    SellerDashboardStatsDTO getDashboardStats();
}
