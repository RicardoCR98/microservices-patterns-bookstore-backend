package com.microservice.auth.service;

import com.microservice.auth.controller.AuthController;
import com.microservice.auth.model.AuthUser;
import com.microservice.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;

    /**
     * Se usa en el proceso normal de login (cuando Spring Security
     * recibe username/password). Aquí 'username' = 'email'.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AuthUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found by email: {}", email);
                    return new UsernameNotFoundException("User not found by email: " + email);
                });

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.getIsActive(),
                true,
                true,
                true,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    /**
     * Se usa en el filtro JWT para cargar el usuario por su ID,
     * dado que el token lleva el userId en el 'sub'.
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        AuthUser user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found by ID: {}", userId);
                    return new UsernameNotFoundException("User not found by ID: " + userId);
                });

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.getIsActive(),
                true,
                true,
                true,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
