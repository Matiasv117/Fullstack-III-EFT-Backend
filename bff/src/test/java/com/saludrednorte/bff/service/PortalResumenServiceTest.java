package com.saludrednorte.bff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import reactor.core.publisher.Mono;

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

    @Test
    void construirResumen_reenviaAuthorizationAlGateway() throws Exception {
        AtomicReference<String> pacientesAuth = new AtomicReference<>();
        AtomicReference<String> notificacionesAuth = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/pacientes", exchange -> responderJson(exchange, "[{\"id\":1,\"nombre\":\"Ana\"}]", pacientesAuth));
        server.createContext("/api/notificaciones/pendientes", exchange -> responderJson(exchange, "[{\"id\":10,\"mensaje\":\"Aviso\"}]", notificacionesAuth));
        server.start();

        try {
            int port = server.getAddress().getPort();
            WebClient webClient = WebClient.builder().baseUrl("http://localhost:" + port).build();

            PortalResumenService service = new PortalResumenService(webClient, objectMapper);
            JsonNode resumen = service.construirResumen("Bearer token");

            assertThat(resumen.get("resumen").get("totalPacientes").asInt()).isEqualTo(1);
            assertThat(resumen.get("resumen").get("totalNotificacionesPendientes").asInt()).isEqualTo(1);
            assertThat(resumen.get("errores")).isEmpty();
            assertThat(pacientesAuth.get()).isEqualTo("Bearer token");
            assertThat(notificacionesAuth.get()).isEqualTo("Bearer token");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void construirResumen_degradaCuandoFallanAmbosServicios() throws Exception {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);

        when(webClient.get().uri("/pacientes").retrieve().bodyToMono(JsonNode.class))
                .thenThrow(new IllegalStateException("servicio caído"));
        when(webClient.get().uri("/api/notificaciones/pendientes").retrieve().bodyToMono(JsonNode.class))
                .thenThrow(new IllegalStateException("servicio caído"));

        PortalResumenService service = new PortalResumenService(webClient, objectMapper);
        JsonNode resumen = service.construirResumen();

        assertThat(resumen.get("resumen").get("totalPacientes").asInt()).isEqualTo(0);
        assertThat(resumen.get("resumen").get("totalNotificacionesPendientes").asInt()).isEqualTo(0);
        assertThat(resumen.get("errores")).hasSize(2);
    }

    @Test
    void construirResumen_degradaCuandoFallaPacientes() throws Exception {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        JsonNode notificaciones = objectMapper.readTree("[{\"id\":10}]");

        when(webClient.get().uri("/pacientes").retrieve().bodyToMono(JsonNode.class))
                .thenThrow(new IllegalStateException("servicio caído"));
        when(webClient.get().uri("/api/notificaciones/pendientes").retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(notificaciones));

        PortalResumenService service = new PortalResumenService(webClient, objectMapper);
        JsonNode resumen = service.construirResumen();

        assertThat(resumen.get("resumen").get("totalNotificacionesPendientes").asInt()).isEqualTo(1);
        assertThat(resumen.get("errores")).hasSize(1);
    }

    @Test
    void construirResumen_respuestasVacias() throws Exception {
        WebClient webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);

        when(webClient.get().uri("/pacientes").retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(objectMapper.readTree("[]")));
        when(webClient.get().uri("/api/notificaciones/pendientes").retrieve().bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(objectMapper.readTree("[]")));

        PortalResumenService service = new PortalResumenService(webClient, objectMapper);
        JsonNode resumen = service.construirResumen();

        assertThat(resumen.get("resumen").get("totalPacientes").asInt()).isEqualTo(0);
        assertThat(resumen.get("resumen").get("totalNotificacionesPendientes").asInt()).isEqualTo(0);
        assertThat(resumen.get("errores")).isEmpty();
    }

    private static void responderJson(HttpExchange exchange, String json, AtomicReference<String> authHolder) throws IOException {
        authHolder.set(exchange.getRequestHeaders().getFirst("Authorization"));
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}

