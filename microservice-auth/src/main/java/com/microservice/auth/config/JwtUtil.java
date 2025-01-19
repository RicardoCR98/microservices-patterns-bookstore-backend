package com.microservice.auth.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Genera un token JWT con el ID del usuario como el sujeto (sub).
     *
     * @param userId ID numérico del usuario.
     * @param role   Rol del usuario.
     * @return Token JWT firmado.
     */
    public String generateToken(Long userId, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // Usar el ID numérico del usuario como "sub"
                .claim("role", role) // Agregar el rol como un claim
                .setIssuedAt(new Date()) // Fecha de emisión
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Fecha de expiración
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256) // Firmar con la clave secreta
                .compact();
    }

    /**
     * Extrae el ID del usuario (sub) del token JWT.
     *
     * @param token Token JWT.
     * @return ID del usuario como String.
     */
    public String extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // Retorna el "sub" del token
    }

    /**
     * Valida el token verificando que no haya expirado y que el ID del usuario coincida.
     *
     * @param token Token JWT.
     * @param userId ID del usuario.
     * @return true si el token es válido, false en caso contrario.
     */
    public boolean validateToken(String token, Long userId) {
        String extractedUserId = extractUserId(token);
        return extractedUserId.equals(String.valueOf(userId)) && !isTokenExpired(token);
    }

    /**
     * Verifica si el token ha expirado.
     *
     * @param token Token JWT.
     * @return true si el token ha expirado, false en caso contrario.
     */
    private boolean isTokenExpired(String token) {
        Date expirationDate = Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expirationDate.before(new Date());
    }

    /**
     * Genera un token JWT desde un nombre de usuario.
     *
     * @param userId ID del usuario.
     * @return Token JWT.
     */
    public String generateTokenFromUserId(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // Usar el ID como "sub"
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae la fecha de expiración del token JWT.
     *
     * @param token Token JWT.
     * @return Fecha de expiración.
     */
    public Date getExpirationDateFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}
