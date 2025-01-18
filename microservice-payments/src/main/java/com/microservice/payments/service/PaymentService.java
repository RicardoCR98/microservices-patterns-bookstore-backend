package com.microservice.payments.service;

import com.microservice.payments.dto.PaymentRequest;
import com.microservice.payments.dto.PaymentResponse;
import com.microservice.payments.model.PaymentStatus;
import com.microservice.payments.model.PaymentTransaction;
import com.microservice.payments.repositories.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentTransactionRepository transactionRepository;
    private final PayPalService payPalService;
    private final LogEntryService logEntryService;

    public PaymentResponse processPayment(PaymentRequest request) {
        logger.info("Processing payment for request: {}", request);

        // 1. Verificar si ya existe la transacción con este payPalOrderId
        Optional<PaymentTransaction> existingTx = transactionRepository.findByPayPalOrderId(request.getToken());
        if (existingTx.isPresent() && existingTx.get().getStatus() == PaymentStatus.APPROVED) {
            logEntryService.createLog(
                    existingTx.get(),
                    "PAYMENT_DUPLICATE",
                    "Duplicate payment attempt detected",
                    "PayPalOrderId: " + request.getToken(),
                    existingTx.get().getStatus().name(),
                    existingTx.get().getStatus().name()
            );
            return new PaymentResponse(existingTx.get().getStatus().name(), existingTx.get().getPayPalCaptureId());
        }

        // 2. Crear nueva transacción con estado inicial PENDING
        PaymentTransaction tx = new PaymentTransaction();
        tx.setAmount(request.getAmount());
        tx.setMethod(request.getType());
        tx.setPayPalOrderId(request.getToken());
        tx.setTransactionId("tx-" + UUID.randomUUID());
        tx.setStatus(PaymentStatus.PENDING);

        // Guardar transacción antes de asociarla al log
        tx = transactionRepository.save(tx);

        // Registrar inicio del proceso de pago
        logEntryService.createLog(
                tx,
                "PAYMENT_INITIATED",
                "Payment process initiated",
                "Amount: " + request.getAmount() + ", Method: " + request.getType(),
                null,
                PaymentStatus.PENDING.name()
        );

        // 3. Procesar pago según el método
        try {
            if ("paypal".equalsIgnoreCase(request.getType())) {
                processPayPalPayment(tx, request.getToken());
            } else if ("card".equalsIgnoreCase(request.getType())) {
                processCardPayment(tx);
            } else {
                processInvalidPaymentMethod(tx, request.getType());
            }
        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage());
            tx.setStatus(PaymentStatus.REJECTED);
            transactionRepository.save(tx);

            logEntryService.createLog(
                    tx,
                    "PAYMENT_PROCESSING_ERROR",
                    "Error during payment processing",
                    "Error: " + e.getMessage(),
                    PaymentStatus.PENDING.name(),
                    PaymentStatus.REJECTED.name()
            );
        }

        // 4. Guardar estado final y registrar log
        transactionRepository.save(tx);

        logEntryService.createLog(
                tx,
                "PAYMENT_COMPLETED",
                "Payment process completed",
                "Final Status: " + tx.getStatus(),
                null,
                tx.getStatus().name()
        );

        // 5. Retornar respuesta final
        return new PaymentResponse(
                tx.getStatus().name(),
                (tx.getPayPalCaptureId() != null) ? tx.getPayPalCaptureId() : tx.getTransactionId()
        );
    }

    private void processPayPalPayment(PaymentTransaction tx, String token) {
        try {
            PayPalService.CaptureResult captureResult = payPalService.captureOrder(token);

            if ("COMPLETED".equalsIgnoreCase(captureResult.status())) {
                tx.setStatus(PaymentStatus.APPROVED);
                tx.setPayPalCaptureId(captureResult.captureId());

                logEntryService.createLog(
                        tx,
                        "PAYPAL_CAPTURE_SUCCESS",
                        "PayPal capture completed successfully",
                        "CaptureId: " + captureResult.captureId(),
                        PaymentStatus.PENDING.name(),
                        PaymentStatus.APPROVED.name()
                );
            } else {
                tx.setStatus(PaymentStatus.REJECTED);

                logEntryService.createLog(
                        tx,
                        "PAYPAL_CAPTURE_FAILED",
                        "PayPal capture returned non-completed status",
                        "Status: " + captureResult.status(),
                        PaymentStatus.PENDING.name(),
                        PaymentStatus.REJECTED.name()
                );
            }
        } catch (Exception e) {
            tx.setStatus(PaymentStatus.REJECTED);

            logEntryService.createLog(
                    tx,
                    "PAYPAL_CAPTURE_ERROR",
                    "Error during PayPal capture",
                    "Error: " + e.getMessage(),
                    PaymentStatus.PENDING.name(),
                    PaymentStatus.REJECTED.name()
            );
        }
    }

    private void processCardPayment(PaymentTransaction tx) {
        // Lógica simulada para procesamiento de tarjetas
        tx.setStatus(PaymentStatus.APPROVED);

        logEntryService.createLog(
                tx,
                "CARD_PAYMENT_PROCESSED",
                "Card payment processed successfully",
                "TransactionId: " + tx.getTransactionId(),
                PaymentStatus.PENDING.name(),
                PaymentStatus.APPROVED.name()
        );
    }

    private void processInvalidPaymentMethod(PaymentTransaction tx, String method) {
        tx.setStatus(PaymentStatus.REJECTED);

        logEntryService.createLog(
                tx,
                "INVALID_PAYMENT_METHOD",
                "Unrecognized payment method",
                "Method: " + method,
                PaymentStatus.PENDING.name(),
                PaymentStatus.REJECTED.name()
        );
    }
}
