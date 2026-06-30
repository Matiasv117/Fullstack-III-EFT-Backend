package com.saludrednorte.bff.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
public class ProgresoService {

    private static final Logger log = LoggerFactory.getLogger(ProgresoService.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient webClient;
    private final ObjectMapper mapper;

    public ProgresoService(WebClient.Builder webClientBuilder, ObjectMapper mapper) {
        this.webClient = webClientBuilder.build();
        this.mapper = mapper;
    }

    public void registrarProgreso(Long pacienteId, String estado) {
        try {
            Map<String, String> body = Map.of("estado", estado);
            webClient.post()
                    .uri("lb://ms-progreso/progreso/pacientes/{id}", pacienteId)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block(TIMEOUT);
            log.debug("Progreso registrado: paciente={}, estado={}", pacienteId, estado);
        } catch (Exception e) {
            log.warn("No se pudo registrar progreso para paciente {}: {}", pacienteId, e.getMessage());
        }
    }

    public void actualizarProgreso(Long pacienteId, String estado) {
        try {
            Map<String, String> body = Map.of("estado", estado);
            webClient.put()
                    .uri("lb://ms-progreso/progreso/pacientes/{id}", pacienteId)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block(TIMEOUT);
            log.debug("Progreso actualizado: paciente={}, estado={}", pacienteId, estado);
        } catch (Exception e) {
            log.warn("No se pudo actualizar progreso para paciente {}: {}", pacienteId, e.getMessage());
        }
    }
}
