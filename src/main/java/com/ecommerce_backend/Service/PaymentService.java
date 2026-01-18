package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Fulfillment.Order;
import com.ecommerce_backend.Entity.Fulfillment.Payment;
import com.ecommerce_backend.Entity.Fulfillment.PaymentMethod;
import com.ecommerce_backend.Payloads.PaymentDto;

public interface PaymentService {

    void initiatePayment(Order order, PaymentMethod paymentMethod);

    PaymentDto buildPaymentDto(Payment payment);
}
