package com.microservice.orders.service;

import com.microservice.orders.client.PaymentClient;
import com.microservice.orders.dto.*;
import com.microservice.orders.model.Order;
import com.microservice.orders.model.OrderItem;
import com.microservice.orders.model.OrderStatus;
import com.microservice.orders.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;

    public Order createOrder(CheckoutRequest request) {
        // 1. Calcular total
        double total = 0.0;
        for (CheckoutProductDto p : request.getProducts()) {
            double price = p.getOfferPrice() != null ? p.getOfferPrice() : p.getSalePrice();
            total += price * p.getQuantity();
        }
        // 2. Llamar al microservicio de pagos (msvc-payments)
        PaymentResponse payment = paymentClient.charge(
                new PaymentRequest(total, request.getPayment().getType(), request.getPayment().getToken(), /*orderId opcional*/ null)
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

        // 5. Guardar en BD
        return orderRepository.save(order);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    private String formatAddress(CheckoutAddressDto addr) {
        // Combinar campos en un string
        String line2 = (addr.getLine2() != null) ? addr.getLine2() : "";
        return addr.getLabel() + ", "
                + addr.getLine1() + " "
                + line2 + ", "
                + addr.getCity() + ", "
                + addr.getState() + ", "
                + addr.getCountry() + " - "
                + addr.getZipCode();
    }

}