package com.microservice.users.service;

import com.microservice.users.dto.UserProfileRequest;
import com.microservice.users.exceptions.UserNotFoundException;
import com.microservice.users.model.UserProfile;
import com.microservice.users.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository repository;

    public UserProfile createUserProfile(UserProfileRequest request) {
        UserProfile profile = new UserProfile();
        profile.setUserId(request.getUserId());
        profile.setEmail(request.getEmail());
        profile.setFullName(request.getFullName());
        return repository.save(profile);
    }
    public UserProfile getProfileByUserId(Long userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with userId: " + userId));
    }

    public UserProfile saveProfile(UserProfile profile) {
        return repository.save(profile);
    }
}