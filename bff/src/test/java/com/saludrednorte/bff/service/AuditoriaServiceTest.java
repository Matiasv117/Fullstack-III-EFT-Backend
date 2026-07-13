package com.saludrednorte.bff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuditoriaServiceTest {

    private WebClient webClient;
    private WebClient.Builder webClientBuilder;
    private AuditoriaService service;

    @BeforeEach
    void setUp() {
        webClient = mock(WebClient.class);
        webClientBuilder = mock(WebClient.Builder.class);
        when(webClientBuilder.build()).thenReturn(webClient);
        service = new AuditoriaService(webClientBuilder);
    }

    @SuppressWarnings("unchecked")
    @Test
    void registrarEvento_debeEjecutarPostConDatosCorrectos() {
        WebClient.RequestBodyUriSpec postSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec postBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        service.registrarEvento("testUser", "CREATE", "detalle");

        verify(webClient).post();
        verify(postSpec).uri("lb://ms-auditoria/api/auditoria/eventos");
    }

    @SuppressWarnings("unchecked")
    @Test
    void registrarEvento_debeUsarAnonimoCuandoUsernameEsNull() {
        WebClient.RequestBodyUriSpec postSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec postBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        service.registrarEvento(null, "LOGIN", null);

        verify(postBodySpec).bodyValue(argThat((java.util.Map<String, String> m) ->
                "anonimo".equals(m.get("username")) && "LOGIN".equals(m.get("action")) && "".equals(m.get("details"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    void registrarEvento_debeManejarErrorSinLanzarExcepcion() {
        WebClient.RequestBodyUriSpec postSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec postBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));

        service.registrarEvento("user", "ACTION", "details");
    }
}
