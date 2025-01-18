
// 3. Service
package com.microservice.payments.service;

import com.microservice.payments.model.LogEntry;
import com.microservice.payments.model.PaymentTransaction;
import com.microservice.payments.repositories.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogEntryService {

    private final LogEntryRepository logEntryRepository;

    public LogEntry createLog(
            PaymentTransaction transaction,
            String action,
            String description,
            String additionalDetails,
            String previousStatus,
            String newStatus) {

        LogEntry logEntry = new LogEntry();
        logEntry.setPaymentTransaction(transaction);
        logEntry.setAction(action);
        logEntry.setDescription(description);
        logEntry.setAdditionalDetails(additionalDetails);
        logEntry.setPreviousStatus(previousStatus);
        logEntry.setNewStatus(newStatus);

        return logEntryRepository.save(logEntry);
    }

    public List<LogEntry> getLogsByTransactionId(Long transactionId) {
        return logEntryRepository.findByPaymentTransactionIdOrderByCreatedAtDesc(transactionId);
    }
}