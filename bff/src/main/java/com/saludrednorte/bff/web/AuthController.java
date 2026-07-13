package com.saludrednorte.bff.web;

import com.saludrednorte.bff.dto.LoginRequest;
import com.saludrednorte.bff.dto.PacienteLoginRequest;
import com.saludrednorte.bff.service.AuditoriaService;
import com.saludrednorte.bff.service.AuthProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

/**
 * Controlador de autenticación que delega las operaciones al microservicio ms-auth.
 */
@Tag(name = "Autenticacion", description = "Operaciones de autenticación y registro")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthProxyService authProxyService;

    @Autowired
    private AuditoriaService auditoriaService;

    /**
     * Endpoint de login que delega la generación del token JWT a ms-auth.
     *
     * @param loginRequest credenciales del usuario
     * @return respuesta con el token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            var response = authProxyService.login(loginRequest);
            auditoriaService.registrarEvento(loginRequest.getUsername(), "LOGIN_EXITOSO", "Login exitoso");
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException.Unauthorized ex) {
            auditoriaService.registrarEvento(loginRequest.getUsername(), "LOGIN_FALLIDO", "Credenciales inválidas");
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    /**
     * Endpoint para validar un token JWT delegando a ms-auth.
     *
     * @param token token JWT a validar
     * @return respuesta indicando si el token es válido
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(authProxyService.validateToken(token));
        } catch (WebClientResponseException.Unauthorized ex) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "error", "Token inválido o expirado"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "error", "Error al validar token"));
        }
    }

    /**
     * Endpoint privado que obtiene información del usuario autenticado vía ms-auth.
     *
     * @param token token JWT en el header Authorization
     * @return información del usuario autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser(@RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(authProxyService.getAuthenticatedUser(token));
        } catch (WebClientResponseException.Unauthorized ex) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Error al obtener usuario autenticado"));
        }
    }

    /**
     * Endpoint para login de pacientes que delega a ms-auth.
     *
     * @param request datos del paciente (nombre, apellido, RUT)
     * @return respuesta con el token JWT y datos del paciente
     */
    @PostMapping("/login-paciente")
    public ResponseEntity<?> loginPaciente(@RequestBody PacienteLoginRequest request) {
        try {
            return ResponseEntity.ok(authProxyService.loginPaciente(request));
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al procesar login de paciente"));
        }
    }
}
