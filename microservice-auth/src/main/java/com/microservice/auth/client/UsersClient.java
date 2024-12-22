package com.microservice.auth.client;

import com.microservice.auth.dto.UserProfileRequest;
import com.microservice.auth.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "msvc-users")
public interface UsersClient {

    @PostMapping("/users")
    UserProfileResponse createUserProfile(UserProfileRequest request);
}