package com.saludrednorte.ms_auth.controller;

import com.saludrednorte.ms_auth.dto.LoginRequest;
import com.saludrednorte.ms_auth.dto.LoginResponse;
import com.saludrednorte.ms_auth.dto.PacienteLoginRequest;
import com.saludrednorte.ms_auth.dto.RegisterRequest;
import com.saludrednorte.ms_auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para el proceso de autenticación y validación de tokens JWT.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para inicio de sesión, registro y validación de tokens")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Inicia sesión y genera un token JWT.
     *
     * @param loginRequest credenciales de acceso del usuario
     * @return respuesta HTTP con el token generado y detalles del usuario
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida credenciales y retorna un token JWT")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            return ResponseEntity.ok(authService.login(loginRequest));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        }
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * Solo accesible para usuarios con rol ADMIN.
     *
     * @param request datos del usuario a registrar
     * @return respuesta HTTP con el token JWT del usuario creado
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario y retorna un token JWT (Solo ADMIN)")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, @RequestHeader("Authorization") String token) {
        try {
            // Verificar que el solicitante sea admin
            Map<String, Object> userInfo = authService.getAuthenticatedUser(token);
            String role = (String) userInfo.get("role");
            
            if (!"ROLE_ADMIN".equals(role)) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo administradores pueden registrar funcionarios"));
            }
            
            LoginResponse response = authService.register(request);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
    }

    /**
     * Valida si un token recibido es válido.
     *
     * @param token token a validar (Bearer token)
     * @return respuesta HTTP indicando la validez del token
     */
    @PostMapping("/validate")
    @Operation(summary = "Validar token", description = "Verifica si el token JWT enviado es válido y no ha expirado")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(authService.validateToken(token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "error", "Token inválido"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "error", "Error en validación"));
        }
    }

    /**
     * Retorna información del usuario autenticado según el token JWT.
     *
     * @param token token JWT en el header Authorization
     * @return datos del usuario autenticado
     */
    @GetMapping("/me")
    @Operation(summary = "Usuario autenticado", description = "Obtiene información del usuario a partir del token JWT")
    public ResponseEntity<?> getAuthenticatedUser(@RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(authService.getAuthenticatedUser(token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Autentica un paciente usando sus datos personales (nombre, apellido, RUT).
     * Si el paciente no existe, lo crea automáticamente.
     *
     * @param request datos del paciente (nombre, apellido, RUT)
     * @return respuesta HTTP con el token JWT y datos del paciente
     */
    @PostMapping("/login-paciente")
    @Operation(summary = "Login de paciente", description = "Autentica paciente por datos personales, crea si no existe")
    public ResponseEntity<?> loginPaciente(@Valid @RequestBody PacienteLoginRequest request) {
        try {
            return ResponseEntity.ok(authService.loginPaciente(request));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
