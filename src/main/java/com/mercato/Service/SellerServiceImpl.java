package com.mercato.Service;

import com.mercato.Entity.EcommUser;
import com.mercato.Entity.fulfillment.OrderLine;
import com.mercato.ExceptionHandler.ForbiddenOperationException;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Mapper.FulfillmentOrderMapper;
import com.mercato.Payloads.Response.FulfillmentOrderResponseDTO;
import com.mercato.Payloads.Response.SellerDashboardStatsDTO;
import com.mercato.Repository.OrderLineRepository;
import com.mercato.Repository.ProductRepository;
import com.mercato.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final OrderLineRepository orderLineRepository;
    private final ProductRepository productRepository;
    private final AuthUtil authUtil;

    @Override
    @Transactional(readOnly = true)
    public List<FulfillmentOrderResponseDTO> getAllFulfillmentOrders() {
        EcommUser seller = authUtil.getLoggedInUser();

        List<OrderLine> lines = orderLineRepository
                .findAllBySellerEmail(seller.getEmail());

        return groupByFulfillmentId(lines);
    }

    @Override
    @Transactional(readOnly = true)
    public FulfillmentOrderResponseDTO getFulfillmentOrder(String fulfillmentId) {
        EcommUser seller = authUtil.getLoggedInUser();

        List<OrderLine> lines = orderLineRepository
                .findAllByFulfillmentIdAndSellerEmail(fulfillmentId, seller.getEmail());

        if (lines.isEmpty()) {
            throw new ResourceNotFoundException("FulfillmentOrder", "fulfillmentId", fulfillmentId);
        }

        return FulfillmentOrderMapper.toDto(fulfillmentId, lines);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerDashboardStatsDTO getDashboardStats() {
        EcommUser seller = authUtil.getLoggedInUser();
        if (!seller.isSeller() && !seller.isAdmin()) {
            throw new ForbiddenOperationException(
                    "You are not authorized to access seller dashboard"
            );
        }
        String sellerEmail = seller.getEmail();

        Instant startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfToday = startOfToday.plus(1, ChronoUnit.DAYS);
        Instant startOfMonth = LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();

        BigDecimal allTimeRevenue = orderLineRepository.findTotalRevenueBySeller(sellerEmail);
        BigDecimal thisMonthRevenue = orderLineRepository.findRevenueBySellerBetween(
                sellerEmail, startOfMonth, endOfToday
        );
        BigDecimal todayRevenue = orderLineRepository.findRevenueBySellerBetween(
                sellerEmail, startOfToday, endOfToday
        );

        long totalOrders = orderLineRepository.countDistinctOrdersBySeller(sellerEmail);
        long totalOrderLines = orderLineRepository.countOrderLinesBySeller(sellerEmail);
        BigDecimal averageOrderValue = orderLineRepository.findAverageOrderValueBySeller(sellerEmail);

        List<SellerDashboardStatsDTO.TopSellingProductDTO> topSellingProducts =
                orderLineRepository.findTopSellingProductsBySeller(sellerEmail)
                        .stream()
                        .map(row -> new SellerDashboardStatsDTO.TopSellingProductDTO(
                                (String) row[0],
                                (String) row[1],
                                ((Number) row[2]).intValue(),
                                (BigDecimal) row[3]
                        ))
                        .toList();

        List<SellerDashboardStatsDTO.InventoryAlertDTO> inventoryAlerts =
                productRepository.findLowStockProductsBySeller(sellerEmail, 5)
                        .stream()
                        .map(p -> new SellerDashboardStatsDTO.InventoryAlertDTO(
                                p.getProductId(),
                                p.getProductName(),
                                p.getPhysicalQty(),
                                p.getReservedQty(),
                                p.getAvailableQty()
                        ))
                        .toList();

        Map<String, Long> statusBreakdown = orderLineRepository
                .findOrderLineStatusBreakdownBySeller(sellerEmail)
                .stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1]
                ));

        return new SellerDashboardStatsDTO(
                new SellerDashboardStatsDTO.RevenueStats(
                        allTimeRevenue, thisMonthRevenue, todayRevenue
                ),
                new SellerDashboardStatsDTO.OrderStats(
                        totalOrders, totalOrderLines, averageOrderValue
                ),
                topSellingProducts,
                inventoryAlerts,
                statusBreakdown
        );
    }


    private List<FulfillmentOrderResponseDTO> groupByFulfillmentId(List<OrderLine> lines) {
        Map<String, List<OrderLine>> grouped = lines.stream()
                .collect(Collectors.groupingBy(OrderLine::getFulfillmentId));

        return grouped.entrySet().stream()
                .map(entry ->
                        FulfillmentOrderMapper.toDto(entry.getKey(), entry.getValue()))
                .toList();
    }
}