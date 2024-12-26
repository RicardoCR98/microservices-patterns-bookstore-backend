package com.microservice.payments.repositories;

import com.microservice.payments.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    // Para buscar transacción por payPalOrderId
    Optional<PaymentTransaction> findByPayPalOrderId(String payPalOrderId);
}