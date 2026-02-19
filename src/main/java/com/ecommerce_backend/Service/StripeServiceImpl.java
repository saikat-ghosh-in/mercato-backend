package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Address;
import com.ecommerce_backend.Entity.EcommUser;
import com.ecommerce_backend.Entity.Fulfillment.*;
import com.ecommerce_backend.ExceptionHandler.CustomBadRequestException;
import com.ecommerce_backend.ExceptionHandler.ResourceNotFoundException;
import com.ecommerce_backend.Payloads.Request.PaymentConfirmationRequestDTO;
import com.ecommerce_backend.Payloads.Request.StripePaymentRequestDTO;
import com.ecommerce_backend.Payloads.Response.PaymentConfirmationResponseDTO;
import com.ecommerce_backend.Payloads.Response.PaymentDto;
import com.ecommerce_backend.Repository.OrderRepository;
import com.ecommerce_backend.Repository.PaymentRepository;
import com.ecommerce_backend.Utils.AuthUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Override
    public PaymentIntent createStripePaymentIntent(StripePaymentRequestDTO stripePaymentRequestDTO) throws StripeException {
        EcommUser user = authUtil.getLoggedInUser();
        Customer customer = new Customer();
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
    public PaymentConfirmationResponseDTO confirmPayment(PaymentConfirmationRequestDTO paymentConfirmationRequestDTO) {
        // 1. Find payment by paymentId
        Payment payment = paymentRepository.findById(paymentConfirmationRequestDTO.getPaymentId())
                .orElseThrow(() -> new CustomBadRequestException("Payment not found"));

        // 2. Verify payment status is PENDING
        if (!PaymentStatus.INITIATED.equals(payment.getStatus())) {
            throw new RuntimeException("Payment already confirmed or invalid state");
        }

        // 3. Update payment with gateway details
        payment.setGatewayName(paymentConfirmationRequestDTO.getPgName());
        payment.setGatewayReference(paymentConfirmationRequestDTO.getPgPaymentId());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setGatewayResponseMessage(paymentConfirmationRequestDTO.getPgResponseMessage());
        payment.setCompletedAt(Instant.now());
        paymentRepository.save(payment);

        // 4. Find and update order
        Order order = orderRepository.findByOrderNumber(paymentConfirmationRequestDTO.getOrderNumber())
                .orElseThrow(() -> new CustomBadRequestException("Order not found"));
        order.setOrderStatus(OrderStatus.PLACED);
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        orderRepository.save(order);

        // 6. Return response
        return PaymentConfirmationResponseDTO.builder()
                .success(true)
                .message("Payment confirmation successful")
                .orderNumber(order.getOrderNumber())
                .orderId(order.getOrderId())
                .amount(order.getTotalAmount())
                .paymentStatus("completed")
                .pgPaymentId(payment.getGatewayReference())
                .build();
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
