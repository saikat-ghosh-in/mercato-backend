package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class SellerDashboardStatsDTO {
    private final RevenueStats revenue;
    private final OrderStats orders;
    private final List<TopSellingProductDTO> topSellingProducts;
    private final List<InventoryAlertDTO> inventoryAlerts;
    private final Map<String, Long> orderLineStatusBreakdown;

    @Getter
    @AllArgsConstructor
    public static class RevenueStats {
        private final BigDecimal allTime;
        private final BigDecimal thisMonth;
        private final BigDecimal today;
    }

    @Getter
    @AllArgsConstructor
    public static class OrderStats {
        private final long totalOrders;
        private final long totalOrderLines;
        private final BigDecimal averageOrderValue;
    }

    @Getter
    @AllArgsConstructor
    public static class TopSellingProductDTO {
        private final String productId;
        private final String productName;
        private final int totalShippedQty;
        private final BigDecimal totalRevenue;
    }

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
