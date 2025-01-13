// OrderController.java
package com.microservice.orders.controller;

import com.microservice.orders.dto.CheckoutProductDto;
import com.microservice.orders.dto.CheckoutRequest;
import com.microservice.orders.dto.OrderResponse;
import com.microservice.orders.model.Order;
import com.microservice.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@RequestBody CheckoutRequest request) {
        logger.info("Checkout request received for user ID: {}", request.getUserId());
        Order order = orderService.createOrder(request);

        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus().name());
        response.setTotal(order.getTotal());

        response.setProducts(
                order.getItems().stream().map(oi -> {
                    CheckoutProductDto p = new CheckoutProductDto();
                    p.setId(oi.getProductId());
                    p.setName(oi.getName());
                    p.setImage(oi.getImage());
                    p.setDescription(oi.getDescription());
                    p.setQuantity(oi.getQuantity());
                    p.setOfferPrice(oi.getPrice());
                    p.setSalePrice(oi.getPrice());
                    return p;
                }).collect(Collectors.toList())
        );

        logger.info("Checkout completed successfully for order ID: {}", order.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        logger.info("Fetching order details for order ID: {}", orderId);
        Order order = orderService.getOrder(orderId);

        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus().name());
        response.setTotal(order.getTotal());

        response.setProducts(
                order.getItems().stream().map(oi -> {
                    CheckoutProductDto p = new CheckoutProductDto();
                    p.setId(oi.getProductId());
                    p.setName(oi.getName());
                    p.setImage(oi.getImage());
                    p.setDescription(oi.getDescription());
                    p.setQuantity(oi.getQuantity());
                    p.setOfferPrice(oi.getPrice());
                    p.setSalePrice(oi.getPrice());
                    return p;
                }).collect(Collectors.toList())
        );

        logger.info("Order details fetched successfully for order ID: {}", orderId);
        return ResponseEntity.ok(response);
    }
}
