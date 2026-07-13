package com.saludrednorte.ms_listas_espera.service;

import com.saludrednorte.ms_listas_espera.client.NotificationClient;
import com.saludrednorte.ms_listas_espera.dto.NotificationRequestDTO;
import com.saludrednorte.ms_listas_espera.entity.Estado;
import com.saludrednorte.ms_listas_espera.entity.Gravedad;
import com.saludrednorte.ms_listas_espera.entity.ListaEspera;
import com.saludrednorte.ms_listas_espera.entity.Paciente;
import com.saludrednorte.ms_listas_espera.repository.ListaEsperaProcedimientoRepository;
import com.saludrednorte.ms_listas_espera.repository.ListaEsperaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListaEsperaServiceTest {

    @Mock
    private ListaEsperaRepository listaEsperaRepository;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private ListaEsperaProcedimientoRepository procedimientoRepository;

    private ListaEsperaService listaEsperaService;

    @BeforeEach
    void setUp() {
        listaEsperaService = new ListaEsperaService();
        ReflectionTestUtils.setField(listaEsperaService, "listaEsperaRepository", listaEsperaRepository);
        ReflectionTestUtils.setField(listaEsperaService, "notificationClient", notificationClient);
    }

    @Test
    void agregarAListaEspera_asignaPendienteYNotifica() {
        Paciente paciente = new Paciente();
        paciente.setId(7L);

        ListaEspera listaEspera = new ListaEspera();
        listaEspera.setPaciente(paciente);
        listaEspera.setGravedad(Gravedad.MEDIA);
        listaEspera.setInterconsulta("Cardiología");

        when(listaEsperaRepository.save(any(ListaEspera.class))).thenAnswer(invocation -> {
            ListaEspera guardada = invocation.getArgument(0);
            guardada.setId(99L);
            return guardada;
        });
        when(notificationClient.createNotification(any())).thenReturn(ResponseEntity.ok().build());

        ListaEspera resultado = listaEsperaService.agregarAListaEspera(listaEspera);

        assertThat(resultado.getEstado()).isEqualTo(Estado.PENDIENTE);
        assertThat(resultado.getId()).isEqualTo(99L);

        ArgumentCaptor<NotificationRequestDTO> captor = ArgumentCaptor.forClass(NotificationRequestDTO.class);
        verify(notificationClient).createNotification(captor.capture());
        assertThat(captor.getValue().getPacienteId()).isEqualTo(7L);
        assertThat(captor.getValue().getTipo()).isEqualTo("PACIENTE_ASIGNADO");
        assertThat(captor.getValue().getMensaje()).contains("99");
    }

    @Test
    void actualizarEstado_lanza404SiNoExiste() {
        when(listaEsperaRepository.findById(55L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listaEsperaService.actualizarEstado(55L, Estado.ASIGNADA))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void obtenerListaEspera_retornaListaCompleta() {
        Paciente paciente = new Paciente();
        paciente.setId(1L);

        ListaEspera lista1 = new ListaEspera();
        lista1.setId(1L);
        lista1.setPaciente(paciente);
        lista1.setEstado(Estado.PENDIENTE);

        ListaEspera lista2 = new ListaEspera();
        lista2.setId(2L);
        lista2.setPaciente(paciente);
        lista2.setEstado(Estado.ASIGNADA);

        when(listaEsperaRepository.findAll()).thenReturn(java.util.List.of(lista1, lista2));

        java.util.List<ListaEspera> resultado = listaEsperaService.obtenerListaEspera();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getEstado()).isEqualTo(Estado.PENDIENTE);
    }

    @Test
    void obtenerPorEstado_returnsFiltered() {
        Paciente paciente = new Paciente();
        paciente.setId(1L);

        ListaEspera lista = new ListaEspera();
        lista.setId(1L);
        lista.setPaciente(paciente);
        lista.setEstado(Estado.PENDIENTE);

        when(listaEsperaRepository.findByEstado(Estado.PENDIENTE)).thenReturn(java.util.List.of(lista));

        java.util.List<ListaEspera> resultado = listaEsperaService.obtenerPorEstado(Estado.PENDIENTE);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEstado()).isEqualTo(Estado.PENDIENTE);
    }

    @Test
    void obtenerPorGravedad_returnsOrdenado() {
        Paciente paciente = new Paciente();
        paciente.setId(1L);

        ListaEspera lista1 = new ListaEspera();
        lista1.setId(1L);
        lista1.setPaciente(paciente);
        lista1.setGravedad(Gravedad.ALTA);

        ListaEspera lista2 = new ListaEspera();
        lista2.setId(2L);
        lista2.setPaciente(paciente);
        lista2.setGravedad(Gravedad.ALTA);

        when(listaEsperaRepository.findByGravedadOrderByIdAsc(Gravedad.ALTA))
                .thenReturn(java.util.List.of(lista1, lista2));

        java.util.List<ListaEspera> resultado = listaEsperaService.obtenerPorGravedad(Gravedad.ALTA);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getGravedad()).isEqualTo(Gravedad.ALTA);
    }

    @Test
    void obtenerPorId_existente() {
        Paciente paciente = new Paciente();
        paciente.setId(1L);

        ListaEspera lista = new ListaEspera();
        lista.setId(5L);
        lista.setPaciente(paciente);
        lista.setEstado(Estado.PENDIENTE);

        when(listaEsperaRepository.findById(5L)).thenReturn(Optional.of(lista));

        Optional<ListaEspera> resultado = listaEsperaService.obtenerPorId(5L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(5L);
    }

    @Test
    void obtenerPorId_noExistente() {
        when(listaEsperaRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ListaEspera> resultado = listaEsperaService.obtenerPorId(999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    void agregarAListaEspera_pacientePorIdInvalido() {
        ListaEspera listaEspera = new ListaEspera();
        listaEspera.setPaciente(null);

        assertThatThrownBy(() -> listaEsperaService.agregarAListaEspera(listaEspera))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode().value()).isEqualTo(400);
                });
    }

    @Test
    void agregarAListaEspera_pacientePorIdNulo() {
        Paciente paciente = new Paciente();
        paciente.setId(null);

        ListaEspera listaEspera = new ListaEspera();
        listaEspera.setPaciente(paciente);

        assertThatThrownBy(() -> listaEsperaService.agregarAListaEspera(listaEspera))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void actualizarEstado_exitoso() {
        Paciente paciente = new Paciente();
        paciente.setId(1L);

        ListaEspera lista = new ListaEspera();
        lista.setId(10L);
        lista.setPaciente(paciente);
        lista.setEstado(Estado.PENDIENTE);

        when(listaEsperaRepository.findById(10L)).thenReturn(Optional.of(lista));
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationClient.createNotification(any())).thenReturn(ResponseEntity.ok().build());

        ListaEspera resultado = listaEsperaService.actualizarEstado(10L, Estado.ASIGNADA);

        assertThat(resultado.getEstado()).isEqualTo(Estado.ASIGNADA);
        verify(listaEsperaRepository).save(any(ListaEspera.class));
    }

    @Test
    void actualizarEstado_notificacionFalla() {
        Paciente paciente = new Paciente();
        paciente.setId(1L);

        ListaEspera lista = new ListaEspera();
        lista.setId(10L);
        lista.setPaciente(paciente);
        lista.setEstado(Estado.PENDIENTE);

        when(listaEsperaRepository.findById(10L)).thenReturn(Optional.of(lista));
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationClient.createNotification(any())).thenThrow(new RuntimeException("Error"));

        ListaEspera resultado = listaEsperaService.actualizarEstado(10L, Estado.FINALIZADA);

        assertThat(resultado.getEstado()).isEqualTo(Estado.FINALIZADA);
    }

    @Test
    void eliminarDeListaEspera_exitoso() {
        Paciente paciente = new Paciente();
        paciente.setId(1L);

        ListaEspera lista = new ListaEspera();
        lista.setId(15L);
        lista.setPaciente(paciente);

        when(listaEsperaRepository.findById(15L)).thenReturn(Optional.of(lista));
        when(notificationClient.createNotification(any())).thenReturn(ResponseEntity.ok().build());

        listaEsperaService.eliminarDeListaEspera(15L);

        verify(listaEsperaRepository).deleteById(15L);
    }

    @Test
    void eliminarDeListaEspera_noExiste() {
        when(listaEsperaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listaEsperaService.eliminarDeListaEspera(999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void agregarAListaEspera_conProcedimientoRepository_gravedadNull() {
        Paciente paciente = new Paciente();
        paciente.setId(1L);

        ListaEspera listaEspera = new ListaEspera();
        listaEspera.setPaciente(paciente);
        listaEspera.setGravedad(null);

        ReflectionTestUtils.setField(listaEsperaService, "procedimientoRepository", procedimientoRepository);
        when(procedimientoRepository.calcularGravedad(0, 1)).thenReturn(Gravedad.ALTA);
        when(listaEsperaRepository.save(any(ListaEspera.class))).thenAnswer(invocation -> {
            ListaEspera guardada = invocation.getArgument(0);
            guardada.setId(50L);
            return guardada;
        });
        when(notificationClient.createNotification(any())).thenReturn(ResponseEntity.ok().build());

        ListaEspera resultado = listaEsperaService.agregarAListaEspera(listaEspera);

        assertThat(resultado.getGravedad()).isEqualTo(Gravedad.ALTA);
        assertThat(resultado.getEstado()).isEqualTo(Estado.PENDIENTE);
    }

    @Test
    void actualizarEstado_conProcedimientoRepository() {
        Paciente paciente = new Paciente();
        paciente.setId(1L);

        ListaEspera lista = new ListaEspera();
        lista.setId(20L);
        lista.setPaciente(paciente);
        lista.setEstado(Estado.PENDIENTE);

        ReflectionTestUtils.setField(listaEsperaService, "procedimientoRepository", procedimientoRepository);
        when(listaEsperaRepository.findById(20L)).thenReturn(Optional.of(lista));
        when(listaEsperaRepository.findById(20L)).thenReturn(Optional.of(lista));
        when(notificationClient.createNotification(any())).thenReturn(ResponseEntity.ok().build());

        ListaEspera resultado = listaEsperaService.actualizarEstado(20L, Estado.ASIGNADA);

        assertThat(resultado.getEstado()).isEqualTo(Estado.ASIGNADA);
        verify(procedimientoRepository).actualizarEstado(20L, "ASIGNADA");
    }
}
