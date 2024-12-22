package com.microservice.auth.repositories;

import com.microservice.auth.model.AuthOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<AuthOutbox, Long> { // Cambiado UUID a Long
    List<AuthOutbox> findByProcessedFalse();
}