package com.saludrednorte.bff.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificacionesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private NotificacionesController notificacionesController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificacionesController).build();
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    void obtenerNotificacionesPendientes_debeRetornar200() throws Exception {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("[]"));

        mockMvc.perform(get("/api/notificaciones/pendientes"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerNotificacionesPendientes_debeReenviarStatusCodeError() throws Exception {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenThrow(new WebClientResponseException(500, "Error", null, null, null));

        mockMvc.perform(get("/api/notificaciones/pendientes"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void obtenerNotificacionesPendientes_debeRetornar500EnErrorGenerico() throws Exception {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/notificaciones/pendientes"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void enviarNotificacion_debeRetornar200() throws Exception {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{}"));

        mockMvc.perform(post("/api/notificaciones/1/enviar"))
                .andExpect(status().isOk());
    }

    @Test
    void enviarNotificacion_debeRetornar500EnErrorGenerico() throws Exception {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/notificaciones/1/enviar"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void enviarNotificacionPorCanal_debeRetornar200() throws Exception {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{}"));

        mockMvc.perform(post("/api/notificaciones/1/enviar-canal?canal=EMAIL"))
                .andExpect(status().isOk());
    }

    @Test
    void enviarNotificacionPorCanal_debeReenviarStatusCodeError() throws Exception {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenThrow(new WebClientResponseException(400, "Bad Request", null, null, null));

        mockMvc.perform(post("/api/notificaciones/1/enviar-canal?canal=INVALIDO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void enviarNotificacionPorCanal_debeRetornar500EnErrorGenerico() throws Exception {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/notificaciones/1/enviar-canal?canal=EMAIL"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void enviarTodasLasNotificaciones_debeRetornar200() throws Exception {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{}"));

        mockMvc.perform(post("/api/notificaciones/enviar-todas"))
                .andExpect(status().isOk());
    }

    @Test
    void enviarTodasLasNotificaciones_debeReenviarStatusCodeError() throws Exception {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenThrow(new WebClientResponseException(500, "Error", null, null, null));

        mockMvc.perform(post("/api/notificaciones/enviar-todas"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void obtenerCanalesDisponibles_debeRetornar200() throws Exception {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("[\"EMAIL\"]"));

        mockMvc.perform(get("/api/notificaciones/info/canales"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerEstadoServicio_debeRetornar200() throws Exception {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{\"status\":\"UP\"}"));

        mockMvc.perform(get("/api/notificaciones/info/estado"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerEstadoServicio_debeRetornar500EnErrorGenerico() throws Exception {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/notificaciones/info/estado"))
                .andExpect(status().isInternalServerError());
    }
}
