package com.microservice.books;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.Objects;

@SpringBootApplication
@EnableDiscoveryClient
public class MicroserviceBooksApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		// JWT
		System.setProperty("JWT_SECRET", Objects.requireNonNull(dotenv.get("JWT_SECRET")));
		SpringApplication.run(MicroserviceBooksApplication.class, args);
	}

}
