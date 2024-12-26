package com.microservice.orders.dto;

import lombok.Data;

@Data
public class CheckoutAddressDto {
    private String label;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String phoneNumber;
    private Boolean defaultAddress;
}
