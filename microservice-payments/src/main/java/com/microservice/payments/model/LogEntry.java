package com.microservice.payments.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "payment_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payment_transaction_id", nullable = false)
    private PaymentTransaction paymentTransaction;

    @Column(nullable = false)
    private String action;  // Por ejemplo: "PAYMENT_INITIATED", "PAYMENT_COMPLETED", "STATUS_CHANGED"

    @Column(nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String additionalDetails;  // Para almacenar datos JSON o información extra

    private String previousStatus;
    private String newStatus;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}