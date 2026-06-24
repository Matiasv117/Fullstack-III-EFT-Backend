package com.saludrednorte.ms_auth.controller;

import com.saludrednorte.ms_auth.entity.User;
import com.saludrednorte.ms_auth.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para administración del sistema.
 * <p>
 * Proporciona endpoints exclusivos para administradores para gestionar
 * usuarios funcionarios (crear, modificar, eliminar, listar).
 * Todos los endpoints requieren rol ADMIN.
 * </p>
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administración", description = "API exclusiva para administradores - Gestión de funcionarios")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Lista todos los funcionarios del sistema (excluye admin).
     *
     * @param authorization token JWT del admin
     * @return lista de funcionarios
     */
    @GetMapping("/funcionarios")
    @Operation(summary = "Listar funcionarios", description = "Obtiene la lista de todos los funcionarios del sistema (excluye admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de funcionarios obtenida"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Solo administradores pueden acceder")
    })
    public ResponseEntity<?> listarFuncionarios(
            @Parameter(description = "Token JWT del admin") @RequestHeader("Authorization") String authorization) {
        try {
            // Verificar que sea admin
            if (!esAdmin(authorization)) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo administradores pueden acceder"));
            }

            List<User> funcionarios = userRepository.findAll().stream()
                    .filter(u -> !"ROLE_ADMIN".equals(u.getRole()))
                    .toList();

            return ResponseEntity.ok(funcionarios);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Error de autenticación"));
        }
    }

    /**
     * Crea un nuevo funcionario.
     *
     * @param authorization token JWT del admin
     * @param request datos del nuevo funcionario
     * @return funcionario creado
     */
    @PostMapping("/funcionarios")
    @Operation(summary = "Crear funcionario", description = "Crea un nuevo funcionario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Funcionario creado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Solo administradores pueden acceder"),
            @ApiResponse(responseCode = "409", description = "El username ya existe")
    })
    public ResponseEntity<?> crearFuncionario(
            @Parameter(description = "Token JWT del admin") @RequestHeader("Authorization") String authorization,
            @RequestBody FuncionarioRequest request) {
        try {
            // Verificar que sea admin
            if (!esAdmin(authorization)) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo administradores pueden acceder"));
            }

            // Verificar que el username no exista
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.status(409).body(Map.of("error", "El username ya existe"));
            }

            // Crear funcionario
            User funcionario = new User(
                    request.getUsername(),
                    passwordEncoder.encode(request.getPassword()),
                    "ROLE_FUNCIONARIO"
            );

            User guardado = userRepository.save(funcionario);
            return ResponseEntity.status(201).body(guardado);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Error de autenticación"));
        }
    }

    /**
     * Modifica un funcionario existente.
     *
     * @param authorization token JWT del admin
     * @param id ID del funcionario a modificar
     * @param request nuevos datos del funcionario
     * @return funcionario modificado
     */
    @PutMapping("/funcionarios/{id}")
    @Operation(summary = "Modificar funcionario", description = "Modifica los datos de un funcionario existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funcionario modificado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Solo administradores pueden acceder"),
            @ApiResponse(responseCode = "404", description = "Funcionario no encontrado")
    })
    public ResponseEntity<?> modificarFuncionario(
            @Parameter(description = "Token JWT del admin") @RequestHeader("Authorization") String authorization,
            @Parameter(description = "ID del funcionario") @PathVariable Long id,
            @RequestBody FuncionarioRequest request) {
        try {
            // Verificar que sea admin
            if (!esAdmin(authorization)) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo administradores pueden acceder"));
            }

            Optional<User> funcionarioOpt = userRepository.findById(id);
            if (funcionarioOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Funcionario no encontrado"));
            }

            User funcionario = funcionarioOpt.get();

            // No permitir modificar admin
            if ("ROLE_ADMIN".equals(funcionario.getRole())) {
                return ResponseEntity.status(403).body(Map.of("error", "No se puede modificar al administrador principal"));
            }

            // Actualizar datos
            if (request.getUsername() != null && !request.getUsername().isEmpty()) {
                funcionario.setUsername(request.getUsername());
            }
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                funcionario.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            User guardado = userRepository.save(funcionario);
            return ResponseEntity.ok(guardado);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Error de autenticación"));
        }
    }

    /**
     * Elimina un funcionario.
     *
     * @param authorization token JWT del admin
     * @param id ID del funcionario a eliminar
     * @return respuesta de confirmación
     */
    @DeleteMapping("/funcionarios/{id}")
    @Operation(summary = "Eliminar funcionario", description = "Elimina un funcionario del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funcionario eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Solo administradores pueden acceder"),
            @ApiResponse(responseCode = "404", description = "Funcionario no encontrado")
    })
    public ResponseEntity<?> eliminarFuncionario(
            @Parameter(description = "Token JWT del admin") @RequestHeader("Authorization") String authorization,
            @Parameter(description = "ID del funcionario") @PathVariable Long id) {
        try {
            // Verificar que sea admin
            if (!esAdmin(authorization)) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo administradores pueden acceder"));
            }

            Optional<User> funcionarioOpt = userRepository.findById(id);
            if (funcionarioOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Funcionario no encontrado"));
            }

            User funcionario = funcionarioOpt.get();

            // No permitir eliminar admin
            if ("ROLE_ADMIN".equals(funcionario.getRole())) {
                return ResponseEntity.status(403).body(Map.of("error", "No se puede eliminar al administrador principal"));
            }

            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("mensaje", "Funcionario eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Error de autenticación"));
        }
    }

    /**
     * Verifica si el token pertenece a un administrador.
     *
     * @param authorization header Authorization
     * @return true si es admin, false en caso contrario
     */
    private boolean esAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return false;
        }

        try {
            String token = authorization.substring(7);
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return false;
            }

            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            String sub = payload.substring(payload.indexOf("\"sub\":\"") + 7);
            sub = sub.substring(0, sub.indexOf("\""));

            return "admin".equals(sub);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * DTO para la creación/modificación de funcionarios.
     */
    public static class FuncionarioRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
