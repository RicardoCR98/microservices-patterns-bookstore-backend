package com.microservice.orders.controller;

import com.microservice.orders.dto.CheckoutProductDto;
import com.microservice.orders.dto.CheckoutRequest;
import com.microservice.orders.dto.OrderResponse;
import com.microservice.orders.model.Order;
import com.microservice.orders.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<OrderResponse> checkout(@RequestBody CheckoutRequest request, HttpServletRequest httpRequest) {
        // Obtener el userId desde el token JWT
        String authenticatedUserId = httpRequest.getUserPrincipal().getName();
        logger.info("Checkout request received for authenticated user ID: {}", authenticatedUserId);

        // Establecer el userId del token JWT en el objeto request
        request.setUserId(Long.parseLong(authenticatedUserId));

        // Crear la orden
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
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId, HttpServletRequest httpRequest) {
        String authenticatedUserId = httpRequest.getUserPrincipal().getName();
        logger.info("Fetching order details for order ID: {} by authenticated user ID: {}", orderId, authenticatedUserId);

        Order order = orderService.getOrder(orderId);

        // Validar que el userId de la orden no sea null y que coincida con el usuario autenticado
        if (order.getUserId() == null || !authenticatedUserId.equals(order.getUserId().toString())) {
            logger.warn("Access denied for order ID: {}. Authenticated user ID: {}", orderId, authenticatedUserId);
            return ResponseEntity.status(403).build();
        }

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
