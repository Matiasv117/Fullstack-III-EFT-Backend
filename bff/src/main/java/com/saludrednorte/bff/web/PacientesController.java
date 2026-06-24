package com.saludrednorte.bff.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Controlador de pacientes que delega las operaciones al microservicio ms-gestionpacientes.
 */
@RestController
@RequestMapping("/api/pacientes")
public class PacientesController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String MS_GESTION_PACIENTES_URL = "http://localhost:8083";

    private WebClient getWebClient() {
        return webClientBuilder.build();
    }

    /**
     * Listar todos los pacientes.
     */
    @GetMapping
    public ResponseEntity<?> listarPacientes(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.get()
                    .uri(MS_GESTION_PACIENTES_URL + "/pacientes")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al listar pacientes"));
        }
    }

    /**
     * Registrar un nuevo paciente.
     */
    @PostMapping
    public ResponseEntity<?> registrarPaciente(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.post()
                    .uri(MS_GESTION_PACIENTES_URL + "/pacientes")
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
            return ResponseEntity.status(500).body(Map.of("error", "Error al registrar paciente"));
        }
    }

    /**
     * Eliminar un paciente.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPaciente(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.delete()
                    .uri(MS_GESTION_PACIENTES_URL + "/pacientes/" + id)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al eliminar paciente"));
        }
    }
}
