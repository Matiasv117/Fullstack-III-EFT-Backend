package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.entity.Medico;
import com.saludrednorte.ms_optimizacion.repository.MedicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicoServiceTest {

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private MedicoService medicoService;

    private Medico medico;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        medico = new Medico();
        medico.setId(1L);
        medico.setNombre("Dr. Carlos");
        medico.setEspecialidad("Cardiología");
    }

    @Test
    void testRegistrarMedicoExitoso() {
        // Given
        when(medicoRepository.save(medico)).thenReturn(medico);
        
        // When
        Medico resultado = medicoService.registrarMedico(medico);
        
        // Then
        assertNotNull(resultado);
        assertEquals("Dr. Carlos", resultado.getNombre());
        verify(medicoRepository, times(1)).save(medico);
    }

    @Test
    void testObtenerTodosMedicos() {
        // Given
        List<Medico> medicos = List.of(medico);
        when(medicoRepository.findAll()).thenReturn(medicos);
        
        // When
        List<Medico> resultado = medicoService.obtenerTodosMedicos();
        
        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(medicoRepository, times(1)).findAll();
    }

    @Test
    void testObtenerMedicoPorId() {
        // Given
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        
        // When
        Optional<Medico> resultado = medicoService.obtenerMedicoPorId(1L);
        
        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Dr. Carlos", resultado.get().getNombre());
        verify(medicoRepository, times(1)).findById(1L);
    }

    @Test
    void testActualizarMedicoExitoso() {
        // Given
        when(medicoRepository.existsById(1L)).thenReturn(true);
        when(medicoRepository.save(medico)).thenReturn(medico);
        
        // When
        Medico resultado = medicoService.actualizarMedico(medico);
        
        // Then
        assertNotNull(resultado);
        verify(medicoRepository, times(1)).save(medico);
    }

    @Test
    void testEliminarMedicoExitoso() {
        // Given
        when(medicoRepository.existsById(1L)).thenReturn(true);
        
        // When
        medicoService.eliminarMedico(1L);
        
        // Then
        verify(medicoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testActualizarMedicoNoEncontrado() {
        // Given
        when(medicoRepository.existsById(999L)).thenReturn(false);
        medico.setId(999L);

        // When & Then
        assertThrows(ResponseStatusException.class, () -> medicoService.actualizarMedico(medico));
    }
}

