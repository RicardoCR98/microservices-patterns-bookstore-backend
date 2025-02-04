package com.microservice.users.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    // id es generado automáticamente
    private String id;

    @NotBlank(message = "El nombre de la dirección es requerido")
    private String label;       // Ej: "Home", "Office", etc.

    @NotBlank(message = "La calle principal es requerida")
    private String line1;       // Calle, número

    // Opcional: calle secundaria (puede dejarse en blanco)
    private String line2;

    @NotBlank(message = "La ciudad es requerida")
    private String city;

    @NotBlank(message = "La provincia es requerido")
    private String state;

    @NotBlank(message = "El país es requerido")
    private String country;

    @NotBlank(message = "El código postal es requerido")
    private String zipCode;

    @NotBlank(message = "El número de teléfono es requerido")
    @Pattern(regexp =  "^[0-9]{10}$", message = "El número de teléfono debe tener 10 dígitos")
    private String phoneNumber;
    private Boolean defaultAddress;
}
