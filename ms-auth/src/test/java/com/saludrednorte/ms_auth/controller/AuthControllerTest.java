package com.saludrednorte.ms_auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.ms_auth.dto.LoginRequest;
import com.saludrednorte.ms_auth.dto.LoginResponse;
import com.saludrednorte.ms_auth.dto.RegisterRequest;
import com.saludrednorte.ms_auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_debeRetornar200CuandoCredencialesSonValidas() throws Exception {
        LoginRequest request = new LoginRequest("admin", "admin123");
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("token", "admin", "ROLE_ADMIN"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void login_debeRetornar401CuandoCredencialesSonInvalidas() throws Exception {
        LoginRequest request = new LoginRequest("admin", "wrong");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    void register_debeRetornar201CuandoUsuarioEsNuevo() throws Exception {
        RegisterRequest request = new RegisterRequest("nuevo", "clave123", "ROLE_PACIENTE");
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new LoginResponse("token", "nuevo", "ROLE_PACIENTE"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("nuevo"));
    }

    @Test
    void register_debeRetornar409CuandoUsuarioYaExiste() throws Exception {
        RegisterRequest request = new RegisterRequest("admin", "admin123", "ROLE_ADMIN");
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("El nombre de usuario ya existe"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El nombre de usuario ya existe"));
    }

    @Test
    void validateToken_debeRetornar200CuandoTokenEsValido() throws Exception {
        when(authService.validateToken(anyString()))
                .thenReturn(Map.of("valid", true, "username", "admin"));

        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void validateToken_debeRetornar401CuandoTokenEsInvalido() throws Exception {
        when(authService.validateToken(anyString()))
                .thenThrow(new BadCredentialsException("Token inválido"));

        mockMvc.perform(post("/api/auth/validate")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void getAuthenticatedUser_debeRetornarDatosDelUsuario() throws Exception {
        when(authService.getAuthenticatedUser(anyString()))
                .thenReturn(Map.of("username", "admin", "role", "ROLE_ADMIN"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void getAuthenticatedUser_debeRetornar401CuandoNoAutenticado() throws Exception {
        when(authService.getAuthenticatedUser(anyString()))
                .thenThrow(new BadCredentialsException("No autenticado"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autenticado"));
    }
}
