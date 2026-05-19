package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.entity.Cita;
import com.saludrednorte.ms_optimizacion.entity.EstadoCita;
import com.saludrednorte.ms_optimizacion.entity.Medico;
import com.saludrednorte.ms_optimizacion.repository.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private CitaService citaService;

    private Cita cita;
    private Medico medico;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        medico = new Medico();
        medico.setId(1L);
        medico.setNombre("Dr. Juan");
        
        cita = new Cita();
        cita.setId(1L);
        cita.setPacienteId(100L);
        cita.setMedico(medico);
        cita.setFechaHora(LocalDateTime.now().plusDays(1));
        cita.setEstado(EstadoCita.CONFIRMADA);
    }

    @Test
    void testCrearCitaExitosa() {
        // Given
        when(citaRepository.existsByMedicoAndFechaHoraAndEstadoNot(
                medico, cita.getFechaHora(), EstadoCita.CANCELADA))
                .thenReturn(false);
        when(citaRepository.save(cita)).thenReturn(cita);
        
        // When
        Cita resultado = citaService.crearCita(cita);
        
        // Then
        assertNotNull(resultado);
        assertEquals(EstadoCita.CONFIRMADA, resultado.getEstado());
        verify(citaRepository, times(1)).save(cita);
    }

    @Test
    void testCrearCitaSinMedicoFalla() {
        // Given
        cita.setMedico(null);
        
        // When & Then
        assertThrows(ResponseStatusException.class, () -> {
            citaService.crearCita(cita);
        });
        verify(citaRepository, never()).save(any());
    }

    @Test
    void testObtenerTodasCitas() {
        // Given
        List<Cita> citas = List.of(cita);
        when(citaRepository.findAll()).thenReturn(citas);
        
        // When
        List<Cita> resultado = citaService.obtenerTodasCitas();
        
        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(citaRepository, times(1)).findAll();
    }

    @Test
    void testCancelarCitaExitosa() {
        // Given
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        
        // When
        citaService.cancelarCita(1L);
        
        // Then
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
        verify(citaRepository, times(1)).save(cita);
    }

    @Test
    void testCancelarCitaNoEncontrada() {
        // Given
        when(citaRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResponseStatusException.class, () -> {
            citaService.cancelarCita(999L);
        });
    }

    @Test
    void testEminarCitaExitosa() {
        // Given
        when(citaRepository.existsById(1L)).thenReturn(true);
        
        // When
        citaService.eliminarCita(1L);
        
        // Then
        verify(citaRepository, times(1)).deleteById(1L);
    }

    @Test
    void testObtenerCitaPorIdExistente() {
        // Given
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        
        // When
        Optional<Cita> resultado = citaService.obtenerCitaPorId(1L);
        
        // Then
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        verify(citaRepository, times(1)).findById(1L);
    }

    @Test
    void testObtenerCitasPorEstado() {
        // Given
        List<Cita> citas = List.of(cita);
        when(citaRepository.findByEstado(EstadoCita.CONFIRMADA)).thenReturn(citas);
        
        // When
        List<Cita> resultado = citaService.obtenerCitasPorEstado(EstadoCita.CONFIRMADA);
        
        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(citaRepository, times(1)).findByEstado(EstadoCita.CONFIRMADA);
    }

    @Test
    void testActualizarCitaNoEncontrada() {
        // Given
        when(citaRepository.existsById(999L)).thenReturn(false);
        cita.setId(999L);
        
        // When & Then
        assertThrows(ResponseStatusException.class, () -> {
            citaService.actualizarCita(cita);
        });
    }

    @Test
    void testCrearCitaConflict() {
        // Given: medico already has a cita at that time
        when(citaRepository.existsByMedicoAndFechaHoraAndEstadoNot(
                medico, cita.getFechaHora(), EstadoCita.CANCELADA))
                .thenReturn(true);

        // When & Then
        assertThrows(ResponseStatusException.class, () -> citaService.crearCita(cita));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void testActualizarCitaExitosa() {
        // Given
        cita.setId(2L);
        when(citaRepository.existsById(2L)).thenReturn(true);
        when(citaRepository.save(cita)).thenReturn(cita);

        // When
        Cita resultado = citaService.actualizarCita(cita);

        // Then
        assertNotNull(resultado);
        verify(citaRepository, times(1)).save(cita);
    }

    @Test
    void testEliminarCitaNoEncontrada() {
        // Given
        when(citaRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(ResponseStatusException.class, () -> citaService.eliminarCita(999L));
    }
}

