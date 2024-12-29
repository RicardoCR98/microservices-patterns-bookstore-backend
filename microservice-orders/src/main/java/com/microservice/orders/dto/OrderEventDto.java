package com.microservice.orders.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderEventDto {
    private Long id;
    private Long userId;
    private String address;
    private String status;
    private Double total;
    private List<OrderItemEventDto> items; // sin la referencia de vuelta al Order
}