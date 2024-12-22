package com.microservice.orders.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private String status;
    private Double total;
    private List<CheckoutProductDto> products;
    // Puede incluir dirección si quieres
}
