package com.microservice.gateway.filter;

import com.microservice.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.util.List;
@Component
@RequiredArgsConstructor
@Order(0) // Se ejecuta este filtro antes que otros
public class JwtAuthenticationFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    // Opcional: Rutas que no requieren autenticación
    private static final String[] WHITE_LIST = {
            "/auth/register",  // Permitir registro sin autenticación
            "/auth/login",     // Permitir login sin autenticación
            "/auth/a/login",   // Permitir login de administrador sin autenticación
            "/mantenimiento/**", // Rutas de mantenimiento
    };


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Si la ruta está en la white list, dejamos pasar sin validar token
        for (String route : WHITE_LIST) {
            if (path.matches(route.replace("**", ".*"))) {
                return chain.filter(exchange);
            }
        }

        // Extrae el token del header Authorization
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return this.onError(exchange, "No JWT token found in request headers", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return this.onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        }
        Claims claims = jwtUtil.getAllClaimsFromToken(token);
        String role = claims.get("role", String.class);
        // Validar rol ADMIN para rutas Swagger
        if (path.startsWith("/auth-docs") || path.startsWith("/users-docs") ||
                path.startsWith("/books-docs") || path.startsWith("/orders-docs")) {
            if (!"ADMIN".equals(role)) {
                return this.onError(exchange, "Forbidden: Admin access required", HttpStatus.FORBIDDEN);
            }
        }
        logger.info("Role from token: {}", role);



        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        return Mono.defer(() -> {
            throw new ResponseStatusException(httpStatus, err);
        });
    }
}