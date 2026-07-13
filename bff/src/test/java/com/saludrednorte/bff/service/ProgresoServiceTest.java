package com.saludrednorte.bff.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ProgresoServiceTest {

    private WebClient webClient;
    private WebClient.Builder webClientBuilder;
    private ProgresoService service;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        webClient = mock(WebClient.class);
        webClientBuilder = mock(WebClient.Builder.class);
        when(webClientBuilder.build()).thenReturn(webClient);
        service = new ProgresoService(webClientBuilder, mapper);
    }

    @SuppressWarnings("unchecked")
    @Test
    void registrarProgreso_debeEjecutarPost() {
        WebClient.RequestBodyUriSpec postSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec postBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString(), any(Object[].class))).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        service.registrarProgreso(1L, "EN_ESPERA");

        verify(postSpec).uri("lb://ms-progreso/progreso/pacientes/{id}", 1L);
    }

    @SuppressWarnings("unchecked")
    @Test
    void registrarProgreso_debeManejarError() {
        WebClient.RequestBodyUriSpec postSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec postBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString(), any(Object[].class))).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenThrow(new RuntimeException("Error"));

        service.registrarProgreso(1L, "EN_ESPERA");
    }

    @SuppressWarnings("unchecked")
    @Test
    void actualizarProgreso_debeEjecutarPut() {
        WebClient.RequestBodyUriSpec putSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec putBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec putHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.put()).thenReturn(putSpec);
        when(putSpec.uri(anyString(), any(Object[].class))).thenReturn(putBodySpec);
        when(putBodySpec.bodyValue(any())).thenReturn(putHeadersSpec);
        when(putHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        service.actualizarProgreso(2L, "CITA_ASIGNADA");

        verify(putSpec).uri("lb://ms-progreso/progreso/pacientes/{id}", 2L);
    }

    @SuppressWarnings("unchecked")
    @Test
    void actualizarProgreso_debeManejarError() {
        WebClient.RequestBodyUriSpec putSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec putBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec putHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        when(webClient.put()).thenReturn(putSpec);
        when(putSpec.uri(anyString(), any(Object[].class))).thenReturn(putBodySpec);
        when(putBodySpec.bodyValue(any())).thenReturn(putHeadersSpec);
        when(putHeadersSpec.retrieve()).thenThrow(new RuntimeException("Error"));

        service.actualizarProgreso(2L, "CITA_ASIGNADA");
    }
}
