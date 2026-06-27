package com.saludrednorte.bff.service;

import com.saludrednorte.bff.dto.LoginRequest;
import com.saludrednorte.bff.dto.LoginResponse;
import com.saludrednorte.bff.dto.PacienteLoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthProxyServiceTest {

    private WebClient.Builder builder;
    private WebClient webClient;
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    private WebClient.RequestBodySpec requestBodySpec;
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;
    private AuthProxyService service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        builder = mock(WebClient.Builder.class);
        webClient = mock(WebClient.class);
        requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        requestBodySpec = mock(WebClient.RequestBodySpec.class);
        requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        lenient().when(builder.baseUrl(anyString())).thenReturn(builder);
        lenient().when(builder.build()).thenReturn(webClient);

        service = new AuthProxyService(builder, "http://localhost:8087");
    }

    @Test
    void login_debeRetornarLoginResponse() {
        LoginRequest request = new LoginRequest("admin", "admin123");
        LoginResponse expected = new LoginResponse("token", "admin", "ROLE_ADMIN");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/api/auth/login")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(request)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(LoginResponse.class)).thenReturn(Mono.just(expected));

        LoginResponse result = service.login(request);

        assertThat(result.getToken()).isEqualTo("token");
        assertThat(result.getUsername()).isEqualTo("admin");
    }

    @Test
    void validateToken_debeRetornarMap() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/api/auth/validate")).thenReturn(requestBodySpec);
        when(requestBodySpec.header("Authorization", "Bearer token")).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(Map.of("valid", true)));

        Map<String, Object> result = service.validateToken("Bearer token");

        assertThat(result).containsEntry("valid", true);
    }

    @Test
    void getAuthenticatedUser_debeRetornarMap() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/api/auth/me")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header("Authorization", "Bearer token")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(Map.of("username", "admin")));

        Map<String, Object> result = service.getAuthenticatedUser("Bearer token");

        assertThat(result).containsEntry("username", "admin");
    }

    @Test
    void loginPaciente_debeRetornarMap() {
        PacienteLoginRequest request = new PacienteLoginRequest("Juan", "Perez", "12.345.678-5");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/api/auth/login-paciente")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(request)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(Map.of("token", "jwt")));

        Map<String, Object> result = service.loginPaciente(request);

        assertThat(result).containsEntry("token", "jwt");
    }

    @Test
    void isUnauthorized_debeRetornarTruePara401() {
        WebClientResponseException ex = new WebClientResponseException(401, "Unauthorized", null, null, null);
        assertThat(service.isUnauthorized(ex)).isTrue();
    }

    @Test
    void isUnauthorized_debeRetornarFalseParaOtros() {
        WebClientResponseException ex = new WebClientResponseException(400, "Bad Request", null, null, null);
        assertThat(service.isUnauthorized(ex)).isFalse();
    }
}
