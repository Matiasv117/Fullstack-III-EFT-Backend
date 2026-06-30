package com.saludrednorte.bff.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.bff.web.dto.AutotriageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AutotriageServiceTest {

    private WebClient webClient;
    private WebClient.Builder webClientBuilder;
    private AutotriageService service;
    private ProgresoService progresoService;
    private final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        webClient = mock(WebClient.class);
        webClientBuilder = mock(WebClient.Builder.class);
        when(webClientBuilder.build()).thenReturn(webClient);
        progresoService = mock(ProgresoService.class);
        service = new AutotriageService(webClientBuilder, mapper, progresoService);
    }

    @Test
    void procesar_debeRetornarStatusOkCuandoAmbasLlamadasExitosas() {
        AutotriageRequest request = new AutotriageRequest();
        request.setPacienteId(1L);
        request.setGravedad(3);
        request.setLat(-33.4489);
        request.setLon(-70.6693);
        request.setSintomas("Dolor de cabeza");

        WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec getHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.RequestBodyUriSpec postSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec postBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec getResponseSpec = mock(WebClient.ResponseSpec.class);
        WebClient.ResponseSpec postResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.header(anyString(), anyString())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(getResponseSpec);
        when(getResponseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mapper.createObjectNode().put("nivel", "ALTO")));

        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(postBodySpec);
        when(postBodySpec.header(anyString(), anyString())).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);
        when(postResponseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mapper.createObjectNode()));

        JsonNode result = service.procesar(request, "Bearer token");

        assertThat(result.get("status").asText()).isEqualTo("ok");
        assertThat(result.has("prioridad")).isTrue();
        assertThat(result.has("listaEspera")).isTrue();
    }

    @Test
    void procesar_debeManejarErrorEnPrioridad() {
        AutotriageRequest request = new AutotriageRequest();
        request.setPacienteId(1L);
        request.setGravedad(3);
        request.setLat(-33.4489);
        request.setLon(-70.6693);
        request.setSintomas("Fiebre");

        WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestBodyUriSpec postSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec postBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec postResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenThrow(new RuntimeException("Error de conexion"));

        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(postBodySpec);
        when(postBodySpec.header(anyString(), anyString())).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);
        when(postResponseSpec.bodyToMono(JsonNode.class))
                .thenReturn(Mono.just(mapper.createObjectNode().put("id", 1)));

        JsonNode result = service.procesar(request, null);

        assertThat(result.get("status").asText()).isEqualTo("ok");
        assertThat(result.has("prioridadError")).isTrue();
        assertThat(result.has("listaEspera")).isTrue();
    }

    @Test
    void procesar_debeManejarErrorEnListaEspera() {
        AutotriageRequest request = new AutotriageRequest();
        request.setPacienteId(1L);
        request.setGravedad(1);
        request.setLat(-33.4489);
        request.setLon(-70.6693);
        request.setSintomas("Dolor");

        WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec getHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec getResponseSpec = mock(WebClient.ResponseSpec.class);
        WebClient.RequestBodyUriSpec postSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec postBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.uri(anyString())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.header(anyString(), anyString())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(getResponseSpec);
        when(getResponseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mapper.createObjectNode().put("nivel", "BAJO")));

        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(postBodySpec);
        when(postBodySpec.header(anyString(), anyString())).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenThrow(new WebClientResponseException(400, "Bad Request", null, null, null));

        JsonNode result = service.procesar(request, "Bearer token");

        assertThat(result.get("status").asText()).isEqualTo("ok");
        assertThat(result.has("prioridad")).isTrue();
        assertThat(result.has("listaError")).isTrue();
    }
}
