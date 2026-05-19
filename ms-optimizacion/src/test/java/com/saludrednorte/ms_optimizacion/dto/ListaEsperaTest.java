package com.saludrednorte.ms_optimizacion.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListaEsperaTest {

    private ListaEsperaDTO listaEspera;

    @BeforeEach
    void setUp() {
        listaEspera = new ListaEsperaDTO();
    }

    @Test
    void testConstructorVacio() {
        // Given
        ListaEsperaDTO dto = new ListaEsperaDTO();
        
        // Then
        assertNull(dto.getId());
        assertNull(dto.getPacienteId());
        assertNull(dto.getInterconsulta());
        assertNull(dto.getGravedad());
        assertNull(dto.getEstado());
    }

    @Test
    void testConstructorConParametros() {
        // Given
        Long id = 1L;
        Long pacienteId = 100L;
        String interconsulta = "Cardiologo";
        String gravedad = "Alta";
        String estado = "En espera";
        
        // When
        ListaEsperaDTO dto = new ListaEsperaDTO(id, pacienteId, interconsulta, gravedad, estado);
        
        // Then
        assertEquals(id, dto.getId());
        assertEquals(pacienteId, dto.getPacienteId());
        assertEquals(interconsulta, dto.getInterconsulta());
        assertEquals(gravedad, dto.getGravedad());
        assertEquals(estado, dto.getEstado());
    }

    @Test
    void testSetId() {
        // Given
        Long id = 5L;
        
        // When
        listaEspera.setId(id);
        
        // Then
        assertEquals(id, listaEspera.getId());
    }

    @Test
    void testSetPacienteId() {
        // Given
        Long pacienteId = 200L;
        
        // When
        listaEspera.setPacienteId(pacienteId);
        
        // Then
        assertEquals(pacienteId, listaEspera.getPacienteId());
    }

    @Test
    void testSetInterconsulta() {
        // Given
        String interconsulta = "Pediatria";
        
        // When
        listaEspera.setInterconsulta(interconsulta);
        
        // Then
        assertEquals(interconsulta, listaEspera.getInterconsulta());
    }

    @Test
    void testSetGravedad() {
        // Given
        String gravedad = "Baja";
        
        // When
        listaEspera.setGravedad(gravedad);
        
        // Then
        assertEquals(gravedad, listaEspera.getGravedad());
    }

    @Test
    void testSetEstado() {
        // Given
        String estado = "Procesada";
        
        // When
        listaEspera.setEstado(estado);
        
        // Then
        assertEquals(estado, listaEspera.getEstado());
    }

    @Test
    void testTodosCamposJuntos() {
        // Given
        Long id = 10L;
        Long pacienteId = 150L;
        String interconsulta = "Oncologia";
        String gravedad = "Critica";
        String estado = "Completa";
        
        // When
        listaEspera.setId(id);
        listaEspera.setPacienteId(pacienteId);
        listaEspera.setInterconsulta(interconsulta);
        listaEspera.setGravedad(gravedad);
        listaEspera.setEstado(estado);
        
        // Then
        assertEquals(id, listaEspera.getId());
        assertEquals(pacienteId, listaEspera.getPacienteId());
        assertEquals(interconsulta, listaEspera.getInterconsulta());
        assertEquals(gravedad, listaEspera.getGravedad());
        assertEquals(estado, listaEspera.getEstado());
    }

    @Test
    void testValoresNulos() {
        // Given & When
        listaEspera.setId(null);
        listaEspera.setPacienteId(null);
        listaEspera.setInterconsulta(null);
        listaEspera.setGravedad(null);
        listaEspera.setEstado(null);
        
        // Then
        assertNull(listaEspera.getId());
        assertNull(listaEspera.getPacienteId());
        assertNull(listaEspera.getInterconsulta());
        assertNull(listaEspera.getGravedad());
        assertNull(listaEspera.getEstado());
    }
}

