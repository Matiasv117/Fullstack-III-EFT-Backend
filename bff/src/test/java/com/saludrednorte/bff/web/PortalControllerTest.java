package com.saludrednorte.bff.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.bff.service.PortalResumenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortalControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PortalResumenService portalResumenService;

    @InjectMocks
    private PortalController portalController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(portalController).build();
    }

    @Test
    void resumen_debeRetornar200() throws Exception {
        when(portalResumenService.construirResumen(anyString()))
                .thenReturn(objectMapper.readTree("{\"resumen\":{\"totalPacientes\":0}}"));

        mockMvc.perform(get("/api/portal/resumen")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void resumen_debeFuncionarSinToken() throws Exception {
        when(portalResumenService.construirResumen(null))
                .thenReturn(objectMapper.readTree("{\"resumen\":{\"totalPacientes\":0}}"));

        mockMvc.perform(get("/api/portal/resumen"))
                .andExpect(status().isOk());
    }
}
