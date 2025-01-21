package com.microservice.orders.client;

import com.microservice.orders.dto.PaymentRequest;
import com.microservice.orders.dto.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentClientFallback implements PaymentClient{
    @Override
    public PaymentResponse charge(PaymentRequest request) {
        System.err.println("[Fallback] PaymentClientFallback: no se pudo procesar el pago.");
        return  new PaymentResponse("REJECTED", "fallback", "FALLBACK_CHARGE");
    }
}
