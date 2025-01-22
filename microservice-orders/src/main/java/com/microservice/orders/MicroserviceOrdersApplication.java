package com.microservice.orders;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Objects;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@EnableRabbit
public class MicroserviceOrdersApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		//RabbitMQ
		System.setProperty("RABBITMQ_USER", Objects.requireNonNull(dotenv.get("RABBITMQ_USER")));
		System.setProperty("RABBITMQ_PASSWORD", Objects.requireNonNull(dotenv.get("RABBITMQ_PASSWORD")));
		// JWT
		System.setProperty("JWT_SECRET", Objects.requireNonNull(dotenv.get("JWT_SECRET")));
		// Base de datos
		System.setProperty("DB_USERNAME", Objects.requireNonNull(dotenv.get("DB_USERNAME")));
		System.setProperty("DB_PASSWORD", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));

		SpringApplication.run(MicroserviceOrdersApplication.class, args);
	}

}
