package com.microservice.payments.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class LogEntryDTO {
    private Long id;
    private Long paymentTransactionId;
    private String action;
    private String description;
    private String additionalDetails;
    private String previousStatus;
    private String newStatus;
    private Instant createdAt;
}