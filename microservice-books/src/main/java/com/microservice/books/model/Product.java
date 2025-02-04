package com.microservice.books.model;

import com.microservice.books.model.Condition;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@Document(collection = "books")
public class Product {

    @Id
    private String id;
    private Long userId;
    @NotBlank(message = "El título no debe estar vacío")
    @Size(max = 100, message = "El título no debe exceder los 100 caracteres")
    private String title; //Lo llena el usuario input text
    @NotBlank(message = "El autor no debe estar vacío")
    @Size(max = 100, message = "El autor no debe exceder los 100 caracteres")
    private String author; //Lo llena el usuario input text
    @NotBlank(message = "El ISBN no debe estar vacío")
    @Pattern(regexp = "\\d{10}|\\d{13}", message = "El ISBN debe ser de 10 o 13 dígitos")
    private String isbn; //Lo llena el usuario input text
    @NotBlank(message = "El editor no debe estar vacío")
    @Size(max = 100, message = "El editor no debe exceder los 100 caracteres")
    private String publisher; //Lo llena el usuario input text
    @NotBlank(message = "La fecha de publicación no debe estar vacía")
    private String publicationDate; //Lo llena el usuario input date
    @NotNull(message = "La categoría no debe ser nula")
    private Category category; //Lo llena el usuario select
    @NotNull(message = "El género no debe ser nulo")
    private Genre genre; //Lo llena el usuario select
    @NotNull(message = "El número de páginas no debe ser nulo")
    @Min(value = 1, message = "El número de páginas debe ser al menos 1")
    private Integer nPages; //Lo llena el usuario input number
    @NotBlank(message = "La descripción no debe estar vacía")
    @Size(max = 500, message = "La descripción no debe exceder los 500 caracteres")
    private String description; //Lo llena el usuario textarea
    @NotNull(message = "La cantidad en stock no debe ser nula")
    @Min(value = 0, message = "La cantidad en stock no puede ser negativa")
    private Integer stockQuantity; //Lo llena el usuario input number
    @NotNull(message = "El precio de venta no debe ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio de venta debe ser mayor a 0")
    private BigDecimal salePrice; //Lo llena el usuario input number
    private BigDecimal offerPrice; // Calculado automáticamente
    @DecimalMin(value = "0.0", message = "El porcentaje de oferta debe ser al menos 0")
    @DecimalMax(value = "100.0", message = "El porcentaje de oferta no puede exceder el 100")
    private BigDecimal offer; //Lo llena el usuario input number
    private byte[] cover; //Lo llena el usuario input file
    @NotNull(message = "La condición no debe ser nula")
    private Condition condition; //Lo llena el usuario select
    @DecimalMin(value = "0.0", message = "La calificación mínima es 0")
    @DecimalMax(value = "5.0", message = "La calificación máxima es 5")
    private Double rating; // Lo llena el usuario input number
    private Boolean isAvailable; // Calculado automáticamente
    private LocalDate createdAt = LocalDate.now(); // Generar automáticamente la fecha actual
    // Constructor
    public Product() {
        // Generar automáticamente un ID si es null
        if (this.id == null) {
            this.id = UUID.randomUUID().toString(); // Genera un UUID único
        }
        // Calcula automáticamente el offerPrice si salePrice y offer están definidos
        if (this.salePrice != null && this.offer != null) {
            this.setOfferPrice(this.salePrice, this.offer);
        }
        // Calcula automáticamente isAvailable si stockQuantity está definido
        if (this.stockQuantity != null) {
            this.setIsAvailable(this.stockQuantity);
        }
    }
    // Setter personalizado para salePrice
    public void setOfferPrice(BigDecimal salesPrice, BigDecimal offer) {
        if (salesPrice == null || offer == null) {
            throw new IllegalArgumentException("salePrice y offer no deben ser nulos");
        }
        if(offer.equals(BigDecimal.ZERO)){
            this.offerPrice = salesPrice;
            return;
        }
        BigDecimal discount = salePrice.multiply(offer.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
        this.offerPrice = salePrice.subtract(discount);
    }
    // Setter personalizado para isAvailable
    public void setIsAvailable(Integer stockQuantity) {
        this.isAvailable = stockQuantity > 0;
    }

}