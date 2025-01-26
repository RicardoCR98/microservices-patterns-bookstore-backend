package com.microservice.auth.controller;

import com.microservice.auth.dto.AdminResponse;
import com.microservice.auth.dto.ApiResponse;
import com.microservice.auth.dto.UpdateUserRequest;
import com.microservice.auth.model.AuthUser;
import com.microservice.auth.model.UserRole;
import com.microservice.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la gestión de usuarios.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final AuthService authService;

    /**
     * Endpoint para obtener la lista de usuarios que tienen rol USER.
     * Requiere autenticación con rol ADMIN.
     */
    @GetMapping("/role/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersWithRoleUser() {
        log.info("Petición GET /admin/role/user: Listando usuarios con rol USER");

        // Quién está realizando la petición (ADMIN)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser currentUser = authService.findUserByEmail(authentication.getName());

        // Llamamos al metodo que recibe el admin
        List<AuthUser> users = authService.findAllByRole(UserRole.USER, currentUser);

        log.debug("Se encontraron {} usuarios con rol USER", users.size());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lista de usuarios con rol USER", users)
        );
    }

    /**
     * Endpoint para obtener todos los usuarios existentes, independientemente del rol.
     * Requiere autenticación con rol ADMIN.
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        log.info("Petición GET /admin/all: Listando todos los usuarios (requiere rol ADMIN)");

        // Obtener admin autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser currentUser = authService.findUserByEmail(authentication.getName());

        // Nuevo metodo que requiere el admin
        List<AuthUser> users = authService.findAll(currentUser);

        // Convertir a AdminResponse
        List<AdminResponse> adminResponses = users.stream().map(user -> new AdminResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getIsDeleted()
        )).toList();

        log.debug("Se encontraron {} usuarios en total", users.size());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lista de todos los usuarios", adminResponses)
        );
    }

    /**
     * Endpoint para actualizar los campos permitidos de un usuario por su ID.
     * - Solo se pueden cambiar isActive, fullName y email.
     * - Se requiere rol ADMIN para hacer esta operación.
     * - Un ADMIN NO puede actualizar a otro ADMIN.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest updateRequest
    ) {
        log.info("Petición PUT /admin/{}: Actualizar usuario con ID {}", id, id);
        log.debug("Datos para actualización: {}", updateRequest);

        try {
            // Usuario que hace la petición
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();
            AuthUser currentUser = authService.findUserByEmail(currentUserEmail);

            // Actualizamos el usuario objetivo
            AuthUser updatedUser = authService.updateUser(
                    id,
                    updateRequest.getIsActive(),
                    updateRequest.getFullName(),
                    updateRequest.getEmail(),
                    updateRequest.getRole().name(),
                    currentUser
            );

            log.info("Usuario con ID {} actualizado correctamente por {}", id, currentUserEmail);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Usuario actualizado con éxito", null)
            );

        } catch (IllegalArgumentException e) {
            log.warn("Error al actualizar usuario con ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null)
            );
        }
    }

    /**
     * Endpoint para eliminar a un usuario por su ID.
     * - Requiere rol ADMIN.
     * - Un ADMIN NO puede eliminar a otro ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        log.info("Petición DELETE /admin/{}: Eliminar usuario con ID {}", id, id);

        try {
            // Usuario que hace la petición
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();
            log.debug("El usuario autenticado que hace la petición es: {}", currentUserEmail);

            AuthUser currentUser = authService.findUserByEmail(currentUserEmail);

            // Eliminamos al usuario objetivo
            authService.deleteUser(id, currentUser);

            log.info("Usuario con ID {} eliminado correctamente por {}", id, currentUserEmail);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Usuario eliminado con éxito", null)
            );
        } catch (IllegalArgumentException e) {
            log.warn("Error al eliminar usuario con ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null)
            );
        }
    }
}
