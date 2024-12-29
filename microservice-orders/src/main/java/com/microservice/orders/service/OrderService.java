package com.microservice.orders.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.orders.client.PaymentClient;
import com.microservice.orders.dto.*;
import com.microservice.orders.model.Order;
import com.microservice.orders.model.OrderItem;
import com.microservice.orders.model.OrderOutbox;
import com.microservice.orders.model.OrderStatus;
import com.microservice.orders.repositories.OrderOutboxRepository;
import com.microservice.orders.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final OrderOutboxRepository outboxRepository;

    @Transactional
    public Order createOrder(CheckoutRequest request) {
        // 1. Calcular total
        double total = 0.0;
        for (CheckoutProductDto p : request.getProducts()) {
            double price = p.getOfferPrice() != null ? p.getOfferPrice() : p.getSalePrice();
            total += price * p.getQuantity();
        }

        // 2. Llamar al microservicio de pagos
        PaymentResponse payment = paymentClient.charge(
                new PaymentRequest(
                        total,
                        request.getPayment().getType(),
                        request.getPayment().getToken(),
                        null  // orderId opcional
                )
        );

        // 3. Crear la entidad Order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotal(total);
        order.setAddress(formatAddress(request.getBilling()));
        if ("APPROVED".equalsIgnoreCase(payment.getStatus())) {
            order.setStatus(OrderStatus.PAID);
        } else {
            order.setStatus(OrderStatus.REJECTED);
        }

        // 4. Crear items
        List<OrderItem> items = new ArrayList<>();
        for (CheckoutProductDto p : request.getProducts()) {
            OrderItem oi = new OrderItem();
            oi.setProductId(p.getId());
            oi.setName(p.getName());
            oi.setImage(p.getImage());
            oi.setPrice((p.getOfferPrice() != null) ? p.getOfferPrice() : p.getSalePrice());
            oi.setQuantity(p.getQuantity());
            oi.setDescription(p.getDescription());
            oi.setOrder(order);
            items.add(oi);
        }
        order.setItems(items);

        // 5. Guardar la Order en la BD
        Order savedOrder = orderRepository.save(order);

        // 6. Mapear la Order a un DTO (sin referencias cíclicas)
        OrderEventDto eventDto = mapOrderToEventDto(savedOrder);

        // 7. Serializar el DTO
        String payload = convertirDtoAJson(eventDto);

        // 8. Registrar en la tabla Outbox
        OrderOutbox outbox = new OrderOutbox();
        outbox.setEventType("ORDER_CREATED");
        outbox.setAggregateType("Order");
        outbox.setAggregateId(savedOrder.getId());
        outbox.setPayload(payload);  // <-- usando el DTO
        outbox.setCreatedAt(LocalDateTime.now());
        outbox.setStatus("PENDING");
        outboxRepository.save(outbox);

        return savedOrder;
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    private String formatAddress(CheckoutAddressDto addr) {
        String line2 = (addr.getLine2() != null) ? addr.getLine2() : "";
        return addr.getLabel() + ", "
                + addr.getLine1() + " "
                + line2 + ", "
                + addr.getCity() + ", "
                + addr.getState() + ", "
                + addr.getCountry() + " - "
                + addr.getZipCode();
    }

    /**
     * Convierte la entidad Order en OrderEventDto.
     */
    private OrderEventDto mapOrderToEventDto(Order order) {
        OrderEventDto dto = new OrderEventDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setAddress(order.getAddress());
        dto.setStatus(order.getStatus().name());
        dto.setTotal(order.getTotal());

        // Convertir los items al DTO
        if (order.getItems() != null) {
            List<OrderItemEventDto> itemDtos = order.getItems().stream().map(item -> {
                OrderItemEventDto iDto = new OrderItemEventDto();
                iDto.setProductId(item.getProductId());
                iDto.setName(item.getName());
                iDto.setImage(item.getImage());
                iDto.setPrice(item.getPrice());
                iDto.setQuantity(item.getQuantity());
                iDto.setDescription(item.getDescription());
                return iDto;
            }).collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        return dto;
    }

    /**
     * Serializa el DTO a JSON sin caer en recursión.
     */
    private String convertirDtoAJson(OrderEventDto eventDto) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(eventDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing OrderEventDto to JSON", e);
        }
    }

}
