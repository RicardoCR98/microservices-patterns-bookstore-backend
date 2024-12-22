package com.microservice.orders.dto;

import lombok.Data;

@Data
public class CheckoutPaymentDto {
    private String type;
    private String method;
    private String token; // token del metodo de pago
}
