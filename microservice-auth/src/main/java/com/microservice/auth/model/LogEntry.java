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

    /**
     * Relación obligatoria (NOT NULL) con un usuario real.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    private AuthUser user;

    @Column(nullable = false)
    private String action; // "USER_REGISTERED", "USER_UPDATED", etc.

    @Column(nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String additionalDetails;

    private Long performedByUserId;
    private String previousRole;
    private String newRole;
    private Boolean previousActiveStatus;
    private Boolean newActiveStatus;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
