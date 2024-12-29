package com.microservice.notifications.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.notifications.client.UsersClient;
import com.microservice.notifications.dto.OrderEventDto;
import com.microservice.notifications.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final EmailService emailService;
    private final UsersClient usersClient;
    private final ObjectMapper objectMapper; // inyectado automáticamente si lo declaras como bean o usas @Bean

    /**
     * Escucha los mensajes de la cola 'orders-queue'
     * y procesa el evento "OrderEventDto"
     */
    @RabbitListener(queues = "${app.rabbitmq.queue:orders-queue}")
    @RabbitHandler
    public void receiveMessage(String message) {
        try {
            // 1. Deserializar el JSON a OrderEventDto
            OrderEventDto orderEvent = objectMapper.readValue(message, OrderEventDto.class);

            // 2. Obtener los datos del usuario
            UserDto user = usersClient.getUserProfile(orderEvent.getUserId());
            if (user == null) {
                log.error("[NotificationListener] No se pudo obtener el usuario con ID={}", orderEvent.getUserId());
                return;
            }

            // 3. Enviar correo
            emailService.sendOrderEmail(user.getEmail(), orderEvent);

            log.info("[NotificationListener] Email enviado a {} por la orden #{}",
                    user.getEmail(), orderEvent.getId());

        } catch (Exception e) {
            log.error("[NotificationListener] Error procesando mensaje. Mensaje={}, error={}",
                    message, e.getMessage());
        }
    }
}
