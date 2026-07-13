package com.saludrednorte.bff.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.bff.service.AuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks
    private AuditoriaController auditoriaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(auditoriaController).build();
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    void listarEventos_debeRetornar200() throws Exception {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("[]"));

        mockMvc.perform(get("/api/auditoria/eventos"))
                .andExpect(status().isOk());
    }

    @Test
    void listarEventos_debeReenviarStatusCodeError() throws Exception {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenThrow(new WebClientResponseException(403, "Forbidden", null, null, null));

        mockMvc.perform(get("/api/auditoria/eventos"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarEventos_debeRetornar500EnErrorGenerico() throws Exception {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/auditoria/eventos"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void registrarEvento_debeRetornar200() throws Exception {
        mockMvc.perform(post("/api/auditoria/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"LOGIN\",\"details\":\"test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void registrarEvento_debeRetornar400CuandoActionEsNull() throws Exception {
        mockMvc.perform(post("/api/auditoria/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarEvento_debeRetornar500CuandoAuditoriaFalla() throws Exception {
        doThrow(new RuntimeException("Error")).when(auditoriaService).registrarEvento(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/auditoria/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"LOGIN\"}"))
                .andExpect(status().isInternalServerError());
    }
}
