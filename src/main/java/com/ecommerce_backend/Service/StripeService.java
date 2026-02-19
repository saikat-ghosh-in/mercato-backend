package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Fulfillment.Order;
import com.ecommerce_backend.Entity.Fulfillment.Payment;
import com.ecommerce_backend.Entity.Fulfillment.PaymentMethod;
import com.ecommerce_backend.Payloads.Request.PaymentConfirmationRequestDTO;
import com.ecommerce_backend.Payloads.Request.StripePaymentRequestDTO;
import com.ecommerce_backend.Payloads.Response.PaymentConfirmationResponseDTO;
import com.ecommerce_backend.Payloads.Response.PaymentDto;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

public interface StripeService {

    PaymentIntent createStripePaymentIntent(StripePaymentRequestDTO stripePaymentRequestDTO) throws StripeException;

    void initiatePayment(Order order, PaymentMethod paymentMethod);

    PaymentDto buildPaymentDto(Payment payment);

    PaymentConfirmationResponseDTO confirmPayment(PaymentConfirmationRequestDTO paymentConfirmationRequestDTO);
}
