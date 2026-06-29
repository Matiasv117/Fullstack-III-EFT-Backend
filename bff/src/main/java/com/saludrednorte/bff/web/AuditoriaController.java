package com.saludrednorte.bff.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Controlador para la consulta de eventos de auditoría.
 * Actúa como proxy hacia ms-auditoría para el frontend.
 */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String MS_AUDITORIA_URL = "lb://ms-auditoria";

    private WebClient getWebClient() {
        return webClientBuilder.build();
    }

    /**
     * Lista los eventos de auditoría registrados en el sistema.
     *
     * @param token token JWT para autenticación contra ms-auditoria
     * @return lista de eventos de auditoría
     */
    @GetMapping("/eventos")
    public ResponseEntity<?> listarEventos(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            WebClient webClient = getWebClient();
            String response = webClient.get()
                    .uri(MS_AUDITORIA_URL + "/api/auditoria/eventos")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al listar eventos de auditoría"));
        }
    }
}
