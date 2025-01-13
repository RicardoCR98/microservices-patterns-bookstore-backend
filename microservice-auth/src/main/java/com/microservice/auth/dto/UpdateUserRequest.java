package com.microservice.auth.dto;

import lombok.Data;

/**
 * DTO para actualizar únicamente los campos permitidos:
 *  - isActive
 *  - fullName
 *  - email
 */
@Data
public class UpdateUserRequest {
    private Boolean isActive;
    private String fullName;
    private String email;
}
