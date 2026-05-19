package com.saludrednorte.ms_notificaciones.scheduler;

import com.saludrednorte.ms_notificaciones.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class NotificationSchedulerTest {

    private NotificationService service;
    private NotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        service = mock(NotificationService.class);
        scheduler = new NotificationScheduler(service);
    }

    @Test
    void enviarNotificacionesPendientes_invokesService() {
        scheduler.enviarNotificacionesPendientes();
        verify(service, times(1)).sendPending();
    }

    @Test
    void enviarNotificacionesPendientes_handlesExceptionGracefully() {
        doThrow(new RuntimeException("boom")).when(service).sendPending();
        // should not throw
        scheduler.enviarNotificacionesPendientes();
        verify(service, times(1)).sendPending();
    }
}

