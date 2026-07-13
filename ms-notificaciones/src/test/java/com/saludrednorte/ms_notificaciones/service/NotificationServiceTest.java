package com.saludrednorte.ms_notificaciones.service;

import com.saludrednorte.ms_notificaciones.entity.EstadoNotificacion;
import com.saludrednorte.ms_notificaciones.entity.Notification;
import com.saludrednorte.ms_notificaciones.entity.TipoNotificacion;
import com.saludrednorte.ms_notificaciones.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests unitarios para NotificationService
 * Valida la lógica de creación, envío y gestión de notificaciones
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService service;

    private Notification notification;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "emailService", emailService);
        notification = new Notification();
        notification.setId(1L);
        notification.setPacienteId(123L);
        notification.setTipo(TipoNotificacion.CITA_CONFIRMADA);
        notification.setMensaje("Su cita ha sido confirmada");
        notification.setEstado(EstadoNotificacion.PENDIENTE);
        notification.setIntentosEnvio(0);
    }

    @Test
    void testCreateNotification() {
        when(repository.save(any(Notification.class))).thenReturn(notification);

        Notification result = service.create(notification);

        assertThat(result).isNotNull();
        assertThat(result.getEstado()).isEqualTo(EstadoNotificacion.PENDIENTE);
        verify(repository, times(1)).save(notification);
    }

    @Test
    void testFindPending() {
        when(repository.findByEstado(EstadoNotificacion.PENDIENTE)).thenReturn(List.of(notification));

        List<Notification> result = service.findPending();

        assertThat(result).hasSize(1);
        verify(repository, times(1)).findByEstado(EstadoNotificacion.PENDIENTE);
    }

    @Test
    void testFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(notification));

        Optional<Notification> result = service.findById(1L);

        assertThat(result).isPresent();
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testSendById() {
        when(repository.findById(1L)).thenReturn(Optional.of(notification));
        when(repository.save(any(Notification.class))).thenReturn(notification);

        boolean result = service.sendById(1L);

        assertThat(result).isTrue();
        verify(repository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendByIdNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        boolean result = service.sendById(999L);

        assertThat(result).isFalse();
        verify(repository, times(1)).findById(999L);
    }

    @Test
    void testFindByPacienteId() {
        when(repository.findByPacienteId(123L)).thenReturn(List.of(notification));

        List<Notification> result = service.findByPacienteId(123L);

        assertThat(result).hasSize(1);
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(notification));

        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void testSendPending() {
        when(repository.findByEstado(EstadoNotificacion.PENDIENTE)).thenReturn(List.of(notification));
        when(repository.save(any(Notification.class))).thenReturn(notification);

        service.sendPending();

        verify(repository).save(any(Notification.class));
    }

    @Test
    void testSendByIdWithChannel() {
        when(repository.findById(1L)).thenReturn(Optional.of(notification));
        when(repository.save(any(Notification.class))).thenReturn(notification);

        assertThat(service.sendById(1L, "SMS")).isTrue();
    }

    @Test
    void testGetAvailableChannels() {
        assertThat(service.getAvailableChannels()).containsExactly("EMAIL", "SMS", "PUSH");
    }

    @Test
    void testCreateWithNullCreadoAt() {
        notification.setCreadoAt(null);
        when(repository.save(any(Notification.class))).thenReturn(notification);

        Notification result = service.create(notification);

        assertThat(result).isNotNull();
        assertThat(result.getCreadoAt()).isNotNull();
    }

    @Test
    void testCreateWithNullIntentosEnvio() {
        notification.setIntentosEnvio(null);
        when(repository.save(any(Notification.class))).thenReturn(notification);

        Notification result = service.create(notification);

        assertThat(result).isNotNull();
        assertThat(result.getIntentosEnvio()).isZero();
    }

    @Test
    void testSendPendingWithEmailDestino() {
        notification.setEmailDestino("paciente@test.com");
        when(repository.findByEstado(EstadoNotificacion.PENDIENTE)).thenReturn(List.of(notification));
        when(repository.save(any(Notification.class))).thenReturn(notification);

        service.sendPending();

        verify(repository).save(any(Notification.class));
    }

    @Test
    void testSendPendingWithBlankEmailDestino() {
        notification.setEmailDestino("");
        when(repository.findByEstado(EstadoNotificacion.PENDIENTE)).thenReturn(List.of(notification));
        when(repository.save(any(Notification.class))).thenReturn(notification);

        service.sendPending();

        verify(repository).save(any(Notification.class));
    }

    @Test
    void testSendByIdWithNullChannel() {
        when(repository.findById(1L)).thenReturn(Optional.of(notification));
        when(repository.save(any(Notification.class))).thenReturn(notification);

        boolean result = service.sendById(1L, null);

        assertThat(result).isTrue();
    }

    @Test
    void testCreateNullNotification() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.create(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testCreateDuplicatePending() {
        when(repository.existsByPacienteIdAndTipoAndMensajeAndEstado(
                notification.getPacienteId(), notification.getTipo(), notification.getMensaje(), EstadoNotificacion.PENDIENTE))
                .thenReturn(true);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.create(notification))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void testSendByIdInvalidChannel() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.sendById(1L, "WHATSAPP"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
