package com.microservice.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotNull
    private String fullName;

    @NotNull @Email
    private String email;

    @NotNull
    private String password;
}
