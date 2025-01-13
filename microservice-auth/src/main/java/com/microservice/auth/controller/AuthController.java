
// AuthController.java
package com.microservice.auth.controller;

import com.microservice.auth.config.JwtUtil;
import com.microservice.auth.dto.ApiResponse;
import com.microservice.auth.dto.AuthResponse;
import com.microservice.auth.dto.LoginRequest;
import com.microservice.auth.dto.RegisterRequest;
import com.microservice.auth.model.AuthUser;
import com.microservice.auth.model.UserRole;
import com.microservice.auth.service.AuthService;
import com.microservice.auth.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
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

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationConfiguration authConfig;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Register request received for email: {}", request.getEmail());

        try {
            AuthUser newUser = authService.registerUser(request.getFullName(), request.getEmail(), request.getPassword());
            logger.info("User registered successfully with ID: {}", newUser.getUserId());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "User registered successfully", new AuthResponse(
                            newUser.getUserId(),
                            newUser.getFullName(),
                            null, // No se genera token en el registro
                            newUser.getRole().name(),
                            null, // No se genera fecha de expiración
                            newUser.getIsActive()
                    ))
            );
        } catch (IllegalArgumentException e) {
            logger.error("Error registering user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, e.getMessage(), null)
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request received for email: {}", request.getEmail());

        try {
            AuthUser authUser = authService.findUserByEmail(request.getEmail());

            if (!authUser.getIsActive()) {
                logger.warn("El usuario se encuentra bloqueado: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>(false, "El usuario se encuentra bloqueado", null)
                );
            }

            AuthenticationManager authManager = authConfig.getAuthenticationManager();

            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            String token = jwtUtil.generateToken(userDetails);
            Long expirationDate = jwtUtil.getExpirationDateFromToken(token).getTime();

            AuthResponse data = new AuthResponse(
                    authUser.getUserId(),
                    authUser.getFullName(),
                    token,
                    authUser.getRole().name(),
                    expirationDate,
                    authUser.getIsActive()
            );

            logger.info("User logged in successfully: {}", request.getEmail());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Login successful", data)
            );
        } catch (Exception e) {
            logger.error("Error during login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>(false, "Credenciales inválidas", null)
            );
        }
    }

    /**
     * Register Admin
     * Requiere Rol ADMIN para acceder.
     */
    @PostMapping("/a/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerA(@Valid @RequestBody RegisterRequest request) {
        logger.info("Admin register request received for email: {}", request.getEmail());

        try {
            AuthUser newUser = authService.registerAdmin(request.getFullName(), request.getEmail(), request.getPassword());
            logger.info("Admin registered successfully with ID: {}", newUser.getUserId());

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Admin registered successfully",
                            new AuthResponse(
                                    newUser.getUserId(),
                                    newUser.getFullName(),
                                    null, // No se genera token en el registro
                                    newUser.getRole().name(),
                                    null, // No se genera fecha de expiración
                                    newUser.getIsActive()
                            )
                    )
            );
        } catch (IllegalArgumentException e) {
            logger.error("Error registering Admin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, e.getMessage(), null)
            );
        }
    }

    /**
     * Login Admin
     * NO requiere Rol ADMIN para acceder.
     */
    @PostMapping("/a/login")
    public ResponseEntity<?> loginAdmin(@Valid @RequestBody LoginRequest request) {
        logger.info("Admin login request received for email: {}", request.getEmail());

        try {
            AuthUser authUser = authService.findUserByEmail(request.getEmail());

            if (!authUser.getIsActive()) {
                logger.warn("El administrador se encuentra bloqueado: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>(false, "El usuario se encuentra bloqueado", null)
                );
            }

            if (!authUser.getRole().equals(UserRole.ADMIN)) {
                logger.warn("El usuario no es administrador: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ApiResponse<>(false, "El usuario no es administrador", null)
                );
            }

            AuthenticationManager authManager = authConfig.getAuthenticationManager();

            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            String token = jwtUtil.generateToken(userDetails);
            Long expirationDate = jwtUtil.getExpirationDateFromToken(token).getTime();

            AuthResponse data = new AuthResponse(
                    authUser.getUserId(),
                    authUser.getFullName(),
                    token,
                    authUser.getRole().name(),
                    expirationDate,
                    authUser.getIsActive()
            );

            logger.info("Admin logged in successfully: {}", request.getEmail());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Login successful", data)
            );
        } catch (Exception e) {
            logger.error("Error during login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>(false, "Credenciales inválidas", null)
            );
        }
    }
}
