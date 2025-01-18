package com.microservice.auth;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.Objects;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MicroserviceAuthApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		// Base de datos
		System.setProperty("DB_USERNAME", Objects.requireNonNull(dotenv.get("DB_USERNAME")));
		System.setProperty("DB_PASSWORD", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));

		// JWT
		System.setProperty("JWT_SECRET", Objects.requireNonNull(dotenv.get("JWT_SECRET")));

		// OAuth2 Google
		System.setProperty("GOOGLE_CLIENT_ID", Objects.requireNonNull(dotenv.get("GOOGLE_CLIENT_ID")));
		System.setProperty("GOOGLE_CLIENT_SECRET", Objects.requireNonNull(dotenv.get("GOOGLE_CLIENT_SECRET")));
		System.setProperty("GOOGLE_REDIRECT_URI", Objects.requireNonNull(dotenv.get("GOOGLE_REDIRECT_URI")));

		// OAuth2 Facebook
		System.setProperty("FACEBOOK_CLIENT_ID", Objects.requireNonNull(dotenv.get("FACEBOOK_CLIENT_ID")));
		System.setProperty("FACEBOOK_CLIENT_SECRET", Objects.requireNonNull(dotenv.get("FACEBOOK_CLIENT_SECRET")));
		System.setProperty("FACEBOOK_REDIRECT_URI", Objects.requireNonNull(dotenv.get("FACEBOOK_REDIRECT_URI")));

		SpringApplication.run(MicroserviceAuthApplication.class, args);
	}
}
