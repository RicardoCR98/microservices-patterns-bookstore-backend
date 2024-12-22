package com.microservice.auth.repositories;

import com.microservice.auth.model.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderRepository extends JpaRepository<OAuthProvider, Long> {
    Optional<OAuthProvider> findByProviderNameAndProviderUserId(String providerName, String providerUserId);
}