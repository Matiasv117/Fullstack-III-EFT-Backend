package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.client.ListaEsperaClient;
import com.saludrednorte.ms_optimizacion.dto.ListaEsperaDTO;
import com.saludrednorte.ms_optimizacion.entity.Cita;
import com.saludrednorte.ms_optimizacion.entity.EstadoCita;
import com.saludrednorte.ms_optimizacion.entity.Medico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstrategiaLIFOTest {

    @Mock
    private ListaEsperaClient listaEsperaClient;

    @Mock
    private CitaService citaService;

    private EstrategiaLIFO estrategiaLIFO;
    private Cita cita;
    private Medico medico;

    @BeforeEach
    void setUp() {
        estrategiaLIFO = new EstrategiaLIFO();
        ReflectionTestUtils.setField(estrategiaLIFO, "listaEsperaClient", listaEsperaClient);
        ReflectionTestUtils.setField(estrategiaLIFO, "citaService", citaService);

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
    void testReasignarCitaLIFO_listaVacia() {
        when(listaEsperaClient.getListaEspera()).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> estrategiaLIFO.reasignarCita(cita));
    }

    @Test
    void testReasignarCitaLIFO_listaNull() {
        when(listaEsperaClient.getListaEspera()).thenReturn(null);

        assertDoesNotThrow(() -> estrategiaLIFO.reasignarCita(cita));
    }

    @Test
    void testReasignarCitaLIFO_conCandidato() {
        ListaEsperaDTO candidato = new ListaEsperaDTO(5L, 200L, "consulta", "MEDIA", "PENDIENTE");
        when(listaEsperaClient.getListaEspera()).thenReturn(List.of(candidato));
        when(citaService.actualizarCita(any(Cita.class))).thenReturn(cita);

        estrategiaLIFO.reasignarCita(cita);

        verify(citaService).actualizarCita(cita);
        verify(listaEsperaClient).actualizarEstado(eq(5L), eq("ASIGNADA"));
        assertEquals(200L, cita.getPacienteId());
    }
}
