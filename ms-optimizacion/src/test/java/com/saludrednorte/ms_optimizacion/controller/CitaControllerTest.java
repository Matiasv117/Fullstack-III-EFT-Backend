package com.saludrednorte.ms_optimizacion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.ms_optimizacion.dto.CitaDTO;
import com.saludrednorte.ms_optimizacion.dto.MedicoDTO;
import com.saludrednorte.ms_optimizacion.entity.Cita;
import com.saludrednorte.ms_optimizacion.entity.EstadoCita;
import com.saludrednorte.ms_optimizacion.entity.Medico;
import com.saludrednorte.ms_optimizacion.mapper.ClinicalMapper;
import com.saludrednorte.ms_optimizacion.service.CitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CitaControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CitaService citaService;

    @Mock
    private ClinicalMapper mapper;

    @InjectMocks
    private CitaController controller;

    private Cita cita;
    private CitaDTO citaDTO;
    private Medico medico;
    private MedicoDTO medicoDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        medico = new Medico(1L, "Dr. Test", "Cardiología");
        medicoDTO = new MedicoDTO(1L, "Dr. Test", "Cardiología");
        
        cita = new Cita(1L, 100L, medico, LocalDateTime.now().plusDays(1), EstadoCita.CONFIRMADA);
        citaDTO = new CitaDTO(1L, 100L, medicoDTO, LocalDateTime.now().plusDays(1), EstadoCita.CONFIRMADA);
    }

    @Test
    void testCrearCitaExitosa() throws Exception {
        // Given
        when(mapper.toCitaEntity(any(CitaDTO.class))).thenReturn(cita);
        when(citaService.crearCita(any(Cita.class))).thenReturn(cita);
        when(mapper.toCitaDTO(any(Cita.class))).thenReturn(citaDTO);
        
        // When & Then
        mockMvc.perform(post("/citas")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(citaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
        
        verify(citaService, times(1)).crearCita(any());
    }

    @Test
    void testObtenerTodasCitas() throws Exception {
        // Given
        List<Cita> citas = List.of(cita);
        List<CitaDTO> citasDTO = List.of(citaDTO);
        when(citaService.obtenerTodasCitas()).thenReturn(citas);
        when(mapper.toCitaDTO(any(Cita.class))).thenReturn(citaDTO);
        
        // When & Then
        mockMvc.perform(get("/citas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        
        verify(citaService, times(1)).obtenerTodasCitas();
    }

    @Test
    void testObtenerCitaPorIdExistente() throws Exception {
        // Given
        when(citaService.obtenerCitaPorId(1L)).thenReturn(Optional.of(cita));
        when(mapper.toCitaDTO(any(Cita.class))).thenReturn(citaDTO);
        
        // When & Then
        mockMvc.perform(get("/citas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
        
        verify(citaService, times(1)).obtenerCitaPorId(1L);
    }

    @Test
    void testObtenerCitaPorIdNoEncontrada() throws Exception {
        // Given
        when(citaService.obtenerCitaPorId(999L)).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/citas/999"))
                .andExpect(status().isNotFound());
        
        verify(citaService, times(1)).obtenerCitaPorId(999L);
    }

    @Test
    void testObtenerCitasPorEstado() throws Exception {
        // Given
        List<Cita> citas = List.of(cita);
        when(citaService.obtenerCitasPorEstado(EstadoCita.CONFIRMADA)).thenReturn(citas);
        when(mapper.toCitaDTO(any(Cita.class))).thenReturn(citaDTO);
        
        // When & Then
        mockMvc.perform(get("/citas/estado/CONFIRMADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        
        verify(citaService, times(1)).obtenerCitasPorEstado(EstadoCita.CONFIRMADA);
    }

    @Test
    void testCancelarCitaExitosa() throws Exception {
        // Given & When & Then
        mockMvc.perform(delete("/citas/1"))
                .andExpect(status().isOk());
        
        verify(citaService, times(1)).cancelarCita(1L);
    }
}

