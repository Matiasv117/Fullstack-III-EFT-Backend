package com.saludrednorte.bff.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.bff.dto.LoginRequest;
import com.saludrednorte.bff.dto.LoginResponse;
import com.saludrednorte.bff.dto.PacienteLoginRequest;
import com.saludrednorte.bff.service.AuthProxyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthProxyService authProxyService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_debeRetornar200() throws Exception {
        LoginRequest request = new LoginRequest("admin", "admin123");
        when(authProxyService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("token", "admin", "ROLE_ADMIN"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void login_debeRetornar401CuandoNoAutorizado() throws Exception {
        when(authProxyService.login(any(LoginRequest.class)))
                .thenThrow(mock(WebClientResponseException.Unauthorized.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    void login_debeReenviarStatusCodeDeError() throws Exception {
        when(authProxyService.login(any(LoginRequest.class)))
                .thenThrow(new WebClientResponseException(400, "Bad Request", null, null, null));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateToken_debeRetornar200() throws Exception {
        when(authProxyService.validateToken("Bearer valid-token"))
                .thenReturn(Map.of("valid", true, "username", "admin"));

        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void validateToken_debeRetornar401CuandoNoAutorizado() throws Exception {
        when(authProxyService.validateToken("Bearer bad-token"))
                .thenThrow(new WebClientResponseException(401, "Unauthorized", null, null, null));

        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void validateToken_debeRetornar401EnErrorGenerico() throws Exception {
        when(authProxyService.validateToken("Bearer bad-token"))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAuthenticatedUser_debeRetornar200() throws Exception {
        when(authProxyService.getAuthenticatedUser("Bearer valid-token"))
                .thenReturn(Map.of("username", "admin", "role", "ROLE_ADMIN"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void getAuthenticatedUser_debeRetornar401CuandoNoAutorizado() throws Exception {
        when(authProxyService.getAuthenticatedUser("Bearer bad-token"))
                .thenThrow(mock(WebClientResponseException.Unauthorized.class));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autenticado"));
    }

    @Test
    void getAuthenticatedUser_debeRetornar401EnErrorGenerico() throws Exception {
        when(authProxyService.getAuthenticatedUser("Bearer bad-token"))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginPaciente_debeRetornar200() throws Exception {
        PacienteLoginRequest request = new PacienteLoginRequest("Juan", "Perez", "12.345.678-5");
        when(authProxyService.loginPaciente(any(PacienteLoginRequest.class)))
                .thenReturn(Map.of("token", "jwt", "username", "PACIENTE_1"));

        mockMvc.perform(post("/api/auth/login-paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"));
    }

    @Test
    void loginPaciente_debeReenviarStatusCode() throws Exception {
        when(authProxyService.loginPaciente(any(PacienteLoginRequest.class)))
                .thenThrow(new WebClientResponseException(400, "Bad Request", null, null, null));

        mockMvc.perform(post("/api/auth/login-paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PacienteLoginRequest("", "", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginPaciente_debeRetornar500EnErrorGenerico() throws Exception {
        when(authProxyService.loginPaciente(any(PacienteLoginRequest.class)))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/auth/login-paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PacienteLoginRequest("J", "P", "1-9"))))
                .andExpect(status().isInternalServerError());
    }
}
