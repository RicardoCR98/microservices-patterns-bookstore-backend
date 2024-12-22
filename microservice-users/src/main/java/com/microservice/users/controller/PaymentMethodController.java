package com.microservice.users.controller;


import com.microservice.users.dto.PaymentMethodRequest;
import com.microservice.users.dto.PaymentMethodResponse;
import com.microservice.users.model.PaymentMethod;
import com.microservice.users.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {
    private final PaymentMethodService paymentMethodService;

    @PostMapping
    public ResponseEntity<PaymentMethodResponse> addPaymentMethod(
            @PathVariable Long userId,
            @RequestBody PaymentMethodRequest request) {
        PaymentMethod pm = paymentMethodService.addPaymentMethod(userId, request);
        PaymentMethodResponse response = new PaymentMethodResponse(
                pm.getId(),
                pm.getType(),
                pm.getCardHolderName(),
                pm.getCardBrand(),
                pm.getLast4(),
                pm.getExpirationMonth(),
                pm.getExpirationYear(),
                pm.getDefaultMethod()
        );
        return ResponseEntity.ok(response);
    }
}