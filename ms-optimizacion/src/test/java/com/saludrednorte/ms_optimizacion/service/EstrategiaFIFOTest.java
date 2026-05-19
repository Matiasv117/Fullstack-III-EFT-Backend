package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.entity.Cita;
import com.saludrednorte.ms_optimizacion.entity.EstadoCita;
import com.saludrednorte.ms_optimizacion.entity.Medico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EstrategiaFIFOTest {

    private EstrategiaFIFO estrategiaFIFO;
    private Cita cita;
    private Medico medico;

    @BeforeEach
    void setUp() {
        estrategiaFIFO = new EstrategiaFIFO();
        
        medico = new Medico();
        medico.setId(1L);
        medico.setNombre("Dr. Fernando");
        
        cita = new Cita();
        cita.setId(1L);
        cita.setPacienteId(100L);
        cita.setMedico(medico);
        cita.setFechaHora(LocalDateTime.now().plusDays(1));
        cita.setEstado(EstadoCita.CANCELADA);
    }

    @Test
    void testReasignarCitaFIFONoDaError() {
        // Given: estrategia FIFO y una cita cancelada
        // When: llamamos a reasignarCita
        // Then: no debe lanzar excepción
        assertDoesNotThrow(() -> {
            estrategiaFIFO.reasignarCita(cita);
        });
    }

    @Test
    void testReasignarCitaFIFOConCitaValida() {
        // Given
        assertNotNull(cita);
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
        
        // When
        estrategiaFIFO.reasignarCita(cita);
        
        // Then: la cita debe seguir siendo válida
        assertNotNull(cita.getMedico());
        assertNotNull(cita.getPacienteId());
    }

    @Test
    void testReasignarCitaFIFOMultiplesTiempos() {
        // Given: múltiples citas
        Cita cita2 = new Cita();
        cita2.setId(2L);
        cita2.setPacienteId(101L);
        cita2.setMedico(medico);
        cita2.setFechaHora(LocalDateTime.now().plusDays(2));
        
        // When: procesar ambas
        estrategiaFIFO.reasignarCita(cita);
        estrategiaFIFO.reasignarCita(cita2);
        
        // Then: ambas deben procesarse sin errores
        assertNotNull(cita.getId());
        assertNotNull(cita2.getId());
    }
}

