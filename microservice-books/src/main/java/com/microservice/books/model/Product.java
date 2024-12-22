package com.microservice.books.model;

import com.microservice.books.model.Condition;
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
    private String id; // Generar automáticamente un UUID único

    private String title; //Lo llena el usuario input text
    private String author; //Lo llena el usuario input text
    private String isbn; //Lo llena el usuario input text
    private String publisher; //Lo llena el usuario input text
    private LocalDate publicationDate; //Lo llena el usuario input date
    private Category category; //Lo llena el usuario select
    private Genre genre; //Lo llena el usuario select
    private Integer nPages; //Lo llena el usuario input number
    private String description; //Lo llena el usuario textarea
    private Integer stockQuantity; //Lo llena el usuario input number
    private BigDecimal salePrice; //Lo llena el usuario input number
    private BigDecimal offerPrice; // Calculado automáticamente
    private BigDecimal offer; //Lo llena el usuario input number
    private String cover; //Lo llena el usuario input file
    private Condition condition; //Lo llena el usuario select
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