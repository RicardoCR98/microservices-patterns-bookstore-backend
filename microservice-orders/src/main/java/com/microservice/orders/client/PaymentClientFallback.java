package com.microservice.orders.client;

import com.microservice.orders.dto.PaymentRequest;
import com.microservice.orders.dto.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentClientFallback implements PaymentClient{

    @Override
    public PaymentResponse charge(PaymentRequest request) {
        // Lógica en caso de que el microservicio de Payments falle repetidamente
        // y se abra el circuito o no se pueda contactar al servicio.
        System.err.println("[Fallback] PaymentClientFallback: no se pudo procesar el pago.");

        // Puedes devolver un PaymentResponse genérico
        return  new PaymentResponse("REJECTED", "fallback", "FALLBACK_CHARGE");
    }
}
