package com.saludrednorte.ms_optimizacion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.ms_optimizacion.entity.Horario;
import com.saludrednorte.ms_optimizacion.entity.Medico;
import com.saludrednorte.ms_optimizacion.service.HorarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HorarioControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private HorarioService horarioService;

    @InjectMocks
    private HorarioController controller;

    private Horario horario;
    private Medico medico;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        medico = new Medico();
        medico.setId(1L);
        medico.setNombre("Dr. Torres");
        
        horario = new Horario();
        horario.setId(1L);
        horario.setMedico(medico);
        horario.setFecha(LocalDate.now());
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(10, 0));
        horario.setDisponible(true);
    }

    @Test
    void testCrearHorarioExitoso() throws Exception {
        // Given
        when(horarioService.crearHorario(any(Horario.class))).thenReturn(horario);
        
        // When & Then
        mockMvc.perform(post("/horarios")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(horario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.disponible").value(true));
        
        verify(horarioService, times(1)).crearHorario(any());
    }

    @Test
    void testObtenerTodosHorarios() throws Exception {
        // Given
        List<Horario> horarios = List.of(horario);
        when(horarioService.obtenerTodosHorarios()).thenReturn(horarios);
        
        // When & Then
        mockMvc.perform(get("/horarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        
        verify(horarioService, times(1)).obtenerTodosHorarios();
    }

    @Test
    void testObtenerHorariosDisponibles() throws Exception {
        // Given
        List<Horario> horarios = List.of(horario);
        when(horarioService.obtenerHorariosDisponibles(any(Medico.class), any(LocalDate.class)))
                .thenReturn(horarios);
        
        // When & Then
        mockMvc.perform(get("/horarios/disponibles?medicoId=1&fecha=2025-05-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        
        verify(horarioService, times(1)).obtenerHorariosDisponibles(any(), any());
    }

    @Test
    void testObtenerHorarioPorIdExistente() throws Exception {
        // Given
        when(horarioService.obtenerHorarioPorId(1L)).thenReturn(Optional.of(horario));
        
        // When & Then
        mockMvc.perform(get("/horarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.disponible").value(true));
        
        verify(horarioService, times(1)).obtenerHorarioPorId(1L);
    }

    @Test
    void testObtenerHorarioPorIdNoEncontrado() throws Exception {
        // Given
        when(horarioService.obtenerHorarioPorId(999L)).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/horarios/999"))
                .andExpect(status().isNotFound());
        
        verify(horarioService, times(1)).obtenerHorarioPorId(999L);
    }

    @Test
    void testActualizarHorarioExitoso() throws Exception {
        // Given
        when(horarioService.actualizarHorario(any(Horario.class))).thenReturn(horario);
        
        // When & Then
        mockMvc.perform(put("/horarios")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(horario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
        
        verify(horarioService, times(1)).actualizarHorario(any());
    }

    @Test
    void testEliminarHorarioExitoso() throws Exception {
        // Given & When & Then
        mockMvc.perform(delete("/horarios/1"))
                .andExpect(status().isOk());
        
        verify(horarioService, times(1)).eliminarHorario(1L);
    }
}

