package com.microservice.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotNull
    private String fullName;

    @NotNull @Email
    private String email;

    @NotNull @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
}
