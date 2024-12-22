package com.microservice.orders.dto;

import lombok.Data;

@Data
public class CheckoutAddressDto {
    private String name;
    private String destination;
    private String building;
    private String street;
    private String city;
    private String state;
    private String country;
    private String post;
    private String phone;
    private Boolean isDefault;
}
