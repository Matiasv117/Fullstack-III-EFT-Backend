package com.saludrednorte.ms_auth.controller;

import com.saludrednorte.ms_auth.entity.User;
import com.saludrednorte.ms_auth.dto.UserDTO;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador REST para administración del sistema.
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
     */
    @GetMapping("/funcionarios")
    @Operation(summary = "Listar funcionarios", description = "Obtiene la lista de todos los funcionarios del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de funcionarios obtenida"),
            @ApiResponse(responseCode = "403", description = "Solo administradores pueden acceder")
    })
    public ResponseEntity<?> listarFuncionarios(
            @Parameter(description = "Token JWT del admin") @RequestHeader("Authorization") String authorization) {
        try {
            if (!esAdmin(authorization)) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo administradores pueden acceder"));
            }

            List<UserDTO> funcionarios = userRepository.findAll().stream()
                    .filter(u -> !"ROLE_ADMIN".equals(u.getRole()))
                    .map(UserDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(funcionarios);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Error de autenticación"));
        }
    }

    /**
     * Crea un nuevo funcionario.
     */
    @PostMapping("/funcionarios")
    @Operation(summary = "Crear funcionario", description = "Crea un nuevo funcionario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Funcionario creado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Solo administradores pueden acceder"),
            @ApiResponse(responseCode = "409", description = "El username ya existe")
    })
    public ResponseEntity<?> crearFuncionario(
            @Parameter(description = "Token JWT del admin") @RequestHeader("Authorization") String authorization,
            @RequestBody FuncionarioRequest request) {
        try {
            if (!esAdmin(authorization)) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo administradores pueden acceder"));
            }

            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.status(409).body(Map.of("error", "El username ya existe"));
            }

            User funcionario = new User(
                    request.getUsername(),
                    passwordEncoder.encode(request.getPassword()),
                    "ROLE_FUNCIONARIO"
            );
            funcionario.setNombreCompleto(request.getNombreCompleto());
            funcionario.setEmail(request.getEmail());
            funcionario.setCreadoPor(extraerUsernameDelToken(authorization));
            funcionario.setActivo(true);

            User guardado = userRepository.save(funcionario);
            return ResponseEntity.status(201).body(new UserDTO(guardado));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Error de autenticación"));
        }
    }

    /**
     * Modifica un funcionario existente.
     */
    @PutMapping("/funcionarios/{id}")
    @Operation(summary = "Modificar funcionario", description = "Modifica los datos de un funcionario existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funcionario modificado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Solo administradores pueden acceder"),
            @ApiResponse(responseCode = "404", description = "Funcionario no encontrado")
    })
    public ResponseEntity<?> modificarFuncionario(
            @Parameter(description = "Token JWT del admin") @RequestHeader("Authorization") String authorization,
            @Parameter(description = "ID del funcionario") @PathVariable Long id,
            @RequestBody FuncionarioRequest request) {
        try {
            if (!esAdmin(authorization)) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo administradores pueden acceder"));
            }

            Optional<User> funcionarioOpt = userRepository.findById(id);
            if (funcionarioOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Funcionario no encontrado"));
            }

            User funcionario = funcionarioOpt.get();

            if ("ROLE_ADMIN".equals(funcionario.getRole())) {
                return ResponseEntity.status(403).body(Map.of("error", "No se puede modificar al administrador principal"));
            }

            if (request.getUsername() != null && !request.getUsername().isEmpty()) {
                funcionario.setUsername(request.getUsername());
            }
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                funcionario.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            if (request.getNombreCompleto() != null && !request.getNombreCompleto().isEmpty()) {
                funcionario.setNombreCompleto(request.getNombreCompleto());
            }
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                funcionario.setEmail(request.getEmail());
            }
            
            funcionario.setFechaUltimaModificacion(LocalDateTime.now());
            User guardado = userRepository.save(funcionario);
            return ResponseEntity.ok(new UserDTO(guardado));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Error de autenticación"));
        }
    }

    /**
     * Elimina un funcionario.
     */
    @DeleteMapping("/funcionarios/{id}")
    @Operation(summary = "Eliminar funcionario", description = "Elimina un funcionario del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funcionario eliminado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Solo administradores pueden acceder"),
            @ApiResponse(responseCode = "404", description = "Funcionario no encontrado")
    })
    public ResponseEntity<?> eliminarFuncionario(
            @Parameter(description = "Token JWT del admin") @RequestHeader("Authorization") String authorization,
            @Parameter(description = "ID del funcionario") @PathVariable Long id) {
        try {
            if (!esAdmin(authorization)) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo administradores pueden acceder"));
            }

            Optional<User> funcionarioOpt = userRepository.findById(id);
            if (funcionarioOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Funcionario no encontrado"));
            }

            User funcionario = funcionarioOpt.get();

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
     * Cambia el estado activo/inactivo de un usuario.
     */
    @PutMapping("/funcionarios/{id}/estado")
    @Operation(summary = "Cambiar estado del usuario", description = "Activa o desactiva un usuario")
    public ResponseEntity<?> cambiarEstado(
            @Parameter(description = "Token JWT del admin") @RequestHeader("Authorization") String authorization,
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        try {
            if (!esAdmin(authorization)) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo administradores pueden acceder"));
            }

            Optional<User> usuarioOpt = userRepository.findById(id);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            }

            User usuario = usuarioOpt.get();
            usuario.setActivo(request.get("activo"));
            usuario.setFechaUltimaModificacion(LocalDateTime.now());
            User guardado = userRepository.save(usuario);
            return ResponseEntity.ok(new UserDTO(guardado));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Error de autenticación"));
        }
    }

    /**
     * Cambia el rol de un usuario (promoción/degradación).
     */
    @PutMapping("/funcionarios/{id}/rol")
    @Operation(summary = "Cambiar rol del usuario", description = "Cambia el rol de un usuario entre funcionario y admin")
    public ResponseEntity<?> cambiarRol(
            @Parameter(description = "Token JWT del admin") @RequestHeader("Authorization") String authorization,
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            if (!esAdmin(authorization)) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo administradores pueden acceder"));
            }

            String nuevoRol = request.get("rol");
            if (!nuevoRol.equals("ROLE_FUNCIONARIO") && !nuevoRol.equals("ROLE_ADMIN")) {
                return ResponseEntity.status(400).body(Map.of("error", "Rol inválido"));
            }

            Optional<User> usuarioOpt = userRepository.findById(id);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            }

            User usuario = usuarioOpt.get();
            usuario.setRole(nuevoRol);
            usuario.setFechaUltimaModificacion(LocalDateTime.now());
            User guardado = userRepository.save(usuario);
            return ResponseEntity.ok(new UserDTO(guardado));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Error de autenticación"));
        }
    }

    /**
     * Verifica si el token pertenece a un administrador.
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
     * Extrae el username del token JWT.
     */
    private String extraerUsernameDelToken(String authorization) {
        try {
            String token = authorization.substring(7);
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return "sistema";
            }

            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            String sub = payload.substring(payload.indexOf("\"sub\":\"") + 7);
            return sub.substring(0, sub.indexOf("\""));
        } catch (Exception e) {
            return "sistema";
        }
    }

    /**
     * DTO para la creación/modificación de funcionarios.
     */
    public static class FuncionarioRequest {
        private String username;
        private String password;
        private String nombreCompleto;
        private String email;

        // Getters y Setters
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

        public String getNombreCompleto() {
            return nombreCompleto;
        }

        public void setNombreCompleto(String nombreCompleto) {
            this.nombreCompleto = nombreCompleto;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
