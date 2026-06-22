package com.saludrednorte.bff.service;

import com.saludrednorte.bff.dto.LoginRequest;
import com.saludrednorte.bff.dto.LoginResponse;
import com.saludrednorte.bff.dto.PacienteLoginRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Servicio que delega las operaciones de autenticación al microservicio ms-auth.
 */
@Service
public class AuthProxyService {

    private final WebClient authWebClient;

    public AuthProxyService(WebClient.Builder builder,
                            @Value("${bff.auth.base-url:http://localhost:8087}") String authBaseUrl) {
        this.authWebClient = builder
                .baseUrl(authBaseUrl.endsWith("/") ? authBaseUrl.substring(0, authBaseUrl.length() - 1) : authBaseUrl)
                .build();
    }

    /**
     * Delega el login al microservicio ms-auth.
     */
    public LoginResponse login(LoginRequest request) {
        return authWebClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();
    }

    /**
     * Delega la validación de token al microservicio ms-auth.
     */
    public Map<String, Object> validateToken(String authorizationHeader) {
        return authWebClient.post()
                .uri("/api/auth/validate")
                .header("Authorization", authorizationHeader)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    /**
     * Delega la consulta del usuario autenticado al microservicio ms-auth.
     */
    public Map<String, Object> getAuthenticatedUser(String authorizationHeader) {
        return authWebClient.get()
                .uri("/api/auth/me")
                .header("Authorization", authorizationHeader)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    /**
     * Delega el login de paciente al microservicio ms-auth.
     */
    public Map<String, Object> loginPaciente(PacienteLoginRequest request) {
        return authWebClient.post()
                .uri("/api/auth/login-paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    /**
     * Indica si la excepción corresponde a un error de autenticación.
     */
    public boolean isUnauthorized(WebClientResponseException ex) {
        return ex.getStatusCode() == HttpStatus.UNAUTHORIZED;
    }
}
