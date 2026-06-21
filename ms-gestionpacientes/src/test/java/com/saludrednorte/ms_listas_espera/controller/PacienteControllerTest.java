package com.saludrednorte.ms_listas_espera.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.ms_listas_espera.entity.Paciente;
import com.saludrednorte.ms_listas_espera.service.PacienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PacienteControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PacienteService pacienteService;

    @InjectMocks
    private PacienteController pacienteController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pacienteController).build();
    }

    @Test
    void registrarPaciente_debeRetornarPaciente() throws Exception {
        Paciente paciente = new Paciente();
        paciente.setNombre("Ana");
        paciente.setApellido("Pérez");
        Paciente guardado = new Paciente();
        guardado.setId(1L);
        guardado.setNombre("Ana");
        when(pacienteService.registrarPaciente(any(Paciente.class))).thenReturn(guardado);

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerTodosPacientes_debeRetornarLista() throws Exception {
        Paciente paciente = new Paciente();
        paciente.setId(1L);
        when(pacienteService.obtenerTodosPacientes()).thenReturn(List.of(paciente));

        mockMvc.perform(get("/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerPacientePorId_debeRetornar200CuandoExiste() throws Exception {
        Paciente paciente = new Paciente();
        paciente.setId(5L);
        when(pacienteService.obtenerPacientePorId(5L)).thenReturn(Optional.of(paciente));

        mockMvc.perform(get("/pacientes/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void obtenerPacientePorId_debeRetornar404CuandoNoExiste() throws Exception {
        when(pacienteService.obtenerPacientePorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/pacientes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarPaciente_debeRetornarPacienteActualizado() throws Exception {
        Paciente paciente = new Paciente();
        paciente.setId(3L);
        paciente.setNombre("Roberto");
        when(pacienteService.actualizarPaciente(any(Paciente.class))).thenReturn(paciente);

        mockMvc.perform(put("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Roberto"));
    }

    @Test
    void eliminarPaciente_debeInvocarServicio() throws Exception {
        mockMvc.perform(delete("/pacientes/7"))
                .andExpect(status().isOk());

        verify(pacienteService).eliminarPaciente(7L);
    }
}
