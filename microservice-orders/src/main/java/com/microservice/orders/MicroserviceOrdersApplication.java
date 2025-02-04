package com.microservice.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@EnableRabbit
public class MicroserviceOrdersApplication {

	public static void main(String[] args) {
		// Se obtienen y validan las variables de entorno requeridas
		String rabbitMqUser     = requireEnv("RABBITMQ_USER");
		String rabbitMqPassword = requireEnv("RABBITMQ_PASSWORD");
		String jwtSecret        = requireEnv("JWT_SECRET");
		String dbUsername       = requireEnv("DB_USERNAME");
		String dbPassword       = requireEnv("DB_PASSWORD");

		// Se establecen como propiedades del sistema para que Spring Boot pueda inyectarlas (por ejemplo, con @Value)
		System.setProperty("RABBITMQ_USER", rabbitMqUser);
		System.setProperty("RABBITMQ_PASSWORD", rabbitMqPassword);
		System.setProperty("JWT_SECRET", jwtSecret);
		System.setProperty("DB_USERNAME", dbUsername);
		System.setProperty("DB_PASSWORD", dbPassword);

		SpringApplication.run(MicroserviceOrdersApplication.class, args);
	}

	/**
	 * @param key Nombre de la variable de entorno
	 * @return Valor de la variable de entorno
	 * @throws IllegalStateException si la variable de entorno no está definida
	 */
	private static String requireEnv(String key) {
		String value = System.getenv(key);
		if (value == null) {
			throw new IllegalStateException("Falta la variable de entorno requerida: " + key);
		}
		return value;
	}
}
