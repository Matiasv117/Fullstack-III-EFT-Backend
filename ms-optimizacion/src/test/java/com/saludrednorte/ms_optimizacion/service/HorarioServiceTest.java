package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.entity.Horario;
import com.saludrednorte.ms_optimizacion.entity.Medico;
import com.saludrednorte.ms_optimizacion.repository.HorarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HorarioServiceTest {

    @Mock
    private HorarioRepository horarioRepository;

    @InjectMocks
    private HorarioService horarioService;

    private Horario horario;
    private Medico medico;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        medico = new Medico();
        medico.setId(1L);
        medico.setNombre("Dr. Lopez");
        
        horario = new Horario();
        horario.setId(1L);
        horario.setMedico(medico);
        horario.setFecha(LocalDate.now());
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(10, 0));
        horario.setDisponible(true);
    }

    @Test
    void testCrearHorarioExitoso() {
        // Given
        when(horarioRepository.save(horario)).thenReturn(horario);
        
        // When
        Horario resultado = horarioService.crearHorario(horario);
        
        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isDisponible());
        verify(horarioRepository, times(1)).save(horario);
    }

    @Test
    void testObtenerTodosHorarios() {
        // Given
        List<Horario> horarios = List.of(horario);
        when(horarioRepository.findAll()).thenReturn(horarios);
        
        // When
        List<Horario> resultado = horarioService.obtenerTodosHorarios();
        
        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(horarioRepository, times(1)).findAll();
    }

    @Test
    void testObtenerHorariosDisponibles() {
        // Given
        List<Horario> horarios = List.of(horario);
        when(horarioRepository.findByMedicoAndFechaAndDisponible(medico, LocalDate.now(), true))
                .thenReturn(horarios);
        
        // When
        List<Horario> resultado = horarioService.obtenerHorariosDisponibles(medico, LocalDate.now());
        
        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).isDisponible());
    }

    @Test
    void testActualizarHorarioExitoso() {
        // Given
        when(horarioRepository.existsById(1L)).thenReturn(true);
        when(horarioRepository.save(horario)).thenReturn(horario);
        
        // When
        Horario resultado = horarioService.actualizarHorario(horario);
        
        // Then
        assertNotNull(resultado);
        verify(horarioRepository, times(1)).save(horario);
    }

    @Test
    void testEliminarHorarioExitoso() {
        // Given
        when(horarioRepository.existsById(1L)).thenReturn(true);
        
        // When
        horarioService.eliminarHorario(1L);
        
        // Then
        verify(horarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void testActualizarHorarioNoEncontrado() {
        // Given
        when(horarioRepository.existsById(999L)).thenReturn(false);
        horario.setId(999L);
        
        // When & Then
        assertThrows(ResponseStatusException.class, () -> {
            horarioService.actualizarHorario(horario);
        });
    }
}

