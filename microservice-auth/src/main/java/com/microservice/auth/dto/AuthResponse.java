package com.microservice.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private Long id;            // ID del usuario
    private String name;        // Nombre completo del usuario
    private String token;       // Token JWT
    private String role;        // Rol del usuario
    private Long expirationDate; // Fecha de expiración del token en timestamp
    private boolean isActive;   // Indica si el usuario está activo
}