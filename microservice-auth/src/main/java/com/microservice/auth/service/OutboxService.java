package com.microservice.auth.service;
import com.microservice.auth.model.AuthOutbox;
import com.microservice.auth.repositories.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;

    // Implementa la lógica para enviar eventos al sistema de mensajería
    // y actualizar el estado de los eventos como procesados.

    public void processOutboxEvents() {
        List<AuthOutbox> events = outboxRepository.findByProcessedFalse();
        for (AuthOutbox event : events) {
            try {
                // Aquí puedes procesar el evento, por ejemplo, enviándolo a Kafka, RabbitMQ, etc.
                System.out.println("Processing event: " + event.getEventType() + " with payload: " + event.getPayload());

                // Simula un procesamiento exitoso
                boolean sendSuccess = true;

                if (sendSuccess) {
                    event.setProcessed(true);
                    event.setProcessedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                    outboxRepository.save(event);
                }
            } catch (Exception e) {
                System.err.println("Error processing event: " + e.getMessage());
            }
        }
    }
}