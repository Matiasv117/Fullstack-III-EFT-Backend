package com.saludrednorte.ms_listas_espera.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PacienteTest {

    @Test
    void testPacienteGettersSetters() {
        Paciente paciente = new Paciente();

        paciente.setId(1L);
        paciente.setNombre("Carlos");
        paciente.setApellido("García");
        paciente.setDni("12345678-9");
        paciente.setTelefono("555-1234");
        paciente.setEmail("carlos@email.com");

        assertThat(paciente.getId()).isEqualTo(1L);
        assertThat(paciente.getNombre()).isEqualTo("Carlos");
        assertThat(paciente.getApellido()).isEqualTo("García");
        assertThat(paciente.getDni()).isEqualTo("12345678-9");
        assertThat(paciente.getTelefono()).isEqualTo("555-1234");
        assertThat(paciente.getEmail()).isEqualTo("carlos@email.com");
    }

    @Test
    void testPacienteValoresNulos() {
        Paciente paciente = new Paciente();

        assertThat(paciente.getId()).isNull();
        assertThat(paciente.getNombre()).isNull();
        assertThat(paciente.getApellido()).isNull();
        assertThat(paciente.getDni()).isNull();
        assertThat(paciente.getTelefono()).isNull();
        assertThat(paciente.getEmail()).isNull();
    }

    @Test
    void testPacienteIdNegativo() {
        Paciente paciente = new Paciente();
        paciente.setId(-1L);

        assertThat(paciente.getId()).isEqualTo(-1L);
    }
}

