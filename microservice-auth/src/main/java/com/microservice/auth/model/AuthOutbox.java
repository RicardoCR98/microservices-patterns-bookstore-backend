package com.microservice.auth.model;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.sql.Timestamp;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="auth_outbox")
public class AuthOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Cambiado a Long con generación automática
    private Long eventId;

    private Long aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Type(JsonType.class)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(nullable = false)
    private Timestamp occurredAt = new Timestamp(System.currentTimeMillis());

    @Column(nullable = false)
    private Boolean processed = true;

    private Timestamp processedAt = new Timestamp(System.currentTimeMillis());;
}
