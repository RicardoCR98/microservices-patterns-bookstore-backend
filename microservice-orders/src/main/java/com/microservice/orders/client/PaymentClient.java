package com.microservice.orders.client;

import com.microservice.orders.dto.PaymentRequest;
import com.microservice.orders.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "msvc-payments")
public interface PaymentClient {
    @PostMapping("/payment/charge")
    PaymentResponse charge(PaymentRequest request);
}
