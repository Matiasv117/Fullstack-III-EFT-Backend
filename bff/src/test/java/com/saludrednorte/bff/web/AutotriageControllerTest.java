package com.saludrednorte.bff.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.bff.service.AutotriageService;
import com.saludrednorte.bff.web.dto.AutotriageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AutotriageControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AutotriageService autotriageService;

    @InjectMocks
    private AutotriageController autotriageController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(autotriageController).build();
    }

    @Test
    void recibirAutotriage_debeRetornar200() throws Exception {
        when(autotriageService.procesar(any(AutotriageRequest.class), any()))
                .thenReturn(objectMapper.readTree("{\"status\":\"ok\"}"));

        mockMvc.perform(post("/api/autotriage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":1,\"gravedad\":3,\"lat\":-33.0,\"lon\":-70.0,\"sintomas\":\"Dolor\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void recibirAutotriage_debeFuncionarConToken() throws Exception {
        when(autotriageService.procesar(any(AutotriageRequest.class), any()))
                .thenReturn(objectMapper.readTree("{\"status\":\"ok\"}"));

        mockMvc.perform(post("/api/autotriage")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":1,\"gravedad\":3,\"lat\":-33.0,\"lon\":-70.0,\"sintomas\":\"Dolor\"}"))
                .andExpect(status().isOk());
    }
}
