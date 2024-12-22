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
        // Calcular total
        double total = 0.0;
        for (CheckoutProductDto p : request.getProducts()) {
            double price = p.getOfferPrice() != null ? p.getOfferPrice() : p.getSalePrice();
            total += price * p.getQuantity();
        }
        // Ahora en vez de card, usas token:
        PaymentResponse payment = paymentClient.charge(
                new PaymentRequest(total, request.getPayment().getMethod(), request.getPayment().getToken(), /*orderId opcional*/ null)
        );

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus("APPROVED".equalsIgnoreCase(payment.getStatus()) ? OrderStatus.PAID : OrderStatus.REJECTED);
        order.setTotal(total);
        order.setAddress(formatAddress(request.getBilling()));

        List<OrderItem> items = request.getProducts().stream().map(p -> {
            OrderItem oi = new OrderItem();
            oi.setProductId(p.getId());
            oi.setName(p.getName());
            oi.setImage(p.getImage());
            oi.setPrice(p.getOfferPrice() != null ? p.getOfferPrice() : p.getSalePrice());
            oi.setQuantity(p.getQuantity());
            oi.setDescription(p.getDescription());
            oi.setOrder(order);
            return oi;
        }).toList();

        order.setItems(new ArrayList<>(items));
        return orderRepository.save(order);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    private String formatAddress(CheckoutAddressDto addr) {
        // Combinar campos en un string
        return addr.getName() + ", " + addr.getStreet() + " " + (addr.getDestination() != null ? addr.getDestination() : "") + " " +
                (addr.getBuilding() != null ? addr.getBuilding() : "") + ", "
                + addr.getCity() + ", " + addr.getState() + ", " + addr.getCountry() + " - " + addr.getPost();
    }
}