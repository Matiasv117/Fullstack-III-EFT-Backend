package com.saludrednorte.ms_auditoria.messaging;

import com.saludrednorte.ms_auditoria.dto.AuditEventRequest;
import com.saludrednorte.ms_auditoria.service.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventListenerTest {

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private AuditEventListener listener;

    @Test
    void procesarEvento_debeRegistrarEnAuditoriaService() {
        AuditEventMessage evento = new AuditEventMessage();
        evento.setUsername("admin");
        evento.setAction("LOGIN_EXITOSO");
        evento.setDetails("Login ok");
        evento.setOrigen("ms-auth");

        listener.procesarEvento(evento);

        ArgumentCaptor<AuditEventRequest> captor = ArgumentCaptor.forClass(AuditEventRequest.class);
        verify(auditoriaService).registrarEvento(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getAction()).isEqualTo("LOGIN_EXITOSO");
    }
}
