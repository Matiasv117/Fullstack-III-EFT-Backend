package com.saludrednorte.ms_optimizacion.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CitaTest {

    private Cita cita;
    private Medico medico;

    @BeforeEach
    void setUp() {
        cita = new Cita();
        medico = new Medico();
        medico.setId(1L);
        medico.setNombre("Dr. Test");
    }

    @Test
    void testSetYGetId() {
        // Given
        Long id = 5L;
        
        // When
        cita.setId(id);
        
        // Then
        assertEquals(id, cita.getId());
    }

    @Test
    void testSetYGetPacienteId() {
        // Given
        Long pacienteId = 100L;
        
        // When
        cita.setPacienteId(pacienteId);
        
        // Then
        assertEquals(pacienteId, cita.getPacienteId());
    }

    @Test
    void testSetYGetMedico() {
        // Given & When
        cita.setMedico(medico);
        
        // Then
        assertNotNull(cita.getMedico());
        assertEquals("Dr. Test", cita.getMedico().getNombre());
    }

    @Test
    void testSetYGetFechaHora() {
        // Given
        LocalDateTime fechaHora = LocalDateTime.of(2026, 5, 20, 10, 30);
        
        // When
        cita.setFechaHora(fechaHora);
        
        // Then
        assertEquals(fechaHora, cita.getFechaHora());
    }

    @Test
    void testSetYGetEstado() {
        // Given & When
        cita.setEstado(EstadoCita.CONFIRMADA);
        
        // Then
        assertEquals(EstadoCita.CONFIRMADA, cita.getEstado());
    }

    @Test
    void testTodosCamposJuntos() {
        // Given
        Long id = 10L;
        Long pacienteId = 150L;
        LocalDateTime fecha = LocalDateTime.of(2026, 5, 21, 14, 0);
        
        // When
        cita.setId(id);
        cita.setPacienteId(pacienteId);
        cita.setMedico(medico);
        cita.setFechaHora(fecha);
        cita.setEstado(EstadoCita.PENDIENTE);
        
        // Then
        assertEquals(id, cita.getId());
        assertEquals(pacienteId, cita.getPacienteId());
        assertEquals(medico, cita.getMedico());
        assertEquals(fecha, cita.getFechaHora());
        assertEquals(EstadoCita.PENDIENTE, cita.getEstado());
    }

    @Test
    void testEstadoCitaCancelada() {
        // Given & When
        cita.setEstado(EstadoCita.CANCELADA);
        
        // Then
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
    }

    @Test
    void testValoresNulos() {
        // Given & When
        cita.setId(null);
        cita.setPacienteId(null);
        cita.setMedico(null);
        cita.setFechaHora(null);
        cita.setEstado(null);
        
        // Then
        assertNull(cita.getId());
        assertNull(cita.getPacienteId());
        assertNull(cita.getMedico());
        assertNull(cita.getFechaHora());
        assertNull(cita.getEstado());
    }
}

