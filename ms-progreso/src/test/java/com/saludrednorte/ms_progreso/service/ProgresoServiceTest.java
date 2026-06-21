package com.saludrednorte.ms_progreso.service;

import com.saludrednorte.ms_progreso.dto.ProgresoRequest;
import com.saludrednorte.ms_progreso.entity.EstadoProgreso;
import com.saludrednorte.ms_progreso.entity.ProgresoPaciente;
import com.saludrednorte.ms_progreso.exception.ProgresoDuplicadoException;
import com.saludrednorte.ms_progreso.exception.ProgresoNoEncontradoException;
import com.saludrednorte.ms_progreso.repository.ProgresoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class ProgresoServiceTest {

    private final ProgresoRepository progresoRepository = Mockito.mock(ProgresoRepository.class);
    private final ProgresoService progresoService = new ProgresoService(progresoRepository);

    @Test
    void registrarProgreso_creaNuevoRegistro() {
        ProgresoRequest request = new ProgresoRequest();
        request.setEstado("EN_LISTA_ACTIVA");

        when(progresoRepository.findByPacienteId(1L)).thenReturn(Optional.empty());
        when(progresoRepository.save(Mockito.any(ProgresoPaciente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String estado = progresoService.registrarProgreso(1L, request).getEstado();

        assertThat(estado).isEqualTo("EN_LISTA_ACTIVA");
    }

    @Test
    void registrarProgreso_falla_siYaExiste() {
        ProgresoRequest request = new ProgresoRequest();
        request.setEstado("EN_LISTA_ACTIVA");

        when(progresoRepository.findByPacienteId(1L))
                .thenReturn(Optional.of(new ProgresoPaciente()));

        assertThatThrownBy(() -> progresoService.registrarProgreso(1L, request))
                .isInstanceOf(ProgresoDuplicadoException.class);
    }

    @Test
    void obtenerProgreso_retornaEstado() {
        ProgresoPaciente progreso = new ProgresoPaciente();
        progreso.setPacienteId(1L);
        progreso.setEstado(EstadoProgreso.EN_LISTA_ACTIVA);

        when(progresoRepository.findByPacienteId(1L)).thenReturn(Optional.of(progreso));

        assertThat(progresoService.obtenerProgreso(1L).getEstado()).isEqualTo("EN_LISTA_ACTIVA");
    }

    @Test
    void actualizarProgreso_falla_siNoExiste() {
        ProgresoRequest request = new ProgresoRequest();
        request.setEstado("CITA_ASIGNADA");

        when(progresoRepository.findByPacienteId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progresoService.actualizarProgreso(1L, request))
                .isInstanceOf(ProgresoNoEncontradoException.class);
    }

    @Test
    void actualizarProgreso_actualizaEstadoExistente() {
        ProgresoRequest request = new ProgresoRequest();
        request.setEstado("CITA_ASIGNADA");

        ProgresoPaciente existente = new ProgresoPaciente();
        existente.setPacienteId(1L);
        existente.setEstado(EstadoProgreso.EN_LISTA_ACTIVA);

        when(progresoRepository.findByPacienteId(1L)).thenReturn(Optional.of(existente));
        when(progresoRepository.save(Mockito.any(ProgresoPaciente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String estado = progresoService.actualizarProgreso(1L, request).getEstado();

        assertThat(estado).isEqualTo("CITA_ASIGNADA");
    }

    @Test
    void registrarProgreso_falla_siEstadoEsNulo() {
        ProgresoRequest request = new ProgresoRequest();
        request.setEstado(null);

        when(progresoRepository.findByPacienteId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progresoService.registrarProgreso(1L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
