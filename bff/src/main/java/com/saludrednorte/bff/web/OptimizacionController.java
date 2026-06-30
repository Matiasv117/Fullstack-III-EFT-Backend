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

/**
 * Controlador de optimización que delega las operaciones al microservicio ms-optimizacion.
 */
@RestController
@RequestMapping("/api/optimizacion")
public class OptimizacionController {

    private static final Logger log = LoggerFactory.getLogger(OptimizacionController.class);

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

    /**
     * Obtener lista de espera optimizada.
     */
    @GetMapping("/lista-espera")
    public ResponseEntity<?> obtenerListaEsperaOptimizada(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.get()
                    .uri(MS_OPTIMIZACION_URL + "/optimizacion/lista-espera")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener lista optimizada"));
        }
    }

    /**
     * Cancelar cita con estrategia.
     */
    @PostMapping("/cancelar/{citaId}")
    public ResponseEntity<?> cancelarCitaConEstrategia(
            @PathVariable Long citaId,
            @RequestParam(defaultValue = "fifo") String estrategia,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.post()
                    .uri(MS_OPTIMIZACION_URL + "/optimizacion/cancelar/" + citaId + "?estrategia=" + estrategia)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            try {
                JsonNode node = mapper.readTree(response);
                if (node.has("pacienteId") && !node.get("pacienteId").isNull()) {
                    Long pacienteId = node.get("pacienteId").asLong();
                    CompletableFuture.runAsync(() -> progresoService.actualizarProgreso(pacienteId, "CITA_ASIGNADA"));
                    auditoriaService.registrarEvento(obtenerUsuarioActual(), "CITA_OPTIMIZADA", "Cita " + node.get("citaId") + " reasignada a paciente " + pacienteId);
                }
            } catch (Exception ex) {
                log.warn("Error en after-call de progreso/auditoría al cancelar cita", ex);
            }
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al cancelar cita"));
        }
    }

    /**
     * Calcular prioridad de paciente.
     */
    @GetMapping("/prioridad")
    public ResponseEntity<?> calcularPrioridad(
            @RequestParam int gravedad,
            @RequestParam double distanciaKm,
            @RequestParam int diasEspera,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.get()
                    .uri(MS_OPTIMIZACION_URL + "/optimizacion/prioridad?gravedad=" + gravedad + "&distanciaKm=" + distanciaKm + "&diasEspera=" + diasEspera)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al calcular prioridad"));
        }
    }
}
