package com.saludrednorte.bff.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Controlador de notificaciones que delega las operaciones al microservicio ms-notificaciones.
 */
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionesController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String MS_NOTIFICACIONES_URL = "lb://ms-notificaciones";

    private WebClient getWebClient() {
        return webClientBuilder.build();
    }

    /**
     * Obtener notificaciones pendientes.
     */
    @GetMapping("/pendientes")
    public ResponseEntity<?> obtenerNotificacionesPendientes(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.get()
                    .uri(MS_NOTIFICACIONES_URL + "/api/notificaciones/pendientes")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener notificaciones pendientes"));
        }
    }

    /**
     * Enviar notificación.
     */
    @PostMapping("/{id}/enviar")
    public ResponseEntity<?> enviarNotificacion(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.post()
                    .uri(MS_NOTIFICACIONES_URL + "/api/notificaciones/" + id + "/enviar")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al enviar notificación"));
        }
    }

    /**
     * Enviar notificación por canal.
     */
    @PostMapping("/{id}/enviar-canal")
    public ResponseEntity<?> enviarNotificacionPorCanal(
            @PathVariable Long id,
            @RequestParam String canal,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.post()
                    .uri(MS_NOTIFICACIONES_URL + "/api/notificaciones/" + id + "/enviar-canal?canal=" + canal)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al enviar notificación por canal"));
        }
    }

    /**
     * Enviar todas las notificaciones.
     */
    @PostMapping("/enviar-todas")
    public ResponseEntity<?> enviarTodasLasNotificaciones(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.post()
                    .uri(MS_NOTIFICACIONES_URL + "/api/notificaciones/enviar-todas")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al enviar todas las notificaciones"));
        }
    }

    /**
     * Obtener canales disponibles.
     */
    @GetMapping("/info/canales")
    public ResponseEntity<?> obtenerCanalesDisponibles(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.get()
                    .uri(MS_NOTIFICACIONES_URL + "/api/notificaciones/info/canales")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener canales disponibles"));
        }
    }

    /**
     * Obtener estado del servicio.
     */
    @GetMapping("/info/estado")
    public ResponseEntity<?> obtenerEstadoServicio(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.get()
                    .uri(MS_NOTIFICACIONES_URL + "/api/notificaciones/info/estado")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener estado del servicio"));
        }
    }
}
