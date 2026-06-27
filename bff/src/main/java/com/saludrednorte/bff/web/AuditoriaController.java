package com.saludrednorte.bff.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String MS_AUDITORIA_URL = "http://localhost:8088";

    private WebClient getWebClient() {
        return webClientBuilder.build();
    }

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
