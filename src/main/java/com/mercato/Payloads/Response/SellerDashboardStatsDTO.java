package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class SellerDashboardStatsDTO {
    private final RevenueStats revenue;
    private final OrderStats fulfillment;
    private final List<TopSellingProductDTO> topSellingProducts;
    private final List<InventoryAlertDTO> inventoryAlerts;
    private final Map<String, Long> orderLineStatusBreakdown;

    @Getter
    @AllArgsConstructor
    public static class InventoryAlertDTO {
        private final String productId;
        private final String productName;
        private final int physicalQty;
        private final int reservedQty;
        private final int availableQty;
    }
}
