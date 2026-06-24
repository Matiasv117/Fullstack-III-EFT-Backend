package com.saludrednorte.bff.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Controlador de administración que delega las operaciones al microservicio ms-auth.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String MS_AUTH_URL = "http://localhost:8087";

    private WebClient getWebClient() {
        return webClientBuilder.build();
    }

    /**
     * Listar todos los funcionarios y administradores.
     */
    @GetMapping("/funcionarios")
    public ResponseEntity<?> listarFuncionarios(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.get()
                    .uri(MS_AUTH_URL + "/api/admin/funcionarios")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al listar funcionarios"));
        }
    }

    /**
     * Crear un nuevo funcionario o administrador.
     */
    @PostMapping("/funcionarios")
    public ResponseEntity<?> crearFuncionario(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.post()
                    .uri(MS_AUTH_URL + "/api/admin/funcionarios")
                    .header("Authorization", token)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al crear funcionario"));
        }
    }

    /**
     * Modificar un funcionario existente.
     */
    @PutMapping("/funcionarios/{id}")
    public ResponseEntity<?> modificarFuncionario(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.put()
                    .uri(MS_AUTH_URL + "/api/admin/funcionarios/" + id)
                    .header("Authorization", token)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al modificar funcionario"));
        }
    }

    /**
     * Eliminar un funcionario.
     */
    @DeleteMapping("/funcionarios/{id}")
    public ResponseEntity<?> eliminarFuncionario(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.delete()
                    .uri(MS_AUTH_URL + "/api/admin/funcionarios/" + id)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al eliminar funcionario"));
        }
    }

    /**
     * Cambiar el estado (activo/inactivo) de un funcionario.
     */
    @PutMapping("/funcionarios/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.put()
                    .uri(MS_AUTH_URL + "/api/admin/funcionarios/" + id + "/estado")
                    .header("Authorization", token)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al cambiar estado"));
        }
    }

    /**
     * Cambiar el rol de un funcionario.
     */
    @PutMapping("/funcionarios/{id}/rol")
    public ResponseEntity<?> cambiarRol(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.put()
                    .uri(MS_AUTH_URL + "/api/admin/funcionarios/" + id + "/rol")
                    .header("Authorization", token)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al cambiar rol"));
        }
    }
}
