package com.microservice.books.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reviews")
public class Review {

    @Id
    private String id; // ID de la reseña

    private Long bookId; // ID del libro al que pertenece la reseña

    private String reviewer; // Nombre del revisor

    private String comment; // Comentario de la reseña

    private Double rating; // Calificación (por ejemplo, de 1 a 5)

    private LocalDate reviewDate; // Fecha de la reseña
}