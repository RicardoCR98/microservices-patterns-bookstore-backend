package com.microservice.auth.config;

import com.microservice.auth.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Rutas públicas en este microservicio que NO requieren autenticación.
     */
    private static final List<String> WHITE_LIST = List.of(
            "/auth/register",
            "/auth/login",
            "/auth/a/login"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Permitir rutas públicas sin autenticación
        if (WHITE_LIST.stream().anyMatch(requestURI::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String userId = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            // Extraer el userId (String) desde tu metodo en JwtUtil
            userId = jwtUtil.extractUserId(jwt);
        }

        // Verificamos que tengamos userId y que aún no esté autenticado en el contexto
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Long id = Long.parseLong(userId); // Parseamos a Long
                UserDetails userDetails = userDetailsService.loadUserById(id);

                // Validar el token con el ID
                if (jwtUtil.validateToken(jwt, id)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (NumberFormatException e) {
                // Si userId no era un número válido, simplemente no autenticamos
                // y dejamos pasar para que devuelva 401 si se requiere
            }
        }

        filterChain.doFilter(request, response);
    }
}
