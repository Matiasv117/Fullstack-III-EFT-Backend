package com.saludrednorte.gateway.auth;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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

    public String generarToken(String usuario, String rol) {
        String datos = usuario + ":" + rol.toUpperCase(Locale.ROOT);
        return Base64.getEncoder().encodeToString(datos.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<TokenData> decodeBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            return Optional.empty();
        }

        try {
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] partes = decoded.split(":", 2);
            if (partes.length != 2 || partes[0].isBlank() || partes[1].isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new TokenData(partes[0], partes[1].toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
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


