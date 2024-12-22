package com.microservice.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequest {
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