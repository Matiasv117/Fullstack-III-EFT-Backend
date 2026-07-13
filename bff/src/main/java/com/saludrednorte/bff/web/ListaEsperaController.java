package com.saludrednorte.bff.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.bff.service.ProgresoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador de lista de espera que delega las operaciones al microservicio ms-gestionpacientes.
 */
@Tag(name = "Lista de Espera", description = "Operaciones de lista de espera")
@RestController
@RequestMapping("/api/lista-espera")
public class ListaEsperaController {

    private static final Logger log = LoggerFactory.getLogger(ListaEsperaController.class);

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ProgresoService progresoService;

    @Autowired
    private ObjectMapper mapper;

    private static final String MS_GESTION_PACIENTES_URL = "lb://ms-listas-espera";

    private WebClient getWebClient() {
        return webClientBuilder.build();
    }

    /**
     * Obtener lista de espera.
     */
    @GetMapping
    public ResponseEntity<?> obtenerListaEspera(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.get()
                    .uri(MS_GESTION_PACIENTES_URL + "/lista-espera")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener lista de espera"));
        }
    }

    /**
     * Obtener métricas de lista de espera.
     */
    @GetMapping("/metricas")
    public ResponseEntity<?> obtenerMetricas(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.get()
                    .uri(MS_GESTION_PACIENTES_URL + "/lista-espera/metricas")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener métricas"));
        }
    }

    /**
     * Agregar paciente a lista de espera.
     */
    @PostMapping
    public ResponseEntity<?> agregarAListaEspera(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.post()
                    .uri(MS_GESTION_PACIENTES_URL + "/lista-espera")
                    .header("Authorization", token)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            try {
                JsonNode node = mapper.readTree(response);
                JsonNode pacienteNode = node.get("paciente");
                if (pacienteNode != null && pacienteNode.has("id") && !pacienteNode.get("id").isNull()) {
                    Long pacienteId = pacienteNode.get("id").asLong();
                    CompletableFuture.runAsync(() -> progresoService.actualizarProgreso(pacienteId, "EN_LISTA_ACTIVA"));
                }
            } catch (Exception ex) {
                log.warn("Error en after-call de progreso al agregar a lista de espera", ex);
            }
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al agregar a lista de espera"));
        }
    }

    /**
     * Eliminar de lista de espera.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarDeListaEspera(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.delete()
                    .uri(MS_GESTION_PACIENTES_URL + "/lista-espera/" + id)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al eliminar de lista de espera"));
        }
    }

    /**
     * Actualizar estado en lista de espera.
     */
    @PutMapping("/{id}/estado/{nuevoEstado}")
    public ResponseEntity<?> actualizarEstadoListaEspera(
            @PathVariable Long id,
            @PathVariable String nuevoEstado,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.put()
                    .uri(MS_GESTION_PACIENTES_URL + "/lista-espera/" + id + "/estado/" + nuevoEstado)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al actualizar estado"));
        }
    }

    /**
     * Obtener pacientes por estado.
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> obtenerPorEstado(
            @PathVariable String estado,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.get()
                    .uri(MS_GESTION_PACIENTES_URL + "/lista-espera/estado/" + estado)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener por estado"));
        }
    }

    /**
     * Obtener pacientes por gravedad.
     */
    @GetMapping("/gravedad/{gravedad}")
    public ResponseEntity<?> obtenerPorGravedad(
            @PathVariable String gravedad,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.get()
                    .uri(MS_GESTION_PACIENTES_URL + "/lista-espera/gravedad/" + gravedad)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener por gravedad"));
        }
    }
}
