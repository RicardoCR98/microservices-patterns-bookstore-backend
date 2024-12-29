package com.microservice.orders.repositories;

import com.microservice.orders.model.OrderOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderOutboxRepository extends JpaRepository<OrderOutbox, Long> {

    List<OrderOutbox> findByStatus(String status); // Buscar por estado PENDING, PROCESSED, etc.

}
