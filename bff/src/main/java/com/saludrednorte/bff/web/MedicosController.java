package com.saludrednorte.bff.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@RestController
@RequestMapping("/api/medicos")
public class MedicosController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String MS_OPTIMIZACION_URL = "lb://ms-optimizacion";

    private WebClient getWebClient() {
        return webClientBuilder.build();
    }

    @GetMapping
    public ResponseEntity<?> listarMedicos(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String response = getWebClient().get()
                    .uri(MS_OPTIMIZACION_URL + "/medicos")
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al listar médicos"));
        }
    }
}
