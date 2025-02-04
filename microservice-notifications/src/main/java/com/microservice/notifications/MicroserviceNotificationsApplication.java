package com.microservice.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MicroserviceNotificationsApplication {

	public static void main(String[] args) {
		// Validamos y obtenemos las variables de entorno requeridas
		String rabbitMqUser     = requireEnv("RABBITMQ_USER");
		String rabbitMqPassword = requireEnv("RABBITMQ_PASSWORD");
		String mailUsername     = requireEnv("MAIL_USERNAME");
		String mailPassword     = requireEnv("MAIL_PASSWORD");

		// Establecemos las propiedades del sistema para que Spring Boot las inyecte
		System.setProperty("RABBITMQ_USER", rabbitMqUser);
		System.setProperty("RABBITMQ_PASSWORD", rabbitMqPassword);
		System.setProperty("MAIL_USERNAME", mailUsername);
		System.setProperty("MAIL_PASSWORD", mailPassword);

		SpringApplication.run(MicroserviceNotificationsApplication.class, args);
	}

	/**
	 * @param key Nombre de la variable de entorno
	 * @return Valor de la variable de entorno
	 */
	private static String requireEnv(String key) {
		String value = System.getenv(key);
		if (value == null) {
			throw new IllegalStateException("Falta la variable de entorno requerida: " + key);
		}
		return value;
	}
}
