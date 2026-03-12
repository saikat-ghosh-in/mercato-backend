package com.mercato.Service;

import com.mercato.Entity.EcommUser;
import com.mercato.Entity.fulfillment.Order;
import com.mercato.Entity.fulfillment.payment.PaymentMethod;
import com.mercato.Payloads.Response.CashfreeOrderResponse;

import java.math.BigDecimal;

public interface CashfreeService {

    CashfreeOrderResponse createOrder(Order order, EcommUser user);

    void initiatePayment(Order order, PaymentMethod paymentMethod, CashfreeOrderResponse cashfreeOrder);

    void terminateOrder(String cfOrderId);

    void handleWebhookEvent(String payload, String signature, String timestamp);

    String retryPayment(Order order, EcommUser user);

    String verifyAndSyncPayment(String orderId);

    String initiateRefund(Order order, String cfOrderId, BigDecimal amount);
}
