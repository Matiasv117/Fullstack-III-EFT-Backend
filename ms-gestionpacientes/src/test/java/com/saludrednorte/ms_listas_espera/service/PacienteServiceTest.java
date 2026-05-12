package com.saludrednorte.ms_listas_espera.service;

import com.saludrednorte.ms_listas_espera.client.NotificationClient;
import com.saludrednorte.ms_listas_espera.dto.NotificationRequestDTO;
import com.saludrednorte.ms_listas_espera.entity.Paciente;
import com.saludrednorte.ms_listas_espera.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private NotificationClient notificationClient;

    private PacienteService pacienteService;

    @BeforeEach
    void setUp() {
        pacienteService = new PacienteService();
        ReflectionTestUtils.setField(pacienteService, "pacienteRepository", pacienteRepository);
        ReflectionTestUtils.setField(pacienteService, "notificationClient", notificationClient);
    }

    @Test
    void registrarPaciente_creaPacienteYNotifica() {
        Paciente paciente = new Paciente();
        paciente.setNombre("Ana");
        paciente.setApellido("Pérez");
        paciente.setDni("12345678-9");

        Paciente guardado = new Paciente();
        guardado.setId(10L);
        guardado.setNombre(paciente.getNombre());
        guardado.setApellido(paciente.getApellido());
        guardado.setDni(paciente.getDni());

        when(pacienteRepository.existsByDniIgnoreCase("12345678-9")).thenReturn(false);
        when(pacienteRepository.save(paciente)).thenReturn(guardado);
        when(notificationClient.createNotification(any())).thenReturn(ResponseEntity.ok().build());

        Paciente resultado = pacienteService.registrarPaciente(paciente);

        assertThat(resultado.getId()).isEqualTo(10L);
        verify(pacienteRepository).save(paciente);

        ArgumentCaptor<NotificationRequestDTO> captor = ArgumentCaptor.forClass(NotificationRequestDTO.class);
        verify(notificationClient).createNotification(captor.capture());
        assertThat(captor.getValue().getPacienteId()).isEqualTo(10L);
        assertThat(captor.getValue().getTipo()).isEqualTo("PACIENTE_ASIGNADO");
        assertThat(captor.getValue().getMensaje()).contains("Ana").contains("Pérez");
    }

    @Test
    void registrarPaciente_rechazaDniDuplicado() {
        Paciente paciente = new Paciente();
        paciente.setDni("12345678-9");

        when(pacienteRepository.existsByDniIgnoreCase("12345678-9")).thenReturn(true);

        assertThatThrownBy(() -> pacienteService.registrarPaciente(paciente))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value());
                });
    }
}


