package com.microservice.payments.service;

import com.microservice.payments.dto.PaymentRequest;
import com.microservice.payments.dto.PaymentResponse;
import com.microservice.payments.model.PaymentStatus;
import com.microservice.payments.model.PaymentTransaction;
import com.microservice.payments.repositories.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository transactionRepository;
    private final PayPalService payPalService;

    public PaymentResponse processPayment(PaymentRequest request) {
        // 1. Verificar si ya existe la transacción
        Optional<PaymentTransaction> existingTx = transactionRepository.findByPayPalOrderId(request.getToken());
        if (existingTx.isPresent() && existingTx.get().getStatus() == PaymentStatus.APPROVED) {
            // ya aprobada => regreso
            return new PaymentResponse(existingTx.get().getStatus().name(), existingTx.get().getPayPalCaptureId());
        }

        // 2. Registrar en BD con status=APPROVED, pues en front ya se capturó
        PaymentTransaction tx = new PaymentTransaction();
        tx.setAmount(request.getAmount());
        tx.setMethod(request.getType()); // "paypal"
        tx.setPayPalOrderId(request.getToken());
        tx.setStatus(PaymentStatus.APPROVED);
        tx.setTransactionId("tx-" + UUID.randomUUID());
        transactionRepository.save(tx);

        // 3. Retornar la respuesta
        return new PaymentResponse(tx.getStatus().name(), tx.getPayPalCaptureId());
    }

}