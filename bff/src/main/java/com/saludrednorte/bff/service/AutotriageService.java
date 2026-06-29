package com.saludrednorte.bff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.saludrednorte.bff.web.dto.AutotriageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Servicio de autotriage que coordina el cálculo de prioridad con
 * ms-optimizacion y el registro en lista de espera con ms-gestionpacientes.
 */
@Service
public class AutotriageService {

    private static final Logger log = LoggerFactory.getLogger(AutotriageService.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final WebClient downstream;
    private final ObjectMapper mapper;

    public AutotriageService(WebClient downstreamWebClient, ObjectMapper mapper) {
        this.downstream = downstreamWebClient;
        this.mapper = mapper;
    }

    /**
     * Procesa una solicitud de autotriage: calcula prioridad del paciente
     * y lo registra en la lista de espera.
     *
     * @param request            datos del autotriage (paciente, gravedad, ubicación)
     * @param authorizationHeader token JWT opcional para autenticar las llamadas downstream
     * @return JSON combinado con resultado de prioridad y registro en lista de espera
     */
    public JsonNode procesar(AutotriageRequest request, String authorizationHeader) {
        ObjectNode root = mapper.createObjectNode();

        // Llamar a optimizacion para calcular prioridad
        try {
            WebClient.RequestHeadersSpec<?> call = downstream.get()
                    .uri(uriBuilder -> uriBuilder.path("/optimizacion/prioridad")
                            .queryParam("gravedad", request.getGravedad())
                            .queryParam("distanciaKm",  (double) Math.round( calcularDistanceKm(request.getLat(), request.getLon()) * 100.0)/100.0)
                            .queryParam("diasEspera", 0)
                            .build());

            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                call = call.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
            }

            JsonNode prioridad = call.retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(TIMEOUT)
                    .blockOptional()
                    .orElse(mapper.createObjectNode().put("nivel","DESCONOCIDO"));

            root.set("prioridad", prioridad);
        } catch (Exception e) {
            log.warn("Fallo al calcular prioridad", e);
            root.put("prioridadError", e.getMessage());
        }

        // Llamar a lista-espera para persistir
        try {
            ObjectNode listaReq = mapper.createObjectNode();
            listaReq.put("pacienteId", request.getPacienteId());
            listaReq.put("interconsulta", request.getSintomas());
            listaReq.put("gravedad", request.getGravedad());

            WebClient.RequestBodySpec post = downstream.post().uri("/lista-espera");
            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                post = post.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
            }
            JsonNode listaResp = post.bodyValue(listaReq)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(TIMEOUT)
                    .blockOptional()
                    .orElse(mapper.createObjectNode());

            root.set("listaEspera", listaResp);
        } catch (Exception e) {
            log.warn("Fallo al agregar a lista de espera", e);
            root.put("listaError", e.getMessage());
        }

        root.put("status","ok");
        return root;
    }

    // Stub: calcular distancia en km desde lat/lon; por ahora retorna 0.
    private double calcularDistanceKm(double lat, double lon) {
        return 0.0;
    }
}
