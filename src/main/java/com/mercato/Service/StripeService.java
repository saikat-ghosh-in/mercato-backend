package com.mercato.Service;

import com.mercato.Entity.fulfillment.Order;
import com.mercato.Entity.fulfillment.Payment;
import com.mercato.Entity.fulfillment.PaymentMethod;
import com.mercato.Payloads.Request.PaymentConfirmationRequestDTO;
import com.mercato.Payloads.Request.StripePaymentRequestDTO;
import com.mercato.Payloads.Response.PaymentConfirmationResponseDTO;
import com.mercato.Payloads.Response.PaymentResponseDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

public interface StripeService {

    PaymentIntent createStripePaymentIntent(StripePaymentRequestDTO stripePaymentRequestDTO) throws StripeException;

    void initiatePayment(Order order, PaymentMethod paymentMethod);

    PaymentResponseDTO buildPaymentDto(Payment payment);

    PaymentConfirmationResponseDTO confirmPayment(PaymentConfirmationRequestDTO paymentConfirmationRequestDTO);
}
