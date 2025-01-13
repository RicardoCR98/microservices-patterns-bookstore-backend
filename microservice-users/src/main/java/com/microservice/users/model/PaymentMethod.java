package com.microservice.users.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethod {
    private String id;               // UUID
    private String type;             // "CREDIT_CARD", "PAYPAL", etc.
    private String cardHolderName;
    private String cardNumber;
    private String cardBrand;        // "VISA", "MASTERCARD"
    private String last4;            // Últimos 4 dígitos de la tarjeta
    private String expirationMonth;
    private String expirationYear;
    private String token; // Token provisto por un servicio de pago externo
}