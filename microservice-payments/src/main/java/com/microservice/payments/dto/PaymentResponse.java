package com.microservice.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private String status;        // "APPROVED" or "REJECTED"
    private String transactionId; // ID interno o "captureId"
}