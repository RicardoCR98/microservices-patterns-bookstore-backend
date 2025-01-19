package com.microservice.orders.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // Revisar si hay un usuario autenticado y si las credenciales (el token) no son null
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() != null) {
            String jwtToken = (String) auth.getCredentials();
            // Adjuntar la cabecera Authorization a la petición Feign
            template.header("Authorization", "Bearer " + jwtToken);
        }
    }
}
