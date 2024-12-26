package com.microservice.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodRequest {
    private String typeCard;
    private String cardHolderName;
    private String cardNumber;
    private String last4;
    private String expirationMonth;
    private String expirationYear;
}