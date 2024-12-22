package com.microservice.orders.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {
    private Long userId;
    private List<CheckoutProductDto> products;
    private CheckoutAddressDto billing;
    private CheckoutPaymentDto payment;
}
