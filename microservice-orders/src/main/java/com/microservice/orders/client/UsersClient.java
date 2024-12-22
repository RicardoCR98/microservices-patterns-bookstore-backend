package com.microservice.orders.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "msvc-users")
public interface UsersClient {
    @GetMapping("/users/{userId}")
    Map<String,Object> getUserProfile(@PathVariable("userId") Long userId);
}