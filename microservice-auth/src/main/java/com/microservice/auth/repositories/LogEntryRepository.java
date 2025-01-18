package com.microservice.auth.repositories;

import com.microservice.auth.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
    List<LogEntry> findByUser_UserIdOrderByCreatedAtDesc(Long userId); // Usa "user.userId" para referenciar correctamente
    List<LogEntry> findByPerformedByUserIdOrderByCreatedAtDesc(Long adminId);
}
