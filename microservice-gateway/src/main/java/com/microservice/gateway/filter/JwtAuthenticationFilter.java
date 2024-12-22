package com.microservice.gateway.filter;

import com.microservice.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Order(0) // Se ejecuta este filtro antes que otros
public class JwtAuthenticationFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;

    // Opcional: Rutas que no requieren autenticación
    private static final String[] WHITE_LIST = {
            "/auth/register",  // Permitir registro sin autenticación
            "/auth/login",     // Permitir login sin autenticación
            "/auth/**",        // Generalmente las rutas públicas del microservicio de autenticación
            "/mantenimiento/**", // Rutas de mantenimiento
            // Añade aquí otras rutas que no necesiten autenticación
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

//        Claims claims = jwtUtil.getAllClaimsFromToken(token);
//        System.out.println("Claims: " + claims);
        // Aquí podrías verificar roles o lo que requieras
        // Ejemplo: Si quieres asegurarte que el usuario tenga cierto rol
        // List<String> roles = claims.get("roles", List.class);

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        return Mono.defer(() -> {
            throw new ResponseStatusException(httpStatus, err);
        });
    }
}