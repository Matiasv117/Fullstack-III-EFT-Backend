package com.saludrednorte.bff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import reactor.core.publisher.Mono;

class PortalResumenServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WebClient.Builder mockBuilder(WebClient webClient) {
        WebClient.Builder builder = mock(WebClient.Builder.class);
        when(builder.build()).thenReturn(webClient);
        return builder;
    }

    @Test
    void construirResumen_integraPacientesYNotificaciones() throws Exception {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        WebClient.Builder builder = mockBuilder(webClient);

        JsonNode pacientes = objectMapper.readTree("[{\"id\":1,\"nombre\":\"Ana\"}]");
        JsonNode notificaciones = objectMapper.readTree("[{\"id\":10,\"mensaje\":\"Aviso\"}]");

        when(webClient.get().uri("lb://ms-listas-espera/pacientes").retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(pacientes));
        when(webClient.get().uri("lb://ms-notificaciones/api/notificaciones/pendientes").retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(notificaciones));

        PortalResumenService service = new PortalResumenService(builder, objectMapper);
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
        WebClient.Builder builder = mockBuilder(webClient);

        JsonNode pacientes = objectMapper.readTree("[{\"id\":1,\"nombre\":\"Ana\"}]");

        when(webClient.get().uri("lb://ms-listas-espera/pacientes").retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(pacientes));
        when(webClient.get().uri("lb://ms-notificaciones/api/notificaciones/pendientes").retrieve().bodyToMono(JsonNode.class))
                .thenThrow(new IllegalStateException("caída temporal"));

        PortalResumenService service = new PortalResumenService(builder, objectMapper);
        JsonNode resumen = service.construirResumen();

        assertThat(resumen.get("resumen").get("totalPacientes").asInt()).isEqualTo(1);
        assertThat(resumen.get("resumen").get("totalNotificacionesPendientes").asInt()).isEqualTo(0);
        assertThat(resumen.get("errores")).hasSize(1);
        assertThat(resumen.get("notificacionesPendientes")).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void construirResumen_reenviaAuthorizationAlGateway() throws Exception {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        WebClient.Builder builder = mockBuilder(webClient);

        WebClient.RequestHeadersSpec pacientesSpec = mock(WebClient.RequestHeadersSpec.class, RETURNS_DEEP_STUBS);
        WebClient.RequestHeadersSpec notifSpec = mock(WebClient.RequestHeadersSpec.class, RETURNS_DEEP_STUBS);

        when(webClient.get().uri("lb://ms-listas-espera/pacientes")).thenReturn(pacientesSpec);
        when(webClient.get().uri("lb://ms-notificaciones/api/notificaciones/pendientes")).thenReturn(notifSpec);

        when(pacientesSpec.header(HttpHeaders.AUTHORIZATION, "Bearer token")).thenReturn(pacientesSpec);
        when(pacientesSpec.retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(objectMapper.readTree("[{\"id\":1,\"nombre\":\"Ana\"}]")));

        when(notifSpec.header(HttpHeaders.AUTHORIZATION, "Bearer token")).thenReturn(notifSpec);
        when(notifSpec.retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(objectMapper.readTree("[{\"id\":10,\"mensaje\":\"Aviso\"}]")));

        PortalResumenService service = new PortalResumenService(builder, objectMapper);
        JsonNode resumen = service.construirResumen("Bearer token");

        assertThat(resumen.get("errores")).isEmpty();
        assertThat(resumen.get("resumen").get("totalPacientes").asInt()).isEqualTo(1);
        assertThat(resumen.get("resumen").get("totalNotificacionesPendientes").asInt()).isEqualTo(1);
        verify(pacientesSpec).header(HttpHeaders.AUTHORIZATION, "Bearer token");
        verify(notifSpec).header(HttpHeaders.AUTHORIZATION, "Bearer token");
    }

    @Test
    void construirResumen_degradaCuandoFallanAmbosServicios() throws Exception {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        WebClient.Builder builder = mockBuilder(webClient);

        when(webClient.get().uri("lb://ms-listas-espera/pacientes").retrieve().bodyToMono(JsonNode.class))
                .thenThrow(new IllegalStateException("servicio caído"));
        when(webClient.get().uri("lb://ms-notificaciones/api/notificaciones/pendientes").retrieve().bodyToMono(JsonNode.class))
                .thenThrow(new IllegalStateException("servicio caído"));

        PortalResumenService service = new PortalResumenService(builder, objectMapper);
        JsonNode resumen = service.construirResumen();

        assertThat(resumen.get("resumen").get("totalPacientes").asInt()).isEqualTo(0);
        assertThat(resumen.get("resumen").get("totalNotificacionesPendientes").asInt()).isEqualTo(0);
        assertThat(resumen.get("errores")).hasSize(2);
    }

    @Test
    void construirResumen_degradaCuandoFallaPacientes() throws Exception {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        WebClient.Builder builder = mockBuilder(webClient);

        JsonNode notificaciones = objectMapper.readTree("[{\"id\":10}]");

        when(webClient.get().uri("lb://ms-listas-espera/pacientes").retrieve().bodyToMono(JsonNode.class))
                .thenThrow(new IllegalStateException("servicio caído"));
        when(webClient.get().uri("lb://ms-notificaciones/api/notificaciones/pendientes").retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(notificaciones));

        PortalResumenService service = new PortalResumenService(builder, objectMapper);
        JsonNode resumen = service.construirResumen();

        assertThat(resumen.get("resumen").get("totalNotificacionesPendientes").asInt()).isEqualTo(1);
        assertThat(resumen.get("errores")).hasSize(1);
    }

    @Test
    void construirResumen_respuestasVacias() throws Exception {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        WebClient.Builder builder = mockBuilder(webClient);

        when(webClient.get().uri("lb://ms-listas-espera/pacientes").retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(objectMapper.readTree("[]")));
        when(webClient.get().uri("lb://ms-notificaciones/api/notificaciones/pendientes").retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(objectMapper.readTree("[]")));

        PortalResumenService service = new PortalResumenService(builder, objectMapper);
        JsonNode resumen = service.construirResumen();

        assertThat(resumen.get("resumen").get("totalPacientes").asInt()).isEqualTo(0);
        assertThat(resumen.get("resumen").get("totalNotificacionesPendientes").asInt()).isEqualTo(0);
        assertThat(resumen.get("errores")).isEmpty();
    }
}

