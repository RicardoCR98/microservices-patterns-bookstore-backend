package com.microservice.users.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@Document(collection = "users")
public class UserProfile {
    @Id
    private String id;
    private Long userId;
    private String email;
    private String fullName;
    // Lista de direcciones
    private List<Address> addresses;

    // Lista de métodos de pago
    private List<PaymentMethod> paymentMethods;

    public UserProfile(){
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString(); // Genera un UUID único
        }
    }
}
