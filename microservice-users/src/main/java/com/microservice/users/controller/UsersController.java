package com.microservice.users.controller;

import com.microservice.users.dto.UserProfileRequest;
import com.microservice.users.dto.UserProfileResponse;
import com.microservice.users.model.UserProfile;
import com.microservice.users.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserProfileService service;

    @PostMapping
    public ResponseEntity<UserProfileResponse> createUser(@RequestBody UserProfileRequest request) {
        UserProfile profile = service.createUserProfile(request);
        UserProfileResponse response = new UserProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getEmail(),
                profile.getFullName(),
                profile.getAddresses(),
                profile.getPaymentMethods()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        UserProfile profile = service.getProfileByUserId(userId);
        UserProfileResponse response = new UserProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getEmail(),
                profile.getFullName(),
                profile.getAddresses(),
                profile.getPaymentMethods()
        );
        return ResponseEntity.ok(response);
    }

}