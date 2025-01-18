package com.microservice.orders.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "order_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String action;  // "ORDER_CREATED", "PAYMENT_PROCESSED", "STATUS_CHANGED", etc.

    @Column(nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String additionalDetails;  // Detalles en JSON u otra información

    private String previousStatus;
    private String newStatus;

    private Double totalAmount;  // Útil para tracking de montos

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
