package com.mercato.Service;

import com.mercato.Entity.EcommUser;
import com.mercato.Entity.fulfillment.*;
import com.mercato.Entity.fulfillment.payment.Payment;
import com.mercato.Entity.fulfillment.payment.PaymentMethod;
import com.mercato.Entity.fulfillment.payment.PaymentStatus;
import com.mercato.ExceptionHandler.CustomBadRequestException;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Repository.OrderRepository;
import com.mercato.Repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderReservationService orderReservationService;

    @Override
    @Transactional
    public PaymentIntent createPaymentIntent(Order order, EcommUser user) throws StripeException {

        if (!OrderStatus.CREATED.equals(order.getOrderStatus())) {
            throw new CustomBadRequestException("Order is not in a payable state");
        }

        Payment existingPayment = order.getPayment();
        if (existingPayment != null && existingPayment.getGatewayReference() != null) {
            String stripeStatus = PaymentIntent.retrieve(existingPayment.getGatewayReference()).getStatus();
            if ("requires_payment_method".equals(stripeStatus) || "requires_confirmation".equals(stripeStatus)) {
                throw new CustomBadRequestException("A reusable payment intent already exists, use retry payment instead");
            }
        }

        Customer customer = getOrCreateStripeCustomer(user, order);

        long amountInSmallestUnit = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setCustomer(customer.getId())
                .setAmount(amountInSmallestUnit)
                .setCurrency(order.getCurrency().toLowerCase())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .putMetadata("orderId", order.getOrderId())
                .setShipping(
                        PaymentIntentCreateParams.Shipping.builder()
                                .setName(order.getRecipientName())
                                .setPhone(order.getRecipientPhone())
                                .setAddress(
                                        PaymentIntentCreateParams.Shipping.Address.builder()
                                                .setLine1(order.getDeliveryAddressLine1())
                                                .setLine2(order.getDeliveryAddressLine2())
                                                .setCity(order.getDeliveryCity())
                                                .setState(order.getDeliveryState())
                                                .setCountry("IN")
                                                .setPostalCode(order.getDeliveryPincode())
                                                .build()
                                )
                                .build()
                )
                .build();

        return PaymentIntent.create(params);
    }

    @Override
    public void cancelPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        paymentIntent.cancel();
        log.info("Cancelled PaymentIntent on Stripe: {}", paymentIntentId);
    }

    @Override
    @Transactional
    public void initiatePayment(Order order, PaymentMethod paymentMethod,
                                String paymentIntentId, String clientSecret) {
        if (paymentMethod != PaymentMethod.CARD) {
            throw new CustomBadRequestException(
                    "Only card payments are supported at this time"
            );
        }

        Payment payment = new Payment();
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency(order.getCurrency());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setGatewayReference(paymentIntentId);
        payment.setGatewayName("stripe");
        payment.setClientSecret(clientSecret);
        payment.setInitiatedAt(Instant.now());

        order.attachPayment(payment);
        order.setPaymentStatus(PaymentStatus.INITIATED);
    }

    @Override
    @Transactional
    public void handleWebhookEvent(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            throw new CustomBadRequestException("Invalid webhook signature");
        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage(), e);
            throw new RuntimeException("Webhook processing failed: " + e.getMessage());
        }

        log.info("Received webhook event: {}", event.getType());

        try {
            switch (event.getType()) {
                case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event);
                case "payment_intent.payment_failed" -> handlePaymentIntentFailed(event);
                case "payment_intent.canceled" -> handlePaymentIntentCancelled(event);
                default -> log.warn("Unhandled webhook event type: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("Webhook event processing error: {}", e.getMessage(), e);
            throw new RuntimeException("Webhook processing failed: " + e.getMessage());
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = getPaymentIntent(event);
        String gatewayReference = paymentIntent.getId();
        log.info("Processing payment_intent.succeeded for: {}", gatewayReference);

        Payment payment = findPaymentWithRetry(gatewayReference);

        if (PaymentStatus.SUCCESS.equals(payment.getStatus())) {
            log.info("Payment already confirmed for: {}", gatewayReference);
            return;
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setGatewayName("stripe");
        payment.setGatewayReference(gatewayReference);
        payment.setGatewayResponseMessage("Payment succeeded");
        payment.setCompletedAt(Instant.now());
        paymentRepository.save(payment);

        Order order = orderRepository.findByOrderIdWithLines(
                        payment.getOrder().getOrderId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "orderId", payment.getOrder().getOrderId()
                ));

        order.confirmOrder();
        recordConfirmationTransitions(order);
        orderReservationService.reserveForOrder(order);
        orderRepository.save(order);

        log.info("Order confirmed via webhook: {}", order.getOrderId());
    }

    private Payment findPaymentWithRetry(String gatewayReference) {
        int maxAttempts = 5;
        long delayMs = 1000;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Optional<Payment> payment = paymentRepository.findByGatewayReference(gatewayReference);
            if (payment.isPresent()) {
                return payment.get();
            }
            log.warn("Payment not found for: {} (attempt {}/{})", gatewayReference, attempt, maxAttempts);
            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        throw new ResourceNotFoundException("Payment", "gatewayReference", gatewayReference);
    }

    private void handlePaymentIntentFailed(Event event) {

        PaymentIntent paymentIntent = getPaymentIntent(event);
        String gatewayReference = paymentIntent.getId();
        log.warn("Processing payment_intent.payment_failed for: {}", gatewayReference);

        Payment payment = paymentRepository.findByGatewayReference(gatewayReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment", "gatewayReference", gatewayReference
                ));

        payment.setStatus(PaymentStatus.FAILED);
        payment.setGatewayResponseMessage(
                paymentIntent.getLastPaymentError() != null
                        ? paymentIntent.getLastPaymentError().getMessage()
                        : "Payment failed"
        );
        payment.setCompletedAt(Instant.now());
        paymentRepository.save(payment);

        log.warn("Payment failed for order: {}", payment.getOrder().getOrderId());
    }

    private void handlePaymentIntentCancelled(Event event) {
        PaymentIntent paymentIntent = getPaymentIntent(event);
        String gatewayReference = paymentIntent.getId();
        log.warn("Processing payment_intent.canceled for: {}", gatewayReference);

        paymentRepository.findByGatewayReference(gatewayReference).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setGatewayResponseMessage("Payment intent cancelled");
            paymentRepository.save(payment);
            log.warn("Payment cancelled for order: {}", payment.getOrder().getOrderId());
        });
    }

    private void recordConfirmationTransitions(Order order) {
        order.getOrderLines().forEach(orderLine ->
                orderLine.addStateTransition(
                        StateTransition.builder()
                                .orderLine(orderLine)
                                .fromStatus(OrderLineStatus.CREATED)
                                .toStatus(OrderLineStatus.CONFIRMED)
                                .action(OrderLineAction.CONFIRM)
                                .triggeredBy(TransitionTrigger.SYSTEM)
                                .reason("Payment confirmed")
                                .build()
                )
        );
    }

    private Customer retrieveStripeCustomerFromEmail(String email) throws StripeException {
        CustomerSearchParams params = CustomerSearchParams.builder()
                .setQuery(String.format("email:'%s'", email))
                .build();
        CustomerSearchResult customers = Customer.search(params);
        if (customers.getData().isEmpty()) {
            throw new ResourceNotFoundException("Customer", "email", email);
        }
        return customers.getData().get(0);
    }

    private Customer getOrCreateStripeCustomer(EcommUser user, Order order) throws StripeException {
        try {
            return retrieveStripeCustomerFromEmail(user.getEmail());
        } catch (Exception e) {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setName(user.getUsername())
                    .setEmail(user.getEmail())
                    .setPhone(order.getRecipientPhone())
                    .setAddress(
                            CustomerCreateParams.Address.builder()
                                    .setLine1(order.getDeliveryAddressLine1())
                                    .setLine2(order.getDeliveryAddressLine2())
                                    .setCity(order.getDeliveryCity())
                                    .setState(order.getDeliveryState())
                                    .setCountry("IN")
                                    .setPostalCode(order.getDeliveryPincode())
                                    .build()
                    )
                    .build();
            return Customer.create(params);
        }
    }

    private static PaymentIntent getPaymentIntent(Event event) {
        PaymentIntent paymentIntent;
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        if (deserializer.getObject().isPresent()) {
            paymentIntent = (PaymentIntent) deserializer.getObject().get();
        } else {
            try {
                paymentIntent = (PaymentIntent) deserializer.deserializeUnsafe();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to deserialize PaymentIntent for event: " + event.getId());
            }
        }

        return paymentIntent;
    }
}