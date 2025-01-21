package com.microservice.auth.service;

import com.microservice.auth.model.LogEntry;
import com.microservice.auth.model.AuthUser;
import com.microservice.auth.repositories.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogEntryService {

    private final LogEntryRepository logEntryRepository;

    public LogEntry createLog(
            AuthUser user,
            String action,
            String description,
            String additionalDetails,
            Long performedByUserId,
            String previousRole,
            String newRole,
            Boolean previousActiveStatus,
            Boolean newActiveStatus
    ) {
        // user JAMÁS debe ser null aquí
        LogEntry logEntry = new LogEntry();
        logEntry.setUser(user);
        logEntry.setAction(action);
        logEntry.setDescription(description);
        logEntry.setAdditionalDetails(additionalDetails);
        logEntry.setPerformedByUserId(performedByUserId);
        logEntry.setPreviousRole(previousRole);
        logEntry.setNewRole(newRole);
        logEntry.setPreviousActiveStatus(previousActiveStatus);
        logEntry.setNewActiveStatus(newActiveStatus);

        return logEntryRepository.save(logEntry);
    }

    public List<LogEntry> getLogsByUserId(Long userId) {
        return logEntryRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    public List<LogEntry> getLogsByPerformedBy(Long adminId) {
        return logEntryRepository.findByPerformedByUserIdOrderByCreatedAtDesc(adminId);
    }
}
