package com.microservice.payments.repositories;

import com.microservice.payments.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    PaymentTransaction findByPayPalOrderId(String payPalOrderId);
}