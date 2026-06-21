package com.saludrednorte.ms_notificaciones.messaging;

import com.saludrednorte.ms_notificaciones.entity.EstadoNotificacion;
import com.saludrednorte.ms_notificaciones.entity.Notification;
import com.saludrednorte.ms_notificaciones.entity.TipoNotificacion;
import com.saludrednorte.ms_notificaciones.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificacionEventListener listener;

    @Test
    void procesarEvento_debeCrearYEnviarNotificacion() {
        NotificacionEvent evento = new NotificacionEvent();
        evento.setPacienteId(10L);
        evento.setTipo("PACIENTE_ASIGNADO");
        evento.setMensaje("Paciente registrado");
        evento.setOrigen("ms-gestionpacientes");

        Notification creada = new Notification();
        creada.setId(1L);
        creada.setPacienteId(10L);
        creada.setTipo(TipoNotificacion.PACIENTE_ASIGNADO);
        creada.setMensaje("Paciente registrado");
        creada.setEstado(EstadoNotificacion.PENDIENTE);

        when(notificationService.create(any(Notification.class))).thenReturn(creada);
        when(notificationService.sendById(1L)).thenReturn(true);

        listener.procesarEvento(evento);

        verify(notificationService).create(any(Notification.class));
        verify(notificationService).sendById(1L);
    }
}
