package com.microservice.users.dto;

import com.microservice.users.model.Address;
import com.microservice.users.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String id;
    private Long userId;
    private String email;
    private String fullName;
    private List<Address> addresses;
    private List<PaymentMethod> paymentMethods;
}