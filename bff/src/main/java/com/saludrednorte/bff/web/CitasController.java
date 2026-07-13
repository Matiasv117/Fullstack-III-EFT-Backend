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

@Tag(name = "Citas", description = "Operaciones de citas médicas")
@RestController
@RequestMapping("/api/citas")
public class CitasController {

    private static final Logger log = LoggerFactory.getLogger(CitasController.class);

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ProgresoService progresoService;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private ObjectMapper mapper;

    private static final String MS_OPTIMIZACION_URL = "lb://ms-optimizacion";

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "funcionario";
    }

    private WebClient getWebClient() {
        return webClientBuilder.build();
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> obtenerCitasPorEstado(
            @PathVariable String estado,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String response = getWebClient().get()
                    .uri(MS_OPTIMIZACION_URL + "/citas/estado/" + estado)
                    .header("Authorization", token != null ? token : "")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            log.error("WebClient error al obtener citas por estado: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error al obtener citas por estado", e);
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener citas por estado: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerTodasCitas(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String response = getWebClient().get()
                    .uri(MS_OPTIMIZACION_URL + "/citas")
                    .header("Authorization", token != null ? token : "")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            log.error("WebClient error al obtener citas: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error al obtener citas", e);
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener citas: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> crearCita(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String response = getWebClient().post()
                    .uri(MS_OPTIMIZACION_URL + "/citas")
                    .header("Authorization", token != null ? token : "")
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            try {
                JsonNode node = mapper.readTree(response);
                Long pacienteId = node.get("pacienteId").asLong();
                CompletableFuture.runAsync(() -> progresoService.actualizarProgreso(pacienteId, "CITA_ASIGNADA"));
                auditoriaService.registrarEvento(obtenerUsuarioActual(), "CITA_OPTIMIZADA", "Cita creada para paciente " + pacienteId);
            } catch (Exception ex) {
                log.warn("Error en after-call de progreso/auditoría al crear cita", ex);
            }
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            log.error("WebClient error al crear cita: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error al crear cita", e);
            return ResponseEntity.status(500).body(Map.of("error", "Error al crear cita: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarCita(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String response = getWebClient().delete()
                    .uri(MS_OPTIMIZACION_URL + "/citas/{id}", id)
                    .header("Authorization", token != null ? token : "")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            log.error("WebClient error al cancelar cita: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error al cancelar cita", e);
            return ResponseEntity.status(500).body(Map.of("error", "Error al cancelar cita: " + e.getMessage()));
        }
    }
}
