package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Fulfillment.Order;
import com.ecommerce_backend.Entity.Fulfillment.Payment;
import com.ecommerce_backend.Entity.Fulfillment.PaymentMethod;
import com.ecommerce_backend.Entity.Fulfillment.PaymentStatus;
import com.ecommerce_backend.Payloads.Response.PaymentDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Override
    @Transactional
    public void initiatePayment(Order order, PaymentMethod paymentMethod) {
        Payment payment = new Payment();

        payment.setAmount(order.getTotalAmount());
        payment.setCurrency(order.getCurrency());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.INITIATED); // initiating payment
        payment.setInitiatedAt(Instant.now());

        order.attachPayment(payment);
        order.setPaymentStatus(PaymentStatus.INITIATED);
    }

    @Override
    public PaymentDto buildPaymentDto(Payment payment) {
        return new PaymentDto(
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod().toString(),
                payment.getStatus().toString(),
                payment.getGatewayReference(),
                payment.getGatewayName(),
                payment.getGatewayResponseMessage(),
                payment.getInitiatedAt(),
                payment.getCompletedAt()
        );
    }
}
