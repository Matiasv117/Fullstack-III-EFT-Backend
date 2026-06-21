package com.saludrednorte.gateway.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

/**
 * Valida tokens JWT emitidos por ms-auth usando la clave compartida del sistema.
 */
@Component
public class JwtTokenValidator {

    @Value("${jwt.secret:miClaveSecretaSuperSeguraParaJWT2024SaludRedNorte}")
    private String secret;

    /**
     * Valida la firma y expiracion del token JWT.
     *
     * @param token token sin prefijo Bearer
     * @return datos del usuario si el token es valido
     */
    public Optional<SimpleAuthService.TokenData> validate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();

            if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
                return Optional.empty();
            }

            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            if (username == null || username.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new SimpleAuthService.TokenData(username, mapRole(role)));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    String mapRole(String jwtRole) {
        if (jwtRole == null || jwtRole.isBlank()) {
            return SimpleAuthService.ROLE_USER;
        }
        String normalized = jwtRole.toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ROLE_ADMIN", "ADMIN" -> SimpleAuthService.ROLE_ADMIN;
            case "ROLE_FUNCIONARIO", "FUNCIONARIO" -> SimpleAuthService.ROLE_ADMIN;
            default -> SimpleAuthService.ROLE_USER;
        };
    }
}
