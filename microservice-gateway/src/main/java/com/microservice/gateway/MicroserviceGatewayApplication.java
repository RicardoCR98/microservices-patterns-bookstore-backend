package com.microservice.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MicroserviceGatewayApplication {

	public static void main(String[] args) {
		String jwtSecret = System.getenv("JWT_SECRET");
		if (jwtSecret == null) {
			throw new IllegalStateException("JWT_SECRET no está definido en el entorno");
		}
		System.setProperty("JWT_SECRET", jwtSecret);

		SpringApplication.run(MicroserviceGatewayApplication.class, args);
	}
}
