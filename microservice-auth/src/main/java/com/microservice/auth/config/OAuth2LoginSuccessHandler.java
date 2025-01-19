package com.microservice.auth.config;

import com.microservice.auth.config.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Aquí asumes que el authentication principal es un UserDetails con un username (email).
        // Tu OidcUserService debería retornar un principal que puedas mapear a tu modelo de usuario.
        // Por ejemplo, si en OAuth2UserServiceImpl creas un usuario en la BD y cargas un UserDetails:

//        String username = authentication.getName();
//        String token = jwtUtil.generateTokenFromUsername(username);
//
//        // Redirige a tu frontend (puede ser http://localhost:3000) con el token como query param
//        String redirectUrl = "http://localhost:3000/oauth2/redirect?token="
//                + URLEncoder.encode(token, StandardCharsets.UTF_8);
//
//        response.sendRedirect(redirectUrl);
    }
}