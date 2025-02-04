package com.microservice.notifications.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitMQConfig {

    @Value("${EXCHANGE_NAME}")
    private String exchangeName;

    @Value("${QUEUE_NAME}")
    private String queueName;

    @Value("${ROUTING_KEY}")
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

    // Configurar RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    // ContainerFactory, si necesitas concurrencia
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // factory.setConcurrentConsumers(2);
        // factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}
