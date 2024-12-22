package com.microservice.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.auth.client.UsersClient;
import com.microservice.auth.model.AuthOutbox;
import com.microservice.auth.model.AuthUser;
import com.microservice.auth.model.UserRole;
import com.microservice.auth.repositories.OutboxRepository;
import com.microservice.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersClient usersClient;

    public AuthUser registerUser(String fullName, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        AuthUser newUser = new AuthUser();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setRole(UserRole.USER);
        newUser.setIsActive(true);

        AuthUser savedUser = userRepository.save(newUser);

        // Cada que se registre un usuario Llamamos a msvc-users para crear el perfil
        usersClient.createUserProfile(new com.microservice.auth.dto.UserProfileRequest(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getFullName()
        ));

        // Crear evento Outbox
        AuthOutbox event = new AuthOutbox();
        event.setAggregateId(savedUser.getUserId());
        event.setEventType("USER_REGISTERED");

        // Crear el payload como un mapa
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", savedUser.getUserId());
        payload.put("email", savedUser.getEmail());
        event.setPayload(payload);

        outboxRepository.save(event);
        return savedUser;
    }
    public AuthUser findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

}