package com.microservice.orders.repositories;

import com.microservice.orders.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
    List<LogEntry> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
