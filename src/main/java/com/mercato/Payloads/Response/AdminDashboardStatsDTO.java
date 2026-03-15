package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class AdminDashboardStatsDTO {
    private final UserStats users;
    private final OrderStats order;
    private final RevenueStats revenue;
    private final List<TopSellingProductDTO> topSellingProducts;
    private final Map<String, Long> orderStatusBreakdown;
    private final List<RecentOrderDTO> recentOrders;

    @Getter
    @AllArgsConstructor
    public static class UserStats {
        private final long total;
        private final long sellers;
        private final long admins;
        private final long newThisMonth;
    }

    @Getter
    @AllArgsConstructor
    public static class RecentOrderDTO {
        private final String orderId;
        private final String customerName;
        private final String customerEmail;
        private final String orderStatus;
        private final BigDecimal totalAmount;
        private final Instant createdAt;
    }
}
