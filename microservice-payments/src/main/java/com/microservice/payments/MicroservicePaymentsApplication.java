package com.microservice.payments;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Objects;

@SpringBootApplication
@EnableDiscoveryClient
public class MicroservicePaymentsApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		// Establecer propiedades del sistema con nombres correctos
		System.setProperty("DB_USERNAME", Objects.requireNonNull(dotenv.get("DB_USERNAME")));
		System.setProperty("DB_PASSWORD", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));

		System.setProperty("paypal.client-id", Objects.requireNonNull(dotenv.get("PAYPAL_CLIENT_ID")));
		System.setProperty("paypal.client-secret", Objects.requireNonNull(dotenv.get("PAYPAL_CLIENT_SECRET")));
		System.out.println("El PAYPAL_CLIENT_ID es: " + dotenv.get("PAYPAL_CLIENT_ID"));
		System.out.println("El PAYPAL_CLIENT_SECRET es: " + dotenv.get("PAYPAL_CLIENT_SECRET"));
		SpringApplication.run(MicroservicePaymentsApplication.class, args);
	}
}
