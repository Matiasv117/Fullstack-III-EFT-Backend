package com.saludrednorte.gateway.auth;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de autenticacion del API Gateway.
 * Valida JWT emitidos por ms-auth.
 */
@Service
public class SimpleAuthService {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/login",
            "/api/auth",
            "/actuator/health",
            "/actuator/info",
            "/error"
    );

    private static final List<String> USER_GET_PATH_PREFIXES = List.of(
            "/api/portal",
            "/pacientes",
            "/lista-espera",
            "/api/notificaciones/pendientes"
    );

    private final JwtTokenValidator jwtTokenValidator;

    public SimpleAuthService(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }

    /**
     * Valida el Bearer token JWT emitido por ms-auth.
     */
    public Optional<TokenData> decodeBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            return Optional.empty();
        }

        return jwtTokenValidator.validate(token);
    }

    public boolean isPublicPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    public boolean canAccess(String method, String path, String role) {
        if (path == null || path.isBlank()) {
            return false;
        }

        if (ROLE_ADMIN.equalsIgnoreCase(role)) {
            return true;
        }

        if (!ROLE_USER.equalsIgnoreCase(role)) {
            return false;
        }

        if (!HttpMethod.GET.matches(method)) {
            return false;
        }

        return USER_GET_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    public record TokenData(String username, String role) {
    }
}
