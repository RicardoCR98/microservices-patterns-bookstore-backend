package com.microservice.books;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MicroserviceBooksApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroserviceBooksApplication.class, args);
	}

}
