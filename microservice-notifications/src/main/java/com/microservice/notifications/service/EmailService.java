package com.microservice.notifications.service;


import com.microservice.notifications.dto.OrderEventDto;
import com.microservice.notifications.dto.OrderItemEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOrderEmail(String toEmail, OrderEventDto order) {
        // 1. Construir contenido del mensaje
        String subject = "Detalles de tu compra. Orden #" + order.getId();
        String text = buildEmailBody(order);

        // 2. Crear el mensaje
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);

        // 3. Enviar
        javaMailSender.send(mailMessage);

        log.info("[EmailService] Correo enviado a {} con la orden #{}", toEmail, order.getId());
    }

    private String buildEmailBody(OrderEventDto order) {
        StringBuilder sb = new StringBuilder();
        sb.append("¡Gracias por tu compra!\n\n")
                .append("Orden ID: ").append(order.getId()).append("\n")
                .append("Estado: ").append(order.getStatus()).append("\n")
                .append("Total: $").append(order.getTotal()).append("\n")
                .append("Dirección: ").append(order.getAddress()).append("\n\n")
                .append("Productos:\n");

        if (order.getItems() != null) {
            for (OrderItemEventDto item : order.getItems()) {
                sb.append("- ").append(item.getName())
                        .append(" (x").append(item.getQuantity()).append(") ")
                        .append(" = $").append(item.getPrice()).append("\n");
            }
        }
        sb.append("\n¡Disfruta tu compra!\n");
        return sb.toString();
    }
}