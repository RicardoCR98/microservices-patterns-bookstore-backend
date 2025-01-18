package com.microservice.notifications;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.Objects;

@SpringBootApplication
//@EnableRabbit
@EnableFeignClients
@EnableDiscoveryClient
public class MicroserviceNotificationsApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		// Email
		System.setProperty("MAIL_USERNAME", Objects.requireNonNull(dotenv.get("MAIL_USERNAME")));
		System.setProperty("MAIL_PASSWORD", Objects.requireNonNull(dotenv.get("MAIL_PASSWORD")));
		SpringApplication.run(MicroserviceNotificationsApplication.class, args);
	}

}
