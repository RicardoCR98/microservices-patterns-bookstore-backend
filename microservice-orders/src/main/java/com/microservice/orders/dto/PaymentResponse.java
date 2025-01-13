package com.microservice.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private String status; // "APPROVED" or "REJECTED"
    private String type; // "card", "paypal"
    private String transactionId;

}
