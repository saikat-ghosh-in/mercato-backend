package com.mercato.Repository;

import com.mercato.Entity.fulfillment.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>{

    Optional<Payment> findByGatewayReference(String gatewayReference);
}