package com.microservice.users.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    private String id;          // Un UUID generado para identificar la dirección
    private String label;       // "Home", "Office", etc.
    private String line1;       // Calle, número
    private String line2;       // Piso, departamento (opcional)
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String phoneNumber; // Teléfono opcional
    private Boolean defaultAddress; // Indica si es la dirección principal
}
