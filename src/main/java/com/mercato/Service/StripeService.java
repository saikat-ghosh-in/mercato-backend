package com.mercato.Service;

import com.mercato.Entity.fulfillment.Order;
import com.mercato.Entity.fulfillment.payment.PaymentMethod;
import com.mercato.Payloads.Request.PaymentConfirmationRequestDTO;
import com.mercato.Payloads.Request.StripePaymentRequestDTO;
import com.mercato.Payloads.Response.PaymentConfirmationResponseDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

public interface StripeService {

    PaymentIntent createStripePaymentIntent(StripePaymentRequestDTO stripePaymentRequestDTO) throws StripeException;

    void initiatePayment(Order order, PaymentMethod paymentMethod);

    PaymentConfirmationResponseDTO confirmPayment(PaymentConfirmationRequestDTO paymentConfirmationRequestDTO);
}
