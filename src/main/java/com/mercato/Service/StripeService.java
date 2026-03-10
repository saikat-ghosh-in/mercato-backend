package com.mercato.Service;

import com.mercato.Entity.EcommUser;
import com.mercato.Entity.fulfillment.Order;
import com.mercato.Entity.fulfillment.payment.PaymentMethod;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

public interface StripeService {

    PaymentIntent createPaymentIntent(Order order, EcommUser user) throws StripeException;

    void cancelPaymentIntent(String paymentIntentId) throws StripeException;

    void initiatePayment(Order order, PaymentMethod paymentMethod,
                         String paymentIntentId, String clientSecret);

    void handleWebhookEvent(String payload, String sigHeader);
}
