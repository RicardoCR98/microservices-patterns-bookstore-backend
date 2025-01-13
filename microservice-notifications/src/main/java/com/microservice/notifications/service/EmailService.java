package com.microservice.notifications.service;


import com.microservice.notifications.dto.OrderEventDto;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Envía el correo con los detalles de la orden, en formato HTML.
     * @param toEmail destino del correo
     * @param order DTO con la información de la orden
     */
    public void sendOrderEmail(String toEmail, OrderEventDto order) {
        try {
            // 1. Crear el contexto de Thymeleaf
            Context context = new Context();
            context.setVariable("order", order);

            // 2. Generar el contenido HTML a partir del template
            String htmlContent = templateEngine.process("order-email", context);

            // 3. Configurar el MimeMessage como HTML
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // Usamos helper para armar el mensaje
            // (import org.springframework.mail.javamail.MimeMessageHelper)
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Detalles de tu compra. Orden #" + order.getId());
            helper.setText(htmlContent, true); // 'true' indica que es HTML

            // 4. Enviar
            javaMailSender.send(mimeMessage);

            log.info("[EmailService] Correo enviado a {} con la orden #{}", toEmail, order.getId());
        } catch (Exception e) {
            log.error("[EmailService] Error al enviar el correo: {}", e.getMessage());
        }
    }
}