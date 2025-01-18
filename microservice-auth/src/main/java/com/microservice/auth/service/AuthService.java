package com.microservice.auth.service;

import com.microservice.auth.client.UsersClient;
import com.microservice.auth.model.AuthOutbox;
import com.microservice.auth.model.AuthUser;
import com.microservice.auth.model.UserRole;
import com.microservice.auth.repositories.OutboxRepository;
import com.microservice.auth.repositories.UserRepository;
import com.microservice.auth.model.LogEntry;
import com.microservice.auth.repositories.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersClient usersClient;
    private final LogEntryRepository logEntryRepository;

    /**
     * Registra un usuario con rol USER.
     */
    public AuthUser registerUser(String fullName, String email, String password) {
        logger.info("Intentando registrar usuario con email {}", email);

        if (userRepository.findByEmail(email).isPresent()) {
            createLog(
                    null,
                    "REGISTRATION_FAILED",
                    "Registration attempt with existing email",
                    "Email: " + email,
                    null,
                    null,
                    null,
                    null,
                    null
            );
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

        createLog(
                savedUser,
                "USER_REGISTERED",
                "New user registration",
                "Email: " + email + ", Role: USER",
                null,
                null,
                UserRole.USER.name(),
                null,
                true
        );

        createUserProfileAndOutboxEvent(savedUser);

        return savedUser;
    }

    /**
     * Registra un usuario con rol ADMIN.
     */
    public AuthUser registerAdmin(String fullName, String email, String password) {
        logger.info("Intentando registrar admin con email: {}", email);

        if (userRepository.findByEmail(email).isPresent()) {
            createLog(
                    null,
                    "ADMIN_REGISTRATION_FAILED",
                    "Admin registration attempt with existing email",
                    "Email: " + email,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            throw new IllegalArgumentException("Email ya en uso");
        }

        AuthUser newUser = new AuthUser();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setRole(UserRole.ADMIN);
        newUser.setIsActive(true);

        AuthUser savedUser = userRepository.save(newUser);

        createLog(
                savedUser,
                "ADMIN_REGISTERED",
                "New admin registration",
                "Email: " + email,
                null,
                null,
                UserRole.ADMIN.name(),
                null,
                true
        );

        createUserProfileAndOutboxEvent(savedUser);
        return savedUser;
    }

    /**
     * Busca un usuario por su email.
     */
    public AuthUser findUserByEmail(String email) {
        logger.debug("Searching for user by email: {}", email);
        AuthUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    createLog(
                            null,
                            "USER_NOT_FOUND",
                            "Attempt to find non-existent user by email",
                            "Email: " + email,
                            null,
                            null,
                            null,
                            null,
                            null
                    );
                    return new IllegalArgumentException("User not found with email: " + email);
                });

        createLog(
                user,
                "USER_FOUND",
                "User found by email",
                "Email: " + email,
                null,
                null,
                user.getRole().name(),
                null,
                user.getIsActive()
        );

        return user;
    }

    /**
     * Retorna todos los usuarios (sin filtrar).
     */
    public List<AuthUser> findAll() {
        logger.info("Listando todos los usuarios");

        List<AuthUser> allUsers = userRepository.findAll();

        if (allUsers.isEmpty()) {
            createLog(
                    null,
                    "NO_USERS_FOUND",
                    "No users found in the system",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            logger.warn("No se encontraron usuarios en el sistema");
        } else {
            createLog(
                    null,
                    "USERS_FOUND",
                    "Users found in the system",
                    "Total users: " + allUsers.size(),
                    null,
                    null,
                    null,
                    null,
                    null
            );
            logger.info("Se encontraron {} usuarios en el sistema", allUsers.size());
        }

        return allUsers;
    }

    /**
     * Retorna todos los usuarios con el rol especificado.
     */
    public List<AuthUser> findAllByRole(UserRole role) {
        logger.info("Listando todos los usuarios con rol: {}", role);

        List<AuthUser> usersByRole = userRepository.findAll().stream()
                .filter(user -> user.getRole().equals(role))
                .toList();

        if (usersByRole.isEmpty()) {
            createLog(
                    null,
                    "NO_USERS_FOUND_BY_ROLE",
                    "No users found with the specified role",
                    "Role: " + role,
                    null,
                    null,
                    role.name(),
                    null,
                    null
            );
            logger.warn("No se encontraron usuarios con el rol: {}", role);
        } else {
            createLog(
                    null,
                    "USERS_FOUND_BY_ROLE",
                    "Users found with the specified role",
                    "Role: " + role + ", Count: " + usersByRole.size(),
                    null,
                    null,
                    role.name(),
                    null,
                    null
            );
            logger.info("Se encontraron {} usuarios con el rol: {}", usersByRole.size(), role);
        }

        return usersByRole;
    }

    /**
     * Actualiza los campos permitidos de un usuario por su ID.
     */
    public AuthUser updateUser(Long userId, Boolean isActive, String fullName, String email, String role, AuthUser currentUser) {
        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            logger.warn("El usuario con ID {} no tiene permisos para actualizar usuarios.", currentUser.getUserId());
            createLog(
                    currentUser,
                    "UNAUTHORIZED_UPDATE_ATTEMPT",
                    "Non-admin user attempted to update user",
                    "Target userId: " + userId,
                    currentUser.getUserId(),
                    null,
                    null,
                    null,
                    null
            );
            throw new IllegalArgumentException("No tienes permisos para actualizar usuarios.");
        }

        AuthUser targetUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("No se encontró usuario con ID {}", userId);
                    createLog(
                            currentUser,
                            "USER_NOT_FOUND",
                            "Attempted to update non-existent user",
                            "Target userId: " + userId,
                            currentUser.getUserId(),
                            null,
                            null,
                            null,
                            null
                    );
                    return new IllegalArgumentException("No existe el usuario con ID: " + userId);
                });

        if (targetUser.getRole().equals(UserRole.ADMIN) && !targetUser.getUserId().equals(currentUser.getUserId())) {
            logger.warn("El admin con ID {} intentó actualizar a otro admin con ID {}. No está permitido.",
                    currentUser.getUserId(), targetUser.getUserId());
            createLog(
                    currentUser,
                    "UNAUTHORIZED_ADMIN_UPDATE",
                    "Admin attempted to update another admin",
                    "Target adminId: " + targetUser.getUserId(),
                    currentUser.getUserId(),
                    targetUser.getRole().name(),
                    role,
                    targetUser.getIsActive(),
                    isActive
            );
            throw new IllegalArgumentException("No puedes editar a otro administrador.");
        }

        String previousRole = targetUser.getRole().name();
        Boolean previousActiveStatus = targetUser.getIsActive();

        if (isActive != null) {
            targetUser.setIsActive(isActive);
            logger.debug("Cambiado el isActive de {} a {}", userId, isActive);
        }
        if (fullName != null && !fullName.isBlank()) {
            targetUser.setFullName(fullName);
            logger.debug("Cambiado el fullName de {} a {}", userId, fullName);
        }
        if (email != null && !email.isBlank()) {
            targetUser.setEmail(email);
            logger.debug("Cambiado el email de {} a {}", userId, email);
        }
        if (role != null && !role.isBlank()) {
            targetUser.setRole(UserRole.valueOf(role));
            logger.debug("Cambiado el role de {} a {}", userId, role);
        }

        AuthUser updatedUser = userRepository.save(targetUser);

        logger.info("Usuario con ID {} fue actualizado por el ADMIN con ID {}", userId, currentUser.getUserId());

        createLog(
                updatedUser,
                "USER_UPDATED",
                "User updated successfully",
                "Updated fields: isActive, fullName, email, role",
                currentUser.getUserId(),
                previousRole,
                targetUser.getRole().name(),
                previousActiveStatus,
                targetUser.getIsActive()
        );

        return updatedUser;
    }

    /**
     * Elimina un usuario.
     */
    public void deleteUser(Long userId, AuthUser currentUser) {
        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            logger.warn("El usuario con ID {} no tiene permisos para eliminar usuarios.", currentUser.getUserId());
            createLog(
                    currentUser,
                    "UNAUTHORIZED_DELETE_ATTEMPT",
                    "Non-admin user attempted to delete user",
                    "Target userId: " + userId,
                    currentUser.getUserId(),
                    null,
                    null,
                    null,
                    null
            );
            throw new IllegalArgumentException("No tienes permisos para eliminar usuarios.");
        }

        AuthUser targetUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("No se encontró usuario con ID {}", userId);
                    createLog(
                            currentUser,
                            "USER_NOT_FOUND",
                            "Attempted to delete non-existent user",
                            "Target userId: " + userId,
                            currentUser.getUserId(),
                            null,
                            null,
                            null,
                            null
                    );
                    return new IllegalArgumentException("No existe el usuario con ID: " + userId);
                });

        if (targetUser.getRole().equals(UserRole.ADMIN) && !targetUser.getUserId().equals(currentUser.getUserId())) {
            logger.warn("El admin con ID {} intentó eliminar a otro admin con ID {}. No está permitido.",
                    currentUser.getUserId(), targetUser.getUserId());
            createLog(
                    currentUser,
                    "UNAUTHORIZED_ADMIN_DELETE",
                    "Admin attempted to delete another admin",
                    "Target adminId: " + targetUser.getUserId(),
                    currentUser.getUserId(),
                    targetUser.getRole().name(),
                    null,
                    targetUser.getIsActive(),
                    null
            );
            throw new IllegalArgumentException("No puedes eliminar a otro administrador.");
        }

        userRepository.delete(targetUser);
        logger.info("Usuario con ID {} eliminado correctamente por el ADMIN con ID {}", userId, currentUser.getUserId());

        createLog(
                targetUser,
                "USER_DELETED",
                "User successfully deleted",
                "Deleted userId: " + userId,
                currentUser.getUserId(),
                targetUser.getRole().name(),
                null,
                targetUser.getIsActive(),
                null
        );
    }

    private void createUserProfileAndOutboxEvent(AuthUser savedUser) {
        usersClient.createUserProfile(new com.microservice.auth.dto.UserProfileRequest(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getFullName()
        ));
        logger.debug("Perfil de usuario creado en msvc-users para el userId: {}", savedUser.getUserId());

        AuthOutbox event = new AuthOutbox();
        event.setAggregateId(savedUser.getUserId());
        event.setEventType("USER_REGISTERED");

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", savedUser.getUserId());
        payload.put("email", savedUser.getEmail());
        event.setPayload(payload);

        outboxRepository.save(event);
        logger.info("Evento Outbox (USER_REGISTERED) creado para userId: {}", savedUser.getUserId());
    }

    private void createLog(
            AuthUser user,
            String action,
            String description,
            String additionalDetails,
            Long performedByUserId,
            String previousRole,
            String newRole,
            Boolean previousActiveStatus,
            Boolean newActiveStatus
    ) {
        LogEntry logEntry = new LogEntry();
        logEntry.setUser(user);
        logEntry.setAction(action);
        logEntry.setDescription(description);
        logEntry.setAdditionalDetails(additionalDetails);
        logEntry.setPerformedByUserId(performedByUserId);
        logEntry.setPreviousRole(previousRole);
        logEntry.setNewRole(newRole);
        logEntry.setPreviousActiveStatus(previousActiveStatus);
        logEntry.setNewActiveStatus(newActiveStatus);
        logEntry.setCreatedAt(Instant.now());
        logEntryRepository.save(logEntry);
    }
}
