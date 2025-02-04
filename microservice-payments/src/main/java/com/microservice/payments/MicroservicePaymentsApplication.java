package com.microservice.payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MicroservicePaymentsApplication {

	public static void main(String[] args) {
		// Validamos y obtenemos las variables de entorno requeridas
		String jwtSecret         = requireEnv("JWT_SECRET");
		String dbUsername        = requireEnv("DB_USERNAME");
		String dbPassword        = requireEnv("DB_PASSWORD");
		String paypalClientId    = requireEnv("PAYPAL_CLIENT_ID");
		String paypalClientSecret = requireEnv("PAYPAL_CLIENT_SECRET");

		// Establecemos las propiedades del sistema para que Spring Boot las inyecte
		System.setProperty("JWT_SECRET", jwtSecret);
		System.setProperty("DB_USERNAME", dbUsername);
		System.setProperty("DB_PASSWORD", dbPassword);
		System.setProperty("paypal.client-id", paypalClientId);
		System.setProperty("paypal.client-secret", paypalClientSecret);

		SpringApplication.run(MicroservicePaymentsApplication.class, args);
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
