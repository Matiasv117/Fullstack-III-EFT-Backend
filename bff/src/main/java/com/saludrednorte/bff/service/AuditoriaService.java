package com.saludrednorte.bff.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient webClient;

    public AuditoriaService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public void registrarEvento(String username, String action, String details) {
        try {
            Map<String, String> body = Map.of(
                    "username", username != null ? username : "anonimo",
                    "action", action,
                    "details", details != null ? details : ""
            );
            webClient.post()
                    .uri("lb://ms-auditoria/api/auditoria/eventos")
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block(TIMEOUT);
            log.debug("Evento auditado: usuario={}, accion={}", username, action);
        } catch (Exception e) {
            log.warn("No se pudo auditar evento: {}", e.getMessage());
        }
    }
}
