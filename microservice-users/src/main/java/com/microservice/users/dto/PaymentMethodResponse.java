package com.microservice.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodResponse {
    private String id;
    private String type;
    private String cardHolderName;
    private String cardBrand;
    private String last4;
    private String expirationMonth;
    private String expirationYear;
    private Boolean defaultMethod;
}