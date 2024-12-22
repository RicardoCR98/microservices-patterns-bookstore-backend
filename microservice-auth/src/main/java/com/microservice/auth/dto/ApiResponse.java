package com.microservice.auth.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success; // Indica si la operación fue exitosa
    private String message;  // Mensaje descriptivo de la operación
    private T data;          // Datos específicos de la operación (puede ser cualquier tipo)
}