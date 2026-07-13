package com.saludrednorte.bff.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.bff.service.AuditoriaService;
import com.saludrednorte.bff.service.ProgresoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador de pacientes que delega las operaciones al microservicio ms-gestionpacientes.
 */
@Tag(name = "Pacientes", description = "Operaciones de pacientes")
@RestController
@RequestMapping("/api/pacientes")
public class PacientesController {

    private static final Logger log = LoggerFactory.getLogger(PacientesController.class);

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ProgresoService progresoService;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private ObjectMapper mapper;

    private static final String MS_GESTION_PACIENTES_URL = "lb://ms-listas-espera";

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "funcionario";
    }

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
            try {
                JsonNode node = mapper.readTree(response);
                Long pacienteId = node.get("id").asLong();
                String nombre = node.has("nombre") ? node.get("nombre").asText() : "";
                CompletableFuture.runAsync(() -> progresoService.registrarProgreso(pacienteId, "SINTOMAS_REGISTRADOS"));
                auditoriaService.registrarEvento(obtenerUsuarioActual(), "PACIENTE_REGISTRADO", "Paciente " + nombre + " (ID " + pacienteId + ")");
            } catch (Exception ex) {
                log.warn("Error en after-call de progreso/auditoría al registrar paciente", ex);
            }
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
