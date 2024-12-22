package com.microservice.auth.controller;

import com.microservice.auth.config.JwtUtil;
import com.microservice.auth.dto.ApiResponse;
import com.microservice.auth.dto.AuthResponse;
import com.microservice.auth.dto.LoginRequest;
import com.microservice.auth.dto.RegisterRequest;
import com.microservice.auth.model.AuthUser;
import com.microservice.auth.service.AuthService;
import com.microservice.auth.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationConfiguration authConfig;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthUser newUser = authService.registerUser(request.getFullName(), request.getEmail(), request.getPassword());

            // Respuesta exitosa
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "User registered successfully", new AuthResponse(
                            newUser.getUserId(),
                            newUser.getFullName(),
                            null, // No se genera token en el registro
                            newUser.getRole().name(),
                            null // No se genera fecha de expiración
                    ))
            );
        } catch (IllegalArgumentException e) {
            // Respuesta de error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, e.getMessage(), null)
            );
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Obtiene el AuthenticationManager desde la configuración
            AuthenticationManager authManager = authConfig.getAuthenticationManager();

            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(auth);

            // Obtén los detalles del usuario autenticado
            AuthUser authUser = authService.findUserByEmail(request.getEmail());
            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            // Genera el token y calcula la fecha de expiración
            String token = jwtUtil.generateToken(userDetails);
            Long expirationDate = jwtUtil.getExpirationDateFromToken(token).getTime();

            // Construir el objeto "data"
            AuthResponse data = new AuthResponse(
                    authUser.getUserId(),
                    authUser.getFullName(),
                    token,
                    authUser.getRole().name(),
                    expirationDate
            );
            // Construir la respuesta final con "success", "message" y "data"
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Login successful", data)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>(false, "Invalid credentials", null)
            );
        }
    }


}
