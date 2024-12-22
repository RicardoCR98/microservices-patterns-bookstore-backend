package com.microservice.payments.controller;

import com.microservice.payments.dto.PaymentRequest;
import com.microservice.payments.dto.PaymentResponse;
import com.microservice.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/charge")
    public ResponseEntity<PaymentResponse> charge(@RequestBody PaymentRequest request) {
        // request.token = "PAYPAL_ORDER_ID_ABC123"
        // request.method = "card" o "paypal"
        // request.amount = ...
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }
}