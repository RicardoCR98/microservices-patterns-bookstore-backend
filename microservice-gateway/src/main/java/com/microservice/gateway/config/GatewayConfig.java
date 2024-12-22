package com.microservice.gateway.config;


import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("auth_route", r -> r.path("/auth/**")
//                        .uri("lb://msvc-auth")) // Poner nombre registrado en Eureka
//
//                .route("books_route", r -> r.path("/books/**")
//                        .uri("lb://msvc-books"))
//
//                .build();
//    }
}