package com.microservice.notifications.client;

import com.microservice.notifications.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "msvc-users") // Nombre del microservicio de usuarios en Eureka
public interface UsersClient {

    @GetMapping("/users/{userId}/notification")
    UserDto getUserProfile(@PathVariable("userId") Long userId);
}