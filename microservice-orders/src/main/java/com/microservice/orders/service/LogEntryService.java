package com.microservice.orders.service;

import com.microservice.orders.model.LogEntry;
import com.microservice.orders.model.Order;
import com.microservice.orders.repositories.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogEntryService {

    private final LogEntryRepository logEntryRepository;

    public LogEntry createLog(
            Order order,
            String action,
            String description,
            String additionalDetails,
            String previousStatus,
            String newStatus,
            Double totalAmount) {

        LogEntry logEntry = new LogEntry();
        logEntry.setOrder(order);
        logEntry.setAction(action);
        logEntry.setDescription(description);
        logEntry.setAdditionalDetails(additionalDetails);
        logEntry.setPreviousStatus(previousStatus);
        logEntry.setNewStatus(newStatus);
        logEntry.setTotalAmount(totalAmount);

        return logEntryRepository.save(logEntry);
    }

    public List<LogEntry> getLogsByOrderId(Long orderId) {
        return logEntryRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }
}