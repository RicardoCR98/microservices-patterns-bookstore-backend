package com.microservice.orders.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.orders.client.PaymentClient;
import com.microservice.orders.dto.*;
import com.microservice.orders.model.*;
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
    private final LogEntryService logEntryService;

    @CircuitBreaker(name = "msvc-payments", fallbackMethod = "paymentFallback")
    public PaymentResponse callPaymentService(PaymentRequest request) {
        logger.info("Calling payment service with request: {}", request);

        if (request.getOrderId() != null) {
            logEntryService.createLog(
                    getOrder(request.getOrderId()),
                    "PAYMENT_SERVICE_CALL",
                    "Initiating payment service call",
                    "Amount: " + request.getAmount() + ", Type: " + request.getType(),
                    null,
                    null,
                    request.getAmount()
            );
        }

        return paymentClient.charge(request);
    }

    public PaymentResponse paymentFallback(PaymentRequest request, Throwable t) {
        logger.error("[CircuitBreaker-Fallback] Payment service unavailable: {}", t.getMessage());

        if (request.getOrderId() != null) {
            logEntryService.createLog(
                    getOrder(request.getOrderId()),
                    "PAYMENT_SERVICE_FALLBACK",
                    "Payment service fallback activated",
                    "Error: " + t.getMessage(),
                    null,
                    "REJECTED",
                    request.getAmount()
            );
        }

        return new PaymentResponse("REJECTED", "fallback-cb", "FALLBACK_CB");
    }

    @Transactional
    public Order createOrder(CheckoutRequest request) {
        logger.info("Creating order for user ID: {}", request.getUserId());

        // Calculate total
        double total = calculateTotal(request.getProducts());
        logger.debug("Calculated total price: {}", total);

        // Create initial order
        Order order = createInitialOrder(request, total);
        Order savedOrder = orderRepository.save(order);

        logEntryService.createLog(
                savedOrder,
                "ORDER_CREATED",
                "Order created successfully",
                "UserId: " + request.getUserId() + ", Total: " + total,
                null,
                OrderStatus.PENDING.name(),
                total
        );

        // Process payment
        PaymentResponse payment = callPaymentService(
                new PaymentRequest(
                        total,
                        request.getPayment().getType(),
                        request.getPayment().getToken(),
                        savedOrder.getId()
                )
        );

        logger.info("Payment response: {}", payment);

        // Update order status based on payment response
        String previousStatus = savedOrder.getStatus().name();
        updateOrderStatus(savedOrder, payment);

        logEntryService.createLog(
                savedOrder,
                "PAYMENT_PROCESSED",
                "Payment processing completed",
                "Payment Status: " + payment.getStatus(),
                previousStatus,
                savedOrder.getStatus().name(),
                total
        );

        // Create outbox event
        createOutboxEvent(savedOrder);

        logEntryService.createLog(
                savedOrder,
                "ORDER_COMPLETED",
                "Order process completed",
                "Final Status: " + savedOrder.getStatus(),
                null,
                savedOrder.getStatus().name(),
                total
        );

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

    private double calculateTotal(List<CheckoutProductDto> products) {
        return products.stream()
                .mapToDouble(p -> {
                    double price = p.getOfferPrice() != null ? p.getOfferPrice() : p.getSalePrice();
                    return price * p.getQuantity();
                }).sum();
    }

    private Order createInitialOrder(CheckoutRequest request, double total) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotal(total);
        order.setAddress(formatAddress(request.getBilling()));
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> items = request.getProducts().stream().map(p -> {
            OrderItem oi = new OrderItem();
            oi.setProductId(p.getId());
            oi.setName(p.getName());
            oi.setImage(p.getImage());
            oi.setPrice((p.getOfferPrice() != null) ? p.getOfferPrice() : p.getSalePrice());
            oi.setQuantity(p.getQuantity());
            oi.setDescription(p.getDescription());
            oi.setOrder(order);
            return oi;
        }).collect(Collectors.toList());
        order.setItems(items);

        return order;
    }

    private void updateOrderStatus(Order order, PaymentResponse payment) {
        if ("APPROVED".equalsIgnoreCase(payment.getStatus())) {
            order.setStatus(OrderStatus.PAID);
        } else {
            order.setStatus(OrderStatus.REJECTED);
        }
        orderRepository.save(order);
    }

    private void createOutboxEvent(Order order) {
        OrderEventDto eventDto = mapOrderToEventDto(order);
        String payload = convertirDtoAJson(eventDto);

        OrderOutbox outbox = new OrderOutbox();
        outbox.setEventType("ORDER_CREATED");
        outbox.setAggregateType("Order");
        outbox.setAggregateId(order.getId());
        outbox.setPayload(payload);
        outbox.setCreatedAt(LocalDateTime.now());
        outbox.setStatus("PENDING");
        outboxRepository.save(outbox);
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

    private OrderEventDto mapOrderToEventDto(Order order) {
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
