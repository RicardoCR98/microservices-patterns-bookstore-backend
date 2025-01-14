// AuthService.java
package com.microservice.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.auth.client.UsersClient;
import com.microservice.auth.model.AuthOutbox;
import com.microservice.auth.model.AuthUser;
import com.microservice.auth.model.UserRole;
import com.microservice.auth.repositories.OutboxRepository;
import com.microservice.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersClient usersClient;
    /**
     * Registra un usuario con rol USER.
     */
    public AuthUser registerUser(String fullName, String email, String password) {
        logger.info("Intentando registrar usuario con email {}", email);

        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("No se puede registrar usuario. El email {} ya está en uso", email);
            throw new IllegalArgumentException("Email ya en uso");
        }

        AuthUser newUser = new AuthUser();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setRole(UserRole.USER);
        newUser.setIsActive(true);

        AuthUser savedUser = userRepository.save(newUser);
        logger.info("Usuario registrado exitosamente con ID: {}", savedUser.getUserId());

        // Llamada a msvc-users para crear el perfil
        usersClient.createUserProfile(new com.microservice.auth.dto.UserProfileRequest(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getFullName()
        ));
        logger.debug("Perfil de usuario creado en msvc-users para el userId: {}", savedUser.getUserId());

        // Crear evento Outbox
        AuthOutbox event = new AuthOutbox();
        event.setAggregateId(savedUser.getUserId());
        event.setEventType("USER_REGISTERED");

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", savedUser.getUserId());
        payload.put("email", savedUser.getEmail());
        event.setPayload(payload);

        outboxRepository.save(event);
        logger.info("Evento Outbox (USER_REGISTERED) creado para userId: {}", savedUser.getUserId());

        return savedUser;
    }

    /**
     * Registra un usuario con rol ADMIN.
     */

    public AuthUser registerAdmin(String fullName, String email, String password) {
        logger.info("Intentando registrar admin con email: {}", email);

        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("No se puede registrar admin. El email {} ya está en uso", email);
            throw new IllegalArgumentException("Email ya en uso");
        }

        AuthUser newUser = new AuthUser();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setRole(UserRole.ADMIN);
        newUser.setIsActive(true);

        AuthUser savedUser = userRepository.save(newUser);
        logger.info("Admin registrado exitosamente con ID: {}", savedUser.getUserId());

        // Llamada a msvc-users para crear el perfil
        usersClient.createUserProfile(new com.microservice.auth.dto.UserProfileRequest(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getFullName()
        ));
        logger.debug("Perfil de admin creado en msvc-users para el userId: {}", savedUser.getUserId());

        // Crear evento Outbox
        AuthOutbox event = new AuthOutbox();
        event.setAggregateId(savedUser.getUserId());
        event.setEventType("USER_REGISTERED");

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", savedUser.getUserId());
        payload.put("email", savedUser.getEmail());
        event.setPayload(payload);

        outboxRepository.save(event);
        logger.info("Evento Outbox (USER_REGISTERED) creado para admin con ID: {}", savedUser.getUserId());

        return savedUser;
    }

    /**
     * Busca un usuario por su email.
     */
    public AuthUser findUserByEmail(String email) {
        logger.debug("Searching for user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new IllegalArgumentException("User not found with email: " + email);
                });
    }

    /**
     * Retorna todos los usuarios (sin filtrar).
     */
    public List<AuthUser> findAll() {
        logger.info("Listando todos los usuarios");
        return userRepository.findAll();
    }

    /**
     * Retorna todos los usuarios con el rol especificado.
     */
    public List<AuthUser> findAllByRole(UserRole role) {
        logger.info("Listando todos los usuarios con rol: {}", role);
        List<AuthUser> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(user -> user.getRole().equals(role))
                .toList();
    }

    /**
     * Actualiza los campos permitidos (isActive, fullName, email) de un usuario dado su ID.
     * Valida que el usuario que actualiza (currentUser) sea ADMIN
     * y no permita actualizar a otro usuario que sea ADMIN.
     */
    public AuthUser updateUser(
            Long userId,
            Boolean isActive,
            String fullName,
            String email,
            String role,
            AuthUser currentUser
    ) {
        // Verificamos que el usuario que hace la petición sea ADMIN
        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            logger.warn("El usuario con ID {} no tiene permisos para actualizar usuarios.", currentUser.getUserId());
            throw new IllegalArgumentException("No tienes permisos para actualizar usuarios.");
        }

        AuthUser targetUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("No se encontró usuario con ID {}", userId);
                    return new IllegalArgumentException("No existe el usuario con ID: " + userId);
                });

        // Si el targetUser es ADMIN y no es el mismo que currentUser, lanzamos excepción.
        if (targetUser.getRole().equals(UserRole.ADMIN)
                && !targetUser.getUserId().equals(currentUser.getUserId())) {
            logger.warn("El admin con ID {} intentó editar a otro admin con ID {}. No está permitido.",
                    currentUser.getUserId(), targetUser.getUserId());
            throw new IllegalArgumentException("No puedes editar a otro administrador.");
        }

        // Actualizamos solo los campos permitidos
        if (isActive != null) {
            targetUser.setIsActive(isActive);
            logger.debug("Cambiado el isActive de {} a {}", userId, isActive);
        }
        if (fullName != null && !fullName.isBlank()) {
            targetUser.setFullName(fullName);
            logger.debug("Cambiado el fullName de {} a {}", userId, fullName);
        }
        if (email != null && !email.isBlank()) {
            // Podrías validar que no exista otro usuario con el mismo email
            targetUser.setEmail(email);
            logger.debug("Cambiado el email de {} a {}", userId, email);
        }
        if (role != null && !role.isBlank()) {
            targetUser.setRole(UserRole.valueOf(role));
            logger.debug("Cambiado el role de {} a {}", userId, role);
        }

        // Guarda los cambios
        AuthUser updatedUser = userRepository.save(targetUser);
        logger.info("Usuario con ID {} fue actualizado por el ADMIN con ID {}", userId, currentUser.getUserId());

        return updatedUser;
    }

    /**
     * Elimina un usuario
     * No permite que un ADMIN elimine a otro ADMIN distinto.
     */
    public void deleteUser(Long userId, AuthUser currentUser) {
        // Verificamos que el usuario que hace la petición sea ADMIN
        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            logger.warn("El usuario con ID {} no tiene permisos para eliminar usuarios.", currentUser.getUserId());
            throw new IllegalArgumentException("No tienes permisos para eliminar usuarios.");
        }

        AuthUser targetUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("No se encontró usuario con ID {}", userId);
                    return new IllegalArgumentException("No existe el usuario con ID: " + userId);
                });

        // Si el targetUser es ADMIN y no es el mismo que currentUser, lanzamos excepción.
        if (targetUser.getRole().equals(UserRole.ADMIN)
                && !targetUser.getUserId().equals(currentUser.getUserId())) {
            logger.warn("El admin con ID {} intentó eliminar a otro admin con ID {}. No está permitido.",
                    currentUser.getUserId(), targetUser.getUserId());
            throw new IllegalArgumentException("No puedes eliminar a otro administrador.");
        }

        // Ejemplo de baja lógica (descomenta si prefieres):
        // targetUser.setIsActive(false);
        // userRepository.save(targetUser);
        // log.info("Usuario con ID {} fue dado de baja lógicamente por el ADMIN con ID {}",
        //         userId, currentUser.getUserId());

        // Eliminación física
        userRepository.delete(targetUser);
        logger.info("Usuario con ID {} fue eliminado físicamente por el ADMIN con ID {}", userId, currentUser.getUserId());
    }
}
