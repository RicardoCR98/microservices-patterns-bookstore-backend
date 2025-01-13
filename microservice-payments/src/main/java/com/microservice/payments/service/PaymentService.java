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
        // 1. Verificar si ya existe la transacción con este payPalOrderId (token)
        Optional<PaymentTransaction> existingTx = transactionRepository.findByPayPalOrderId(request.getToken());
        if (existingTx.isPresent() && existingTx.get().getStatus() == PaymentStatus.APPROVED) {
            // Ya estaba aprobada => regreso
            return new PaymentResponse(existingTx.get().getStatus().name(), existingTx.get().getPayPalCaptureId());
        }

        // 2. Crear una nueva transacción en BD (aún no sabemos si estará APPROVED o REJECTED)
        PaymentTransaction tx = new PaymentTransaction();
        tx.setAmount(request.getAmount());
        tx.setMethod(request.getType());           // "paypal", "card", etc.
        tx.setPayPalOrderId(request.getToken());   // "PAYPAL_ORDER_ID_ABC123"
        tx.setTransactionId("tx-" + UUID.randomUUID());
        tx.setStatus(PaymentStatus.PENDING);       // Empezamos en PENDING

        // 3. Si es PayPal, llamamos a la captura real en el backend
        if ("paypal".equalsIgnoreCase(request.getType())) {
            try {
                // Invocamos al servicio de PayPal, donde aplica el @CircuitBreaker
                PayPalService.CaptureResult captureResult = payPalService.captureOrder(request.getToken());

                // PayPal retornó algo => Revisamos si el status es COMPLETED
                if ("COMPLETED".equalsIgnoreCase(captureResult.status())) {
                    tx.setStatus(PaymentStatus.APPROVED);
                    tx.setPayPalCaptureId(captureResult.captureId());
                } else {
                    tx.setStatus(PaymentStatus.REJECTED);
                }
            } catch (Exception e) {
                // Ocurrió un error llamando a PayPal: se activó fallback o no hay conexión
                System.err.println("Error al capturar la orden en PayPal: " + e.getMessage());
                tx.setStatus(PaymentStatus.REJECTED);
            }
        } else if ("card".equalsIgnoreCase(request.getType())) {
            // Suponiendo que 'card' no requiere llamar a un servicio externo
            // (o ya se hizo la transacción con la pasarela).
            // Lo marcamos APROBADO si tu lógica así lo decide:
            tx.setStatus(PaymentStatus.APPROVED);
            // Si tuvieras otra lógica, la pones acá.
        } else {
            // Tipo no reconocido => RECHAZAR o manejarlo
            tx.setStatus(PaymentStatus.REJECTED);
        }

        // 4. Guardar en BD con el status definido tras la lógica anterior
        transactionRepository.save(tx);

        // 5. Retornar la respuesta final
        return new PaymentResponse(
                tx.getStatus().name(),
                (tx.getPayPalCaptureId() != null) ? tx.getPayPalCaptureId() : tx.getTransactionId()
        );
    }

}