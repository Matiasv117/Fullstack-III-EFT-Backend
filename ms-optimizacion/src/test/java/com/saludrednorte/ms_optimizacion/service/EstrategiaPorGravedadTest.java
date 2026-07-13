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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstrategiaPorGravedadTest {

    @Mock
    private ListaEsperaClient listaEsperaClient;

    @Mock
    private PrioridadCalculadora prioridadCalculadora;

    @Mock
    private CitaService citaService;

    private EstrategiaPorGravedad estrategia;
    private Cita cita;

    @BeforeEach
    void setUp() {
        estrategia = new EstrategiaPorGravedad();
        ReflectionTestUtils.setField(estrategia, "listaEsperaClient", listaEsperaClient);
        ReflectionTestUtils.setField(estrategia, "prioridadCalculadora", prioridadCalculadora);
        ReflectionTestUtils.setField(estrategia, "citaService", citaService);

        Medico medico = new Medico();
        medico.setId(1L);

        cita = new Cita();
        cita.setId(10L);
        cita.setPacienteId(100L);
        cita.setMedico(medico);
        cita.setFechaHora(LocalDateTime.now().plusDays(1));
        cita.setEstado(EstadoCita.CANCELADA);
    }

    @Test
    void reasignarCita_noHaceNadaSiListaVacia() {
        when(listaEsperaClient.getListaEspera()).thenReturn(List.of());

        estrategia.reasignarCita(cita);
    }

    @Test
    void reasignarCita_noHaceNadaSiListaNull() {
        when(listaEsperaClient.getListaEspera()).thenReturn(null);

        estrategia.reasignarCita(cita);
    }

    @Test
    void reasignarCita_noHaceNadaSiCandidatoNull() {
        ListaEsperaDTO entry = new ListaEsperaDTO(1L, 201L, "x", null, "PENDIENTE");
        when(listaEsperaClient.getListaEspera()).thenReturn(List.of(entry));

        estrategia.reasignarCita(cita);
    }

    @Test
    void reasignarCita_asignaPacienteConMayorPrioridad() {
        ListaEsperaDTO baja = new ListaEsperaDTO(1L, 201L, "x", "BAJA", "PENDIENTE");
        ListaEsperaDTO alta = new ListaEsperaDTO(2L, 202L, "x", "ALTA", "PENDIENTE");

        when(listaEsperaClient.getListaEspera()).thenReturn(List.of(baja, alta));
        when(prioridadCalculadora.calcularNivel(anyInt(), anyDouble(), anyInt())).thenReturn(NivelPrioridad.BAJA, NivelPrioridad.ALTA);
        when(citaService.actualizarCita(any(Cita.class))).thenReturn(cita);

        estrategia.reasignarCita(cita);

        verify(citaService).actualizarCita(cita);
    }
}
