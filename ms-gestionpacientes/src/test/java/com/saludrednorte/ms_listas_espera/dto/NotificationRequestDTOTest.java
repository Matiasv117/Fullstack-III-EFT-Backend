package com.saludrednorte.ms_listas_espera.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRequestDTOTest {

    @Test
    void testNotificationRequestDTOConstructorVacio() {
        NotificationRequestDTO dto = new NotificationRequestDTO();

        assertThat(dto.getPacienteId()).isNull();
        assertThat(dto.getTipo()).isNull();
        assertThat(dto.getMensaje()).isNull();
    }

    @Test
    void testNotificationRequestDTOConstructorConParametros() {
        NotificationRequestDTO dto = new NotificationRequestDTO(1L, "TIPO_TEST", "Mensaje de prueba");

        assertThat(dto.getPacienteId()).isEqualTo(1L);
        assertThat(dto.getTipo()).isEqualTo("TIPO_TEST");
        assertThat(dto.getMensaje()).isEqualTo("Mensaje de prueba");
    }

    @Test
    void testNotificationRequestDTOGettersSetters() {
        NotificationRequestDTO dto = new NotificationRequestDTO();

        dto.setPacienteId(5L);
        dto.setTipo("ACTUALIZACION");
        dto.setMensaje("Mensaje de notificación");

        assertThat(dto.getPacienteId()).isEqualTo(5L);
        assertThat(dto.getTipo()).isEqualTo("ACTUALIZACION");
        assertThat(dto.getMensaje()).isEqualTo("Mensaje de notificación");
    }

    @Test
    void testNotificationRequestDTOModificacion() {
        NotificationRequestDTO dto = new NotificationRequestDTO(1L, "TIPO_1", "Mensaje 1");

        dto.setPacienteId(2L);
        dto.setTipo("TIPO_2");
        dto.setMensaje("Mensaje 2");

        assertThat(dto.getPacienteId()).isEqualTo(2L);
        assertThat(dto.getTipo()).isEqualTo("TIPO_2");
        assertThat(dto.getMensaje()).isEqualTo("Mensaje 2");
    }

    @Test
    void testNotificationRequestDTOValoresNegosOVacios() {
        NotificationRequestDTO dto = new NotificationRequestDTO();

        dto.setPacienteId(-1L);
        dto.setTipo("");
        dto.setMensaje("");

        assertThat(dto.getPacienteId()).isEqualTo(-1L);
        assertThat(dto.getTipo()).isEqualTo("");
        assertThat(dto.getMensaje()).isEqualTo("");
    }
}

