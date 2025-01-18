package com.microservice.payments.repositories;

import com.microservice.payments.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
    List<LogEntry> findByPaymentTransactionIdOrderByCreatedAtDesc(Long transactionId);
}