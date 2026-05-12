package com.saludrednorte.ms_listas_espera.service;

import com.saludrednorte.ms_listas_espera.client.NotificationClient;
import com.saludrednorte.ms_listas_espera.dto.NotificationRequestDTO;
import com.saludrednorte.ms_listas_espera.entity.Estado;
import com.saludrednorte.ms_listas_espera.entity.Gravedad;
import com.saludrednorte.ms_listas_espera.entity.ListaEspera;
import com.saludrednorte.ms_listas_espera.entity.Paciente;
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
}


