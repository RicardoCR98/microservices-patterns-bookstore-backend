// OrderService.java
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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final OrderOutboxRepository outboxRepository;

    @CircuitBreaker(name = "msvc-payments", fallbackMethod = "paymentFallback")
    public PaymentResponse callPaymentService(PaymentRequest request) {
        logger.info("Calling payment service with request: {}", request);
        return paymentClient.charge(request);
    }

    public PaymentResponse paymentFallback(PaymentRequest request, Throwable t) {
        logger.error("[CircuitBreaker-Fallback] Payment service unavailable: {}", t.getMessage());
        return new PaymentResponse("REJECTED", "fallback-cb", "FALLBACK_CB");
    }

    @Transactional
    public Order createOrder(CheckoutRequest request) {
        logger.info("Creating order for user ID: {}", request.getUserId());

        double total = 0.0;
        for (CheckoutProductDto p : request.getProducts()) {
            double price = p.getOfferPrice() != null ? p.getOfferPrice() : p.getSalePrice();
            total += price * p.getQuantity();
        }
        logger.debug("Calculated total price: {}", total);

        PaymentResponse payment = callPaymentService(
                new PaymentRequest(
                        total,
                        request.getPayment().getType(),
                        request.getPayment().getToken(),
                        null
                )
        );
        logger.info("Payment response: {}", payment);

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotal(total);
        order.setAddress(formatAddress(request.getBilling()));
        if ("APPROVED".equalsIgnoreCase(payment.getStatus())) {
            order.setStatus(OrderStatus.PAID);
        } else {
            order.setStatus(OrderStatus.REJECTED);
        }
        logger.debug("Order status set to: {}", order.getStatus());

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
        logger.info("Order items set for order ID: {}", order.getId());

        Order savedOrder = orderRepository.save(order);
        logger.info("Order saved successfully with ID: {}", savedOrder.getId());

        OrderEventDto eventDto = mapOrderToEventDto(savedOrder);
        String payload = convertirDtoAJson(eventDto);
        logger.debug("OrderEventDto payload: {}", payload);

        OrderOutbox outbox = new OrderOutbox();
        outbox.setEventType("ORDER_CREATED");
        outbox.setAggregateType("Order");
        outbox.setAggregateId(savedOrder.getId());
        outbox.setPayload(payload);
        outbox.setCreatedAt(LocalDateTime.now());
        outbox.setStatus("PENDING");
        outboxRepository.save(outbox);
        logger.info("Outbox event created for order ID: {}", savedOrder.getId());

        return savedOrder;
    }

    public Order getOrder(Long orderId) {
        logger.info("Fetching order with ID: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Order not found with ID: " + orderId);
                });
    }

    private String formatAddress(CheckoutAddressDto addr) {
        logger.debug("Formatting address for: {}", addr);
        String line2 = (addr.getLine2() != null) ? addr.getLine2() : "";
        return addr.getLabel() + ", "
                + addr.getLine1() + " "
                + line2 + ", "
                + addr.getCity() + ", "
                + addr.getState() + ", "
                + addr.getCountry() + " - "
                + addr.getZipCode();
    }

    private OrderEventDto mapOrderToEventDto(Order order) {
        logger.debug("Mapping Order to OrderEventDto for order ID: {}", order.getId());
        OrderEventDto dto = new OrderEventDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setAddress(order.getAddress());
        dto.setStatus(order.getStatus().name());
        dto.setTotal(order.getTotal());

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

    private String convertirDtoAJson(OrderEventDto eventDto) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(eventDto);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing OrderEventDto to JSON: {}", e.getMessage());
            throw new RuntimeException("Error serializing OrderEventDto to JSON", e);
        }
    }
}