package com.microservice.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @GetMapping("/fallback/auth")
    public Mono<ResponseEntity<String>> authFallback() {
        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Auth Service está temporalmente no disponible. Intenta más tarde.")
        );
    }

    @GetMapping("/fallback/auth-admin")
    public Mono<ResponseEntity<String>> authAdminFallback() {
        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Auth Service (admin) está temporalmente no disponible. Intenta más tarde.")
        );
    }

    @GetMapping("/fallback/books")
    public Mono<ResponseEntity<String>> booksFallback() {
        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Book Service está temporalmente no disponible. Intenta más tarde.")
        );
    }

    @GetMapping("/fallback/users")
    public Mono<ResponseEntity<String>> usersFallback() {
        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("User Service está temporalmente no disponible. Intenta más tarde.")
        );
    }

    @GetMapping("/fallback/orders")
    public Mono<ResponseEntity<String>> ordersFallback() {
        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Order Service está temporalmente no disponible. Intenta más tarde.")
        );
    }

    @GetMapping("/fallback/payments")
    public Mono<ResponseEntity<String>> paymentsFallback() {
        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Payment Service está temporalmente no disponible. Intenta más tarde.")
        );
    }
}
