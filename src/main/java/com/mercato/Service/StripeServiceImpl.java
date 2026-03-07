package com.mercato.Service;

import com.mercato.Entity.Address;
import com.mercato.Entity.EcommUser;
import com.mercato.Entity.fulfillment.*;
import com.mercato.ExceptionHandler.CustomBadRequestException;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Payloads.Request.PaymentConfirmationRequestDTO;
import com.mercato.Payloads.Request.StripePaymentRequestDTO;
import com.mercato.Payloads.Response.PaymentConfirmationResponseDTO;
import com.mercato.Payloads.Response.PaymentResponseDTO;
import com.mercato.Repository.OrderRepository;
import com.mercato.Repository.PaymentRepository;
import com.mercato.Utils.AuthUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final AuthUtil authUtil;
    private final AddressService addressService;
    private final OrderReservationService orderReservationService;

    @Override
    @Transactional(readOnly = true)
    public PaymentIntent createStripePaymentIntent(StripePaymentRequestDTO stripePaymentRequestDTO) throws StripeException {
        EcommUser user = authUtil.getLoggedInUser();
        Customer customer;
        try {
            customer = retrieveStripeCustomerFromEmail(user.getEmail());
        } catch (StripeException e) {
            Address address = addressService.getAddressById(stripePaymentRequestDTO.getAddressId());
            customer = createStripeCustomerFromEmail(user, address);
        }

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setCustomer(customer.getId())
                        .setAmount(stripePaymentRequestDTO.getAmount())
                        .setCurrency(stripePaymentRequestDTO.getCurrency())
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();
        return PaymentIntent.create(params);
    }

    @Override
    @Transactional
    public void initiatePayment(Order order, PaymentMethod paymentMethod) {
        Payment payment = new Payment();

        payment.setAmount(order.getTotalAmount());
        payment.setCurrency(order.getCurrency());
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.INITIATED); // initiating payment
        payment.setInitiatedAt(Instant.now());

        order.attachPayment(payment);
        order.setPaymentStatus(PaymentStatus.INITIATED);
    }


    @Override
    @Transactional
    public PaymentConfirmationResponseDTO confirmPayment(
            PaymentConfirmationRequestDTO paymentConfirmationRequestDTO) {

        Payment payment = paymentRepository.findById(paymentConfirmationRequestDTO.getPaymentId())
                .orElseThrow(() -> new CustomBadRequestException("Payment not found"));

        if (!PaymentStatus.INITIATED.equals(payment.getStatus())) {
            throw new RuntimeException("Payment already confirmed or invalid state");
        }

        updatePayment(payment, paymentConfirmationRequestDTO);

        Order order = orderRepository.findByOrderIdWithLines(
                        paymentConfirmationRequestDTO.getOrderId()
                )
                .orElseThrow(() -> new CustomBadRequestException("Order not found"));

        order.confirmOrder();
        recordConfirmationTransitions(order);
        orderReservationService.reserveForOrder(order);
        orderRepository.save(order);

        return PaymentConfirmationResponseDTO.builder()
                .success(true)
                .message("Payment confirmation successful")
                .orderId(order.getOrderId())
                .amount(order.getTotalAmount())
                .paymentStatus("completed")
                .pgPaymentId(payment.getGatewayReference())
                .build();
    }

    private void updatePayment(Payment payment,
                               PaymentConfirmationRequestDTO dto) {
        payment.setGatewayName(dto.getPgName());
        payment.setGatewayReference(dto.getPgPaymentId());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setGatewayResponseMessage(dto.getPgResponseMessage());
        payment.setCompletedAt(Instant.now());
        paymentRepository.save(payment);
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
        CustomerSearchParams params =
                CustomerSearchParams.builder()
                        .setQuery(String.format("email:'%s'", email))
                        .build();
        CustomerSearchResult customers = Customer.search(params);
        if (customers.getData().isEmpty()) {
            throw new ResourceNotFoundException("Customer", "email", email);
        }
        return customers.getData().get(0);
    }

    private Customer createStripeCustomerFromEmail(EcommUser user, Address billingAddress) throws StripeException {
        try {
            return retrieveStripeCustomerFromEmail(user.getEmail());
        } catch (StripeException e) {

            CustomerCreateParams params =
                    CustomerCreateParams.builder()
                            .setName(user.getUsername())
                            .setEmail(user.getEmail())
                            .setPhone(billingAddress.getRecipientPhone())
                            .setAddress(
                                    CustomerCreateParams.Address.builder()
                                            .setLine1(billingAddress.getAddressLine1())
                                            .setLine2(billingAddress.getAddressLine2())
                                            .setCity(billingAddress.getCity())
                                            .setState(billingAddress.getState())
                                            .setCountry(billingAddress.getCountry())
                                            .setPostalCode(billingAddress.getPincode())
                                            .build()
                            )
                            .build();
            return Customer.create(params);
        }
    }

    @Override
    public PaymentResponseDTO buildPaymentDto(Payment payment) {
        return new PaymentResponseDTO(
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
