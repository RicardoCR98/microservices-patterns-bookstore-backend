package com.microservice.orders.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_outbox")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType; // Ejemplo: "ORDER_CREATED", "ORDER_UPDATED", "ORDER_DELETED"
    private String aggregateType = "Order"; // Tipo de entidad afectada (si usas un sistema genérico de eventos)
    private Long aggregateId; // ID del pedido relacionado

    @Column(columnDefinition = "TEXT")
    private String payload; // Información del evento en formato JSON (serialización de Order, por ejemplo)

    private LocalDateTime createdAt; // Fecha y hora en que se creó el evento
    private LocalDateTime processedAt; // Fecha y hora en que el evento fue enviado a RabbitMQ (puede ser nulo si aún no se procesó)

    private String status = "PENDING"; // PENDING, PROCESSED, ERROR
}
