package com.microservice.notifications.dto;

import lombok.Data;

@Data
public class OrderItemEventDto {
    private String productId;
    private String name;
    private String image;
    private Double price;
    private Integer quantity;
    private String description;
}