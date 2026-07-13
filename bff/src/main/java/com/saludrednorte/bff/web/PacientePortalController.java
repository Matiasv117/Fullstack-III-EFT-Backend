package com.saludrednorte.bff.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@RestController
@RequestMapping("/api/pacientes/portal")
public class PacientePortalController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private static final String MS_GESTION_PACIENTES_URL = "lb://ms-listas-espera";

    private WebClient getWebClient() {
        return webClientBuilder.build();
    }

    @GetMapping("/mis-datos")
    public ResponseEntity<?> getMisDatos(@RequestHeader("Authorization") String authorization) {
        try {
            String response = getWebClient().get()
                    .uri(MS_GESTION_PACIENTES_URL + "/pacientes/portal/mis-datos")
                    .header("Authorization", authorization)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener datos del paciente"));
        }
    }

    @GetMapping("/mi-posicion")
    public ResponseEntity<?> getMiPosicion(@RequestHeader("Authorization") String authorization) {
        try {
            String response = getWebClient().get()
                    .uri(MS_GESTION_PACIENTES_URL + "/pacientes/portal/mi-posicion")
                    .header("Authorization", authorization)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener posición en lista de espera"));
        }
    }

    @PutMapping("/mis-datos")
    public ResponseEntity<?> actualizarMisDatos(
            @RequestHeader("Authorization") String authorization,
            @RequestBody String pacienteJson) {
        try {
            String response = getWebClient().put()
                    .uri(MS_GESTION_PACIENTES_URL + "/pacientes/portal/mis-datos")
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json")
                    .bodyValue(pacienteJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al actualizar datos del paciente"));
        }
    }

    @GetMapping("/mis-citas")
    public ResponseEntity<?> getMisCitas(@RequestHeader("Authorization") String authorization) {
        try {
            String response = getWebClient().get()
                    .uri(MS_GESTION_PACIENTES_URL + "/pacientes/portal/mis-citas")
                    .header("Authorization", authorization)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(response);
        } catch (WebClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener citas del paciente"));
        }
    }
}
