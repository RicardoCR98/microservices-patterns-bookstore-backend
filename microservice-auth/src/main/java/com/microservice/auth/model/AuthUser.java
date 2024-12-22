package com.microservice.auth.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name="auth_user")
public class AuthUser {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable=false)
    private String fullName;

    @Column(unique=true, nullable=false)
    private String email;

    @Column(nullable=false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private UserRole role = UserRole.USER;

    @Column(nullable=false)
    private Boolean isActive = true;

    @Column(nullable=false, updatable=false)
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Column(nullable=false)
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

}
