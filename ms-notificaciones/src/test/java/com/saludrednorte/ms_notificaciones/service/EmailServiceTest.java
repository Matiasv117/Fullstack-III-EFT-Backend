package com.saludrednorte.ms_notificaciones.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "from", "noreply@saludrednorte.cl");
    }

    @Test
    void testSendEmail_smtpNoConfigurado_mailSenderNull() {
        ReflectionTestUtils.setField(emailService, "mailSender", null);
        ReflectionTestUtils.setField(emailService, "smtpHost", "smtp.example.com");

        emailService.sendEmail("test@test.com", "Asunto", "Cuerpo");
    }

    @Test
    void testSendEmail_smtpNoConfigurado_hostNull() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
        ReflectionTestUtils.setField(emailService, "smtpHost", null);

        emailService.sendEmail("test@test.com", "Asunto", "Cuerpo");
    }

    @Test
    void testSendEmail_smtpNoConfigurado_hostBlank() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
        ReflectionTestUtils.setField(emailService, "smtpHost", "");

        emailService.sendEmail("test@test.com", "Asunto", "Cuerpo");
    }

    @Test
    void testSendEmail_exitoso() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
        ReflectionTestUtils.setField(emailService, "smtpHost", "smtp.example.com");

        emailService.sendEmail("test@test.com", "Asunto", "Cuerpo");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmail_excepcion() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
        ReflectionTestUtils.setField(emailService, "smtpHost", "smtp.example.com");

        doThrow(new RuntimeException("Error SMTP")).when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmail("test@test.com", "Asunto", "Cuerpo");
    }
}
