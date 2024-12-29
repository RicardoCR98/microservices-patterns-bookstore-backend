package com.microservice.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private Double amount;
    private String type; // "card", "paypal"
    private String token;  // => "PAYPAL_ORDER_ID_ABC123"
    private Long orderId;  // un ID interno de tu orden, opcional
}