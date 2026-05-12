package com.saludrednorte.bff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;

class PortalResumenServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void construirResumen_integraPacientesYNotificaciones() throws Exception {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        JsonNode pacientes = objectMapper.readTree("[{\"id\":1,\"nombre\":\"Ana\"}]");
        JsonNode notificaciones = objectMapper.readTree("[{\"id\":10,\"mensaje\":\"Aviso\"}]");

        when(webClient.get().uri("/pacientes").retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(pacientes));
        when(webClient.get().uri("/api/notificaciones/pendientes").retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(notificaciones));

        PortalResumenService service = new PortalResumenService(webClient, objectMapper);
        JsonNode resumen = service.construirResumen();

        assertThat(resumen.get("resumen").get("totalPacientes").asInt()).isEqualTo(1);
        assertThat(resumen.get("resumen").get("totalNotificacionesPendientes").asInt()).isEqualTo(1);
        assertThat(resumen.get("errores")).isEmpty();
        assertThat(resumen.get("pacientes")).hasSize(1);
        assertThat(resumen.get("notificacionesPendientes")).hasSize(1);
    }

    @Test
    void construirResumen_degradaCuandoFallaNotificaciones() throws Exception {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        JsonNode pacientes = objectMapper.readTree("[{\"id\":1,\"nombre\":\"Ana\"}]");

        when(webClient.get().uri("/pacientes").retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(pacientes));
        when(webClient.get().uri("/api/notificaciones/pendientes").retrieve().bodyToMono(JsonNode.class))
                .thenThrow(new IllegalStateException("caída temporal"));

        PortalResumenService service = new PortalResumenService(webClient, objectMapper);
        JsonNode resumen = service.construirResumen();

        assertThat(resumen.get("resumen").get("totalPacientes").asInt()).isEqualTo(1);
        assertThat(resumen.get("resumen").get("totalNotificacionesPendientes").asInt()).isEqualTo(0);
        assertThat(resumen.get("errores")).hasSize(1);
        assertThat(resumen.get("notificacionesPendientes")).isEmpty();
    }
}

