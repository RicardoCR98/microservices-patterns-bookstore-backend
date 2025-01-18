package com.microservice.orders.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productId;
    private String name;
    private Double price;
    private Integer quantity;
    private String description;

    @ManyToOne
    @JoinColumn(name="order_id")
    private Order order;

    public Double getTotal() {
        return price * quantity;
    }
}
