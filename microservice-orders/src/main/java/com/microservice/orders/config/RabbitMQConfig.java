package com.microservice.orders.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange:orders-exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue:orders-queue}")
    private String queueName;

    @Value("${app.rabbitmq.routingKey:orders.created}")
    private String routingKey;

    // 1. Declarar Exchange
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    // 2. Declarar Queue
    @Bean
    public Queue orderQueue() {
        return QueueBuilder
                .durable(queueName)
                .build();
    }

    // 3. Binding entre la Queue y el Exchange
    @Bean
    public Binding orderBinding() {
        return BindingBuilder
                .bind(orderQueue())
                .to(orderExchange())
                .with(routingKey);
    }

    // (Opcional) Configurar RabbitTemplate si deseas personalizar la conexión
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    // (Opcional) Listener Container Factory si necesitas configurar concurrencia, etc.
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // Configuraciones adicionales (concurrency, etc.)
        return factory;
    }
}