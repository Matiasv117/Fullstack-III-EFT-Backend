package com.saludrednorte.bff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * Agrega datos del portal de salud: pacientes + notificaciones pendientes en una sola respuesta
 * para que el frontend haga una sola petición (patrón BFF).
 */
@Service
public class PortalResumenService {

    private static final Logger log = LoggerFactory.getLogger(PortalResumenService.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(12);

    private final WebClient downstream;
    private final ObjectMapper objectMapper;

    public PortalResumenService(WebClient downstreamWebClient, ObjectMapper objectMapper) {
        this.downstream = downstreamWebClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Construye el resumen del portal sin token de autorización.
     *
     * @return JSON con pacientes, notificaciones pendientes y resumen
     */
    public JsonNode construirResumen() {
        return construirResumen(null);
    }

    /**
     * Construye el resumen del portal con datos agregados de pacientes
     * y notificaciones pendientes, autenticando las llamadas downstream
     * con el token proporcionado.
     *
     * @param authorizationHeader token JWT para autenticar llamadas a microservicios
     * @return JSON con pacientes, notificaciones pendientes y resumen numérico
     */
    public JsonNode construirResumen(String authorizationHeader) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode errores = root.putArray("errores");

        JsonNode pacientes = fetchPacientes(errores, authorizationHeader);
        JsonNode notificaciones = fetchNotificacionesPendientes(errores, authorizationHeader);

        root.set("pacientes", pacientes);
        root.set("notificacionesPendientes", notificaciones);

        int nPacientes = pacientes.isArray() ? pacientes.size() : 0;
        int nNotif = notificaciones.isArray() ? notificaciones.size() : 0;
        ObjectNode resumen = root.putObject("resumen");
        resumen.put("totalPacientes", nPacientes);
        resumen.put("totalNotificacionesPendientes", nNotif);

        return root;
    }

    private JsonNode fetchPacientes(ArrayNode errores, String authorizationHeader) {
        try {
            WebClient.RequestHeadersSpec<?> request = downstream.get().uri("/pacientes");
            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                request = request.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
            }
            return request.retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(TIMEOUT)
                    .blockOptional()
                    .orElseGet(() -> emptyArray("pacientes vacío"));
        } catch (WebClientResponseException e) {
            log.warn("Fallo al obtener pacientes: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            errores.add("pacientes: " + e.getStatusCode());
            return emptyArray("error");
        } catch (Exception e) {
            log.warn("Fallo al obtener pacientes", e);
            errores.add("pacientes: " + e.getMessage());
            return emptyArray("error");
        }
    }

    private JsonNode fetchNotificacionesPendientes(ArrayNode errores, String authorizationHeader) {
        try {
            WebClient.RequestHeadersSpec<?> request = downstream.get().uri("/api/notificaciones/pendientes");
            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                request = request.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
            }
            return request.retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(TIMEOUT)
                    .blockOptional()
                    .orElseGet(() -> emptyArray("notificaciones vacío"));
        } catch (WebClientResponseException e) {
            log.warn("Fallo al obtener notificaciones: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            errores.add("notificaciones: " + e.getStatusCode());
            return emptyArray("error");
        } catch (Exception e) {
            log.warn("Fallo al obtener notificaciones", e);
            errores.add("notificaciones: " + e.getMessage());
            return emptyArray("error");
        }
    }

    private ArrayNode emptyArray(String ignored) {
        return objectMapper.createArrayNode();
    }
}
