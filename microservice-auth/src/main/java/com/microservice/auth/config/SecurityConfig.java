package com.microservice.auth.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final OAuth2UserServiceImpl oAuth2UserServiceImpl;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitamos CSRF ya que manejamos tokens JWT y son STATELESS bro
                .csrf(csrf -> csrf.disable())

                // Configuración de autorización de rutas
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        // "/auth/register" y "/auth/login" serán públicas
                        .requestMatchers("/auth/register", "/auth/login","/auth/a/login").permitAll()
                        // Rutas a las que sólo se puede acceder con token (autenticación)
                        // y con un rol específico
                        // En este caso, forzamos que "/auth/a/register" requiera rol ADMIN,
                        .requestMatchers("/auth/a/register").hasRole("ADMIN")
                        // Rutas de Swagger solo accesibles para ADMIN
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).hasRole("ADMIN")
                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )

                // Configuración de OAuth2 Login
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oAuth2UserServiceImpl)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                )
//                .oauth2Login(auth->auth.disable())

                // Stateless: no se mantienen sesiones del lado del servidor
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )

                // Añadimos el filtro JWT antes del UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
