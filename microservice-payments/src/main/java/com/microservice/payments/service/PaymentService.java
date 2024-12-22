package com.microservice.payments.service;

import com.microservice.payments.dto.PaymentRequest;
import com.microservice.payments.dto.PaymentResponse;
import com.microservice.payments.model.PaymentStatus;
import com.microservice.payments.model.PaymentTransaction;
import com.microservice.payments.repositories.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository transactionRepository;
    private final PayPalService payPalService;

    /**
     * Procesa el pago capturando la orden en PayPal a partir de request.token (payPalOrderId).
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        // 1. Guardar transacción en BD (PENDING)
        PaymentTransaction tx = new PaymentTransaction();
        tx.setAmount(request.getAmount());
        tx.setMethod(request.getMethod());
        tx.setPayPalOrderId(request.getToken()); // token = payPalOrderId
        tx.setStatus(PaymentStatus.PENDING);

        // Generar un transactionId interno
        String internalTxId = "tx-" + UUID.randomUUID();
        tx.setTransactionId(internalTxId);

        tx = transactionRepository.save(tx);

        try {
            // 2. Capturar la orden en PayPal
            PayPalService.CaptureResult captureResult = payPalService.captureOrder(tx.getPayPalOrderId());

            // 3. Evaluar la respuesta
            if ("COMPLETED".equalsIgnoreCase(captureResult.status())) {
                tx.setStatus(PaymentStatus.APPROVED);
                tx.setPayPalCaptureId(captureResult.captureId());
            } else {
                // A veces PayPal puede devolver "SAVED", "APPROVED", "VOIDED", etc.
                // Revisa la doc: https://developer.paypal.com/docs/api/orders/v2/
                tx.setStatus(PaymentStatus.REJECTED);
            }
            transactionRepository.save(tx);

            // 4. Devolver PaymentResponse
            return new PaymentResponse(tx.getStatus().name(), tx.getPayPalCaptureId());

        } catch (Exception e) {
            // Si falla la captura, marcamos la transacción como REJECTED
            tx.setStatus(PaymentStatus.REJECTED);
            transactionRepository.save(tx);

            throw new RuntimeException("Error en capturar orden PayPal: " + e.getMessage(), e);
        }
    }
}