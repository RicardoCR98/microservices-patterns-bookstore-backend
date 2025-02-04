package com.microservice.auth;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MicroserviceAuthApplication {

	@Value("${DB_USERNAME}")
	private String dbUsername;

	@Value("${DB_PASSWORD}")
	private String dbPassword;

	@Value("${JWT_SECRET}")
	private String jwtSecret;

	@PostConstruct
	public void validateEnvVariables() {
		if (dbUsername == null || dbPassword == null || jwtSecret == null) {
			throw new IllegalStateException("Faltan variables de entorno requeridas para msvc-auth");
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(MicroserviceAuthApplication.class, args);
	}
}
