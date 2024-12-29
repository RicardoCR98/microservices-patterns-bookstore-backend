package com.microservice.orders.service;

import com.microservice.orders.model.OrderOutbox;
import com.microservice.orders.repositories.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OrderOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:orders-exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routingKey:orders.created}")
    private String routingKey;

    /**
     * Tarea programada para leer eventos "PENDING" y enviarlos a RabbitMQ
     */
    @Scheduled(fixedDelay = 10000) // cada 10 segundos
    public void processPendingOutboxMessages() {
        List<OrderOutbox> pendingEvents = outboxRepository.findByStatus("PENDING");

        for (OrderOutbox event : pendingEvents) {
            try {
                // Publicar el payload en RabbitMQ
                rabbitTemplate.convertAndSend(exchange, routingKey, event.getPayload());

                // Marcar evento como PROCESSED
                event.setStatus("PROCESSED");
                event.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(event);

                log.info("[OutboxProcessor] Publicado evento ID={} con aggregateId={} a RabbitMQ",
                        event.getId(), event.getAggregateId());

            } catch (Exception e) {
                // Manejo de error
                log.error("[OutboxProcessor] Error publicando evento ID={} : {}", event.getId(), e.getMessage());
                event.setStatus("ERROR");
                outboxRepository.save(event);
            }
        }
    }
}
