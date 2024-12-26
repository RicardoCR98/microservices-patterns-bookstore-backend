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
    // Nota: La información sensible (número completo de tarjeta) NO se almacena aquí,
    // sino un token proporcionado por un gateway seguro, si fuera el caso.
    private String token; // Token provisto por un servicio de pago externo
}