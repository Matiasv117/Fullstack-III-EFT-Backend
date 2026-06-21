package com.saludrednorte.ms_listas_espera.service;

import com.saludrednorte.ms_listas_espera.client.CitaClient;
import com.saludrednorte.ms_listas_espera.dto.MedicoDTO;
import com.saludrednorte.ms_listas_espera.messaging.NotificacionEventPublisher;
import com.saludrednorte.ms_listas_espera.entity.Paciente;
import com.saludrednorte.ms_listas_espera.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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
    private NotificacionEventPublisher notificacionEventPublisher;

    @Mock
    private CitaClient citaClient;

    private PacienteService pacienteService;

    @BeforeEach
    void setUp() {
        pacienteService = new PacienteService();
        ReflectionTestUtils.setField(pacienteService, "pacienteRepository", pacienteRepository);
        ReflectionTestUtils.setField(pacienteService, "notificacionEventPublisher", notificacionEventPublisher);
        ReflectionTestUtils.setField(pacienteService, "citaClient", citaClient);
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

        MedicoDTO medico = new MedicoDTO();
        medico.setId(1L);
        medico.setNombre("Dr. García");
        medico.setEspecialidad("General");

        when(pacienteRepository.existsByDniIgnoreCase("12345678-9")).thenReturn(false);
        when(pacienteRepository.save(paciente)).thenReturn(guardado);
        when(citaClient.obtenerTodosMedicos()).thenReturn(java.util.List.of(medico));

        Paciente resultado = pacienteService.registrarPaciente(paciente);

        assertThat(resultado.getId()).isEqualTo(10L);
        verify(pacienteRepository).save(paciente);

        verify(notificacionEventPublisher).publicar(10L, "PACIENTE_ASIGNADO",
                "Paciente Ana Pérez registrado en el sistema");
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

    @Test
    void registrarPaciente_conDniNulo() {
        Paciente paciente = new Paciente();
        paciente.setNombre("Juan");
        paciente.setApellido("Gómez");
        paciente.setDni(null);

        Paciente guardado = new Paciente();
        guardado.setId(5L);
        guardado.setNombre("Juan");
        guardado.setApellido("Gómez");

        when(pacienteRepository.save(paciente)).thenReturn(guardado);
        when(citaClient.obtenerTodosMedicos()).thenReturn(java.util.List.of());

        Paciente resultado = pacienteService.registrarPaciente(paciente);

        assertThat(resultado.getId()).isEqualTo(5L);
        verify(pacienteRepository).save(paciente);
    }

    @Test
    void registrarPaciente_notificacionFalla() {
        Paciente paciente = new Paciente();
        paciente.setNombre("Carlos");
        paciente.setApellido("López");
        paciente.setDni("99999999-9");

        Paciente guardado = new Paciente();
        guardado.setId(8L);
        guardado.setNombre("Carlos");
        guardado.setApellido("López");

        when(pacienteRepository.existsByDniIgnoreCase("99999999-9")).thenReturn(false);
        when(pacienteRepository.save(paciente)).thenReturn(guardado);
        when(citaClient.obtenerTodosMedicos()).thenReturn(java.util.List.of());
        org.mockito.Mockito.doThrow(new RuntimeException("RabbitMQ error"))
                .when(notificacionEventPublisher).publicar(org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());

        Paciente resultado = pacienteService.registrarPaciente(paciente);

        assertThat(resultado.getId()).isEqualTo(8L);
        verify(pacienteRepository).save(paciente);
    }

    @Test
    void obtenerTodosPacientes_retornaLista() {
        Paciente paciente1 = new Paciente();
        paciente1.setId(1L);
        paciente1.setNombre("Juan");

        Paciente paciente2 = new Paciente();
        paciente2.setId(2L);
        paciente2.setNombre("María");

        when(pacienteRepository.findAll()).thenReturn(java.util.List.of(paciente1, paciente2));

        java.util.List<Paciente> resultado = pacienteService.obtenerTodosPacientes();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Juan");
        assertThat(resultado.get(1).getNombre()).isEqualTo("María");
    }

    @Test
    void obtenerPacientePorId_existente() {
        Paciente paciente = new Paciente();
        paciente.setId(5L);
        paciente.setNombre("Pedro");

        when(pacienteRepository.findById(5L)).thenReturn(java.util.Optional.of(paciente));

        java.util.Optional<Paciente> resultado = pacienteService.obtenerPacientePorId(5L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Pedro");
    }

    @Test
    void obtenerPacientePorId_noExistente() {
        when(pacienteRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        java.util.Optional<Paciente> resultado = pacienteService.obtenerPacientePorId(999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    void actualizarPaciente_exitoso() {
        Paciente paciente = new Paciente();
        paciente.setId(3L);
        paciente.setNombre("Ana");
        paciente.setEmail("ana@mail.com");

        when(pacienteRepository.existsById(3L)).thenReturn(true);
        when(pacienteRepository.save(paciente)).thenReturn(paciente);

        Paciente resultado = pacienteService.actualizarPaciente(paciente);

        assertThat(resultado.getId()).isEqualTo(3L);
        verify(pacienteRepository).save(paciente);
    }

    @Test
    void actualizarPaciente_noExistente() {
        Paciente paciente = new Paciente();
        paciente.setId(null);

        assertThatThrownBy(() -> pacienteService.actualizarPaciente(paciente))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
                });
    }

    @Test
    void actualizarPaciente_idNoEncontrado() {
        Paciente paciente = new Paciente();
        paciente.setId(999L);
        paciente.setNombre("Test");

        when(pacienteRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> pacienteService.actualizarPaciente(paciente))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
                });
    }

    @Test
    void actualizarPaciente_notificacionFalla() {
        Paciente paciente = new Paciente();
        paciente.setId(7L);
        paciente.setNombre("Roberto");

        when(pacienteRepository.existsById(7L)).thenReturn(true);
        when(pacienteRepository.save(paciente)).thenReturn(paciente);
        org.mockito.Mockito.doThrow(new RuntimeException("Error notificación"))
                .when(notificacionEventPublisher).publicar(org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());

        Paciente resultado = pacienteService.actualizarPaciente(paciente);

        assertThat(resultado.getId()).isEqualTo(7L);
        verify(pacienteRepository).save(paciente);
    }

    @Test
    void eliminarPaciente_exitoso() {
        when(pacienteRepository.existsById(5L)).thenReturn(true);

        pacienteService.eliminarPaciente(5L);

        verify(pacienteRepository).deleteById(5L);
    }

    @Test
    void eliminarPaciente_noExistente() {
        when(pacienteRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> pacienteService.eliminarPaciente(999L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
                });
    }
}
