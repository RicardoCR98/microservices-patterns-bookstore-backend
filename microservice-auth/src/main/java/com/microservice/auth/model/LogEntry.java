package com.microservice.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "auth_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false) // Apunta a "userId" en AuthUser
    private AuthUser user;

    @Column(nullable = false)
    private String action;  // "USER_REGISTERED", "USER_UPDATED", "USER_DELETED", etc.

    @Column(nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String additionalDetails;

    private Long performedByUserId;  // ID del usuario que realizó la acción (útil para acciones admin)

    private String previousRole;
    private String newRole;

    private Boolean previousActiveStatus;
    private Boolean newActiveStatus;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}