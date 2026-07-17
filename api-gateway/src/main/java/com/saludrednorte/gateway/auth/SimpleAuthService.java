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
            "/error",
            "/v3/api-docs",
            "/swagger-ui",
            "/webjars"
    );

    private static final List<String> USER_GET_PATH_PREFIXES = List.of(
            "/api/portal",
            "/api/pacientes",
            "/api/lista-espera",
            "/api/notificaciones",
            "/api/citas",
            "/api/medicos",
            "/api/optimizacion",
            "/api/reportes",
            "/api/horarios"
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

    /**
     * Determina si una ruta no requiere autenticación.
     *
     * @param path la ruta HTTP a evaluar
     * @return true si la ruta es pública (login, auth, actuator)
     */
    public boolean isPublicPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    /**
     * Verifica si un rol tiene permiso para acceder a una ruta con un método específico.
     * Los administradores tienen acceso completo; los usuarios solo GET a rutas permitidas.
     *
     * @param method método HTTP (GET, POST, etc.)
     * @param path   la ruta solicitada
     * @param role   el rol del usuario (ADMIN, USER)
     * @return true si el acceso está permitido
     */
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
