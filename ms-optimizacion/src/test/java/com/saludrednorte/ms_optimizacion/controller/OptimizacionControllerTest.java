package com.saludrednorte.ms_optimizacion.controller;

import com.saludrednorte.ms_optimizacion.dto.ListaEsperaDTO;
import com.saludrednorte.ms_optimizacion.dto.ReasignacionResponse;
import com.saludrednorte.ms_optimizacion.service.NivelPrioridad;
import com.saludrednorte.ms_optimizacion.service.OptimizacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OptimizacionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OptimizacionService optimizacionService;

    @InjectMocks
    private OptimizacionController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testProcesarCancelacionExitosa() throws Exception {
        // Given
        ReasignacionResponse expected = new ReasignacionResponse(1L, 100L, "Juan Pérez");
        when(optimizacionService.procesarCancelacion(1L, "fifo")).thenReturn(expected);
        
        // When & Then
        mockMvc.perform(post("/optimizacion/cancelar/1?estrategia=fifo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citaId").value(1L))
                .andExpect(jsonPath("$.pacienteId").value(100L))
                .andExpect(jsonPath("$.nombrePaciente").value("Juan Pérez"));
        
        verify(optimizacionService, times(1)).procesarCancelacion(1L, "fifo");
    }

    @Test
    void testProcesarCancelacionConEstrategiaGravedad() throws Exception {
        // Given
        when(optimizacionService.procesarCancelacion(5L, "gravedad")).thenReturn(new ReasignacionResponse(5L, 200L, null));
        
        // When & Then
        mockMvc.perform(post("/optimizacion/cancelar/5?estrategia=gravedad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citaId").value(5L))
                .andExpect(jsonPath("$.pacienteId").value(200L));
        
        verify(optimizacionService, times(1)).procesarCancelacion(5L, "gravedad");
    }

    @Test
    void testObtenerListaEsperaVacia() throws Exception {
        // Given
        when(optimizacionService.obtenerListaEspera()).thenReturn(List.of());
        
        // When & Then
        mockMvc.perform(get("/optimizacion/lista-espera"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        
        verify(optimizacionService, times(1)).obtenerListaEspera();
    }

    @Test
    void testObtenerListaEsperaConDatos() throws Exception {
        // Given
        ListaEsperaDTO item1 = new ListaEsperaDTO();
        ListaEsperaDTO item2 = new ListaEsperaDTO();
        when(optimizacionService.obtenerListaEspera()).thenReturn(List.of(item1, item2));
        
        // When & Then
        mockMvc.perform(get("/optimizacion/lista-espera"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        
        verify(optimizacionService, times(1)).obtenerListaEspera();
    }

    @Test
    void testCalcularPrioridad() throws Exception {
        when(optimizacionService.calcularPrioridadPaciente(4, 10.0, 5)).thenReturn(NivelPrioridad.ALTA);

        mockMvc.perform(get("/optimizacion/prioridad")
                        .param("gravedad", "4")
                        .param("distanciaKm", "10.0")
                        .param("diasEspera", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivel").value("ALTA"));

        verify(optimizacionService, times(1)).calcularPrioridadPaciente(4, 10.0, 5);
    }

    @Test
    void testProcesarCancelacionPorDefectoFIFO() throws Exception {
        // Given
        when(optimizacionService.procesarCancelacion(10L, "fifo")).thenReturn(new ReasignacionResponse(10L, 300L, "María García"));
        
        // When & Then (sin especificar estrategia, usa default)
        mockMvc.perform(post("/optimizacion/cancelar/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citaId").value(10L))
                .andExpect(jsonPath("$.pacienteId").value(300L))
                .andExpect(jsonPath("$.nombrePaciente").value("María García"));
        
        verify(optimizacionService, times(1)).procesarCancelacion(10L, "fifo");
    }
}

