package com.mercato.Service;

import com.mercato.Entity.fulfillment.Order;
import com.mercato.Entity.fulfillment.OrderLine;
import com.mercato.Entity.fulfillment.OrderLineStatus;
import com.mercato.Entity.fulfillment.payment.Refund;
import com.mercato.Entity.fulfillment.payment.RefundStatus;
import com.mercato.Repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final CashfreeService cashfreeService;

    @Override
    @Transactional
    public void processRefundIfRequired(Order order) {

        if (refundRepository.existsByPayment_Id(order.getPayment().getId())) {
            log.warn("Refund already processed for order: {}", order.getOrderId());
            return;
        }

        BigDecimal refundAmount = calculateRefundAmount(order);
        if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
            log.info("No refund required for order: {}", order.getOrderId());
            return;
        }

        String cfOrderId = order.getPayment().getCfOrderId();
        if (cfOrderId == null) {
            log.error("No Cashfree order ID found for order: {}", order.getOrderId());
            return;
        }

        String refundId = "RFD-" + order.getOrderId() + "-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase();
        Refund refund = Refund.builder()
                .refundId(refundId)
                .payment(order.getPayment())
                .amount(refundAmount)
                .currency(order.getCurrency())
                .status(RefundStatus.PENDING)
                .reason("Order cancellation")
                .build();

        try {
            String gatewayRefundId = cashfreeService.initiateRefund(
                    order,
                    refund
            );

            refund.setGatewayReference(gatewayRefundId);

            log.info("Refund initiated for order: {}, refundId: {}, amount: {}",
                    order.getOrderId(), gatewayRefundId, refundAmount);

        } catch (Exception e) {
            refund.setStatus(RefundStatus.FAILED);
            refund.setFailureReason(e.getMessage());
            log.error("Refund failed for order: {}, reason: {}",
                    order.getOrderId(), e.getMessage());
        }

        refundRepository.save(refund);
    }

    private BigDecimal calculateRefundAmount(Order order) {
        List<OrderLine> lines = order.getOrderLines();

        boolean nothingShipped = lines.stream()
                .allMatch(l -> l.getShippedQty() == 0);

        boolean allCancelled = lines.stream()
                .allMatch(l -> l.getOrderLineStatus() == OrderLineStatus.CANCELLED);

        if (allCancelled && nothingShipped) {
            return order.getTotalAmount();
        }

        return lines.stream()
                .map(ol -> ol.getUnitPrice()
                        .multiply(BigDecimal.valueOf(ol.getCancelledQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}