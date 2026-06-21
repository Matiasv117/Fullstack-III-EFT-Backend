package com.saludrednorte.ms_progreso.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.ms_progreso.dto.ProgresoRequest;
import com.saludrednorte.ms_progreso.dto.ProgresoResponse;
import com.saludrednorte.ms_progreso.service.ProgresoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProgresoControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private ProgresoService progresoService;

    @InjectMocks
    private ProgresoController progresoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(progresoController).build();
    }

    @Test
    void registrar_debeRetornarProgreso() throws Exception {
        ProgresoRequest request = new ProgresoRequest();
        request.setEstado("EN_LISTA_ACTIVA");
        ProgresoResponse response = new ProgresoResponse(1L, "EN_LISTA_ACTIVA", LocalDateTime.now());
        when(progresoService.registrarProgreso(eq(1L), any(ProgresoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/progreso/pacientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_LISTA_ACTIVA"));
    }

    @Test
    void actualizar_debeRetornarProgresoActualizado() throws Exception {
        ProgresoRequest request = new ProgresoRequest();
        request.setEstado("CITA_ASIGNADA");
        ProgresoResponse response = new ProgresoResponse(1L, "CITA_ASIGNADA", LocalDateTime.now());
        when(progresoService.actualizarProgreso(eq(1L), any(ProgresoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/progreso/pacientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CITA_ASIGNADA"));

        verify(progresoService).actualizarProgreso(eq(1L), any(ProgresoRequest.class));
    }

    @Test
    void obtener_debeRetornarProgreso() throws Exception {
        ProgresoResponse response = new ProgresoResponse(1L, "EN_LISTA_ACTIVA", LocalDateTime.now());
        when(progresoService.obtenerProgreso(1L)).thenReturn(response);

        mockMvc.perform(get("/progreso/pacientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId").value(1));
    }
}
