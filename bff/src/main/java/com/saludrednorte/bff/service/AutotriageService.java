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
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio de autotriage que coordina el cálculo de prioridad con
 * ms-optimizacion y el registro en lista de espera con ms-gestionpacientes.
 */
@Service
public class AutotriageService {

    private static final Logger log = LoggerFactory.getLogger(AutotriageService.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String MS_OPTIMIZACION = "lb://ms-optimizacion";
    private static final String MS_LISTAS_ESPERA = "lb://ms-listas-espera";

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper mapper;
    private final ProgresoService progresoService;

    public AutotriageService(@LoadBalanced WebClient.Builder webClientBuilder, ObjectMapper mapper, ProgresoService progresoService) {
        this.webClientBuilder = webClientBuilder;
        this.mapper = mapper;
        this.progresoService = progresoService;
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
            WebClient client = webClientBuilder.build();
            double distanciaKm = calcularDistanceKm(request.getLat(), request.getLon());
            WebClient.RequestHeadersSpec<?> call = client.get()
                    .uri(MS_OPTIMIZACION + "/optimizacion/prioridad?gravedad=" + request.getGravedad()
                            + "&distanciaKm=" + distanciaKm + "&diasEspera=0");

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
            WebClient client = webClientBuilder.build();
            ObjectNode pacienteNode = mapper.createObjectNode();
            pacienteNode.put("id", request.getPacienteId());
            ObjectNode listaReq = mapper.createObjectNode();
            listaReq.set("paciente", pacienteNode);
            listaReq.put("interconsulta", request.getSintomas());
            listaReq.put("gravedad", mapearGravedad(request.getGravedad()));

            WebClient.RequestBodySpec post = client.post().uri(MS_LISTAS_ESPERA + "/lista-espera");
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

        // Registrar progreso: EVALUANDO_PRIORIDAD + EN_LISTA_ACTIVA (best-effort)
        Long pid = request.getPacienteId();
        CompletableFuture.runAsync(() -> {
            progresoService.registrarProgreso(pid, "EVALUANDO_PRIORIDAD");
            progresoService.actualizarProgreso(pid, "EN_LISTA_ACTIVA");
        });

        return root;
    }

    private String mapearGravedad(int gravedad) {
        if (gravedad >= 4) return "ALTA";
        if (gravedad >= 2) return "MEDIA";
        return "BAJA";
    }

    private double calcularDistanceKm(double lat, double lon) {
        // Coordenadas del hospital (centro de la ciudad)
        double latHospital = -33.4489;
        double lonHospital = -70.6693;
        double radioTierra = 6371.0;
        double dLat = Math.toRadians(lat - latHospital);
        double dLon = Math.toRadians(lon - lonHospital);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(latHospital)) * Math.cos(Math.toRadians(lat)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(radioTierra * c * 100.0) / 100.0;
    }
}
