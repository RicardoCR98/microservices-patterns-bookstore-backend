package com.microservice.payments.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "payment_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;
    private String method;       // "card", "paypal", etc.
    private String payPalOrderId;  // Por ejemplo: "PAYPAL_ORDER_ID_ABC123"
    private String payPalCaptureId; // El ID de la captura tras aprobación
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionId; // Un ID interno si quieres
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}