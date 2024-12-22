package com.microservice.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="oauth_provider", uniqueConstraints=@UniqueConstraint(columnNames={"provider_name","provider_user_id"}))
public class OAuthProvider {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long providerId;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private AuthUser user;

    @Column(nullable=false)
    private String providerName; // e.g. "google", "facebook"

    @Column(nullable=false)
    private String providerUserId;
}
