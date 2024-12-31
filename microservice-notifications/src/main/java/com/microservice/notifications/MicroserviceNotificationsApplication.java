package com.microservice.notifications;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
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
		// Configuración de propiedades del sistema
		System.out.println("El env es: " + dotenv.get("EMAIL_USERNAME") + " y la contraseña es:" + dotenv.get("EMAIL_PASSWORD"));

		// Email
		System.setProperty("EMAIL_USERNAME", Objects.requireNonNull(dotenv.get("EMAIL_USERNAME")));
		System.setProperty("EMAIL_PASSWORD", Objects.requireNonNull(dotenv.get("EMAIL_PASSWORD")));
		SpringApplication.run(MicroserviceNotificationsApplication.class, args);
	}

}
