package com.microservice.orders.dto;

import lombok.Data;

@Data
public class CheckoutProductDto {
    private String id; // productId
    private String name;
    private Double salePrice;
    private Double offerPrice;
    private Integer quantity;
    private String description;
}
