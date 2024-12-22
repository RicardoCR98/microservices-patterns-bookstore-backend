package com.microservice.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private Double amount;
    private String method; // "card", "paypal"
    private String token;  // el token seguro de la pasarela
    private Long orderId;
}
