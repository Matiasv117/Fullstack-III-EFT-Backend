package com.saludrednorte.ms_optimizacion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.ms_optimizacion.dto.MedicoDTO;
import com.saludrednorte.ms_optimizacion.entity.Medico;
import com.saludrednorte.ms_optimizacion.mapper.ClinicalMapper;
import com.saludrednorte.ms_optimizacion.service.MedicoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MedicoControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MedicoService medicoService;

    @Mock
    private ClinicalMapper mapper;

    @InjectMocks
    private MedicoController controller;

    private Medico medico;
    private MedicoDTO medicoDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        
        medico = new Medico(1L, "Dr. Rodriguez", "Pediatría");
        medicoDTO = new MedicoDTO(1L, "Dr. Rodriguez", "Pediatría");
    }

    @Test
    void testRegistrarMedicoExitoso() throws Exception {
        // Given
        when(mapper.toMedicoEntity(any(MedicoDTO.class))).thenReturn(medico);
        when(medicoService.registrarMedico(any(Medico.class))).thenReturn(medico);
        when(mapper.toMedicoDTO(any(Medico.class))).thenReturn(medicoDTO);
        
        // When & Then
        mockMvc.perform(post("/medicos")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(medicoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Dr. Rodriguez"));
        
        verify(medicoService, times(1)).registrarMedico(any());
    }

    @Test
    void testObtenerTodosMedicos() throws Exception {
        // Given
        List<Medico> medicos = List.of(medico);
        when(medicoService.obtenerTodosMedicos()).thenReturn(medicos);
        when(mapper.toMedicoDTO(any(Medico.class))).thenReturn(medicoDTO);
        
        // When & Then
        mockMvc.perform(get("/medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        
        verify(medicoService, times(1)).obtenerTodosMedicos();
    }

    @Test
    void testObtenerMedicoPorIdExistente() throws Exception {
        // Given
        when(medicoService.obtenerMedicoPorId(1L)).thenReturn(Optional.of(medico));
        when(mapper.toMedicoDTO(any(Medico.class))).thenReturn(medicoDTO);
        
        // When & Then
        mockMvc.perform(get("/medicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Dr. Rodriguez"));
        
        verify(medicoService, times(1)).obtenerMedicoPorId(1L);
    }

    @Test
    void testObtenerMedicoPorIdNoEncontrado() throws Exception {
        // Given
        when(medicoService.obtenerMedicoPorId(999L)).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/medicos/999"))
                .andExpect(status().isNotFound());
        
        verify(medicoService, times(1)).obtenerMedicoPorId(999L);
    }

    @Test
    void testActualizarMedicoExitoso() throws Exception {
        // Given
        when(mapper.toMedicoEntity(any(MedicoDTO.class))).thenReturn(medico);
        when(medicoService.actualizarMedico(any(Medico.class))).thenReturn(medico);
        when(mapper.toMedicoDTO(any(Medico.class))).thenReturn(medicoDTO);
        
        // When & Then
        mockMvc.perform(put("/medicos")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(medicoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
        
        verify(medicoService, times(1)).actualizarMedico(any());
    }

    @Test
    void testEliminarMedicoExitoso() throws Exception {
        // Given & When & Then
        mockMvc.perform(delete("/medicos/1"))
                .andExpect(status().isOk());
        
        verify(medicoService, times(1)).eliminarMedico(1L);
    }
}

