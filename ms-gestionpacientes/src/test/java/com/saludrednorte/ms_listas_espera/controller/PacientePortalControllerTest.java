package com.saludrednorte.ms_listas_espera.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.ms_listas_espera.client.CitaClient;
import com.saludrednorte.ms_listas_espera.dto.CitaDTO;
import com.saludrednorte.ms_listas_espera.entity.Estado;
import com.saludrednorte.ms_listas_espera.entity.Gravedad;
import com.saludrednorte.ms_listas_espera.entity.ListaEspera;
import com.saludrednorte.ms_listas_espera.entity.Paciente;
import com.saludrednorte.ms_listas_espera.service.ListaEsperaService;
import com.saludrednorte.ms_listas_espera.service.PacienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PacientePortalControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PacienteService pacienteService;

    @Mock
    private ListaEsperaService listaEsperaService;

    @Mock
    private CitaClient citaClient;

    @InjectMocks
    private PacientePortalController pacientePortalController;

    private String validToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pacientePortalController).build();
        // {"sub":"PACIENTE_1"} base64url-encoded
        String payload = Base64.getUrlEncoder().encodeToString("{\"sub\":\"PACIENTE_1\"}".getBytes());
        validToken = "Bearer header." + payload + ".signature";
    }

    @Test
    void getMisDatos_debeRetornar200() throws Exception {
        Paciente paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNombre("Juan");
        paciente.setApellido("Perez");

        when(pacienteService.obtenerPacientePorId(1L)).thenReturn(Optional.of(paciente));

        mockMvc.perform(get("/pacientes/portal/mis-datos")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.apellido").value("Perez"));
    }

    @Test
    void getMisDatos_debeRetornar401SinToken() throws Exception {
        mockMvc.perform(get("/pacientes/portal/mis-datos"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMisDatos_debeRetornar401ConTokenInvalido() throws Exception {
        mockMvc.perform(get("/pacientes/portal/mis-datos")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMisDatos_debeRetornar404CuandoNoExiste() throws Exception {
        when(pacienteService.obtenerPacientePorId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/pacientes/portal/mis-datos")
                        .header("Authorization", validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMiPosicion_debeRetornar200() throws Exception {
        Paciente paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNombre("Juan");
        paciente.setApellido("Perez");

        ListaEspera le = new ListaEspera();
        le.setId(5L);
        le.setPaciente(paciente);
        le.setEstado(Estado.PENDIENTE);
        le.setGravedad(Gravedad.ALTA);

        List<ListaEspera> lista = List.of(
                le,
                new ListaEspera() {{ setId(1L); setPaciente(new Paciente() {{ setId(2L); }}); }},
                new ListaEspera() {{ setId(2L); setPaciente(new Paciente() {{ setId(3L); }}); }}
        );

        when(listaEsperaService.obtenerListaEspera()).thenReturn(lista);

        mockMvc.perform(get("/pacientes/portal/mi-posicion")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posicion").value(3))
                .andExpect(jsonPath("$.total").value(3));
    }

    @Test
    void getMiPosicion_debeRetornar200SinPosicionCuandoNoEnLista() throws Exception {
        List<ListaEspera> lista = List.of(
                new ListaEspera() {{ setId(1L); setPaciente(new Paciente() {{ setId(2L); }}); }}
        );

        when(listaEsperaService.obtenerListaEspera()).thenReturn(lista);

        mockMvc.perform(get("/pacientes/portal/mi-posicion")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posicion").isEmpty())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void getMiPosicion_debeRetornar401SinToken() throws Exception {
        mockMvc.perform(get("/pacientes/portal/mi-posicion"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMiPosicion_debeRetornar401ConTokenInvalido() throws Exception {
        mockMvc.perform(get("/pacientes/portal/mi-posicion")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMisCitas_debeRetornar200() throws Exception {
        CitaDTO cita = new CitaDTO(1L, 1L, 2L, java.time.LocalDateTime.now(), "CONFIRMADA");
        when(citaClient.obtenerCitasPorPaciente(1L)).thenReturn(List.of(cita));

        mockMvc.perform(get("/pacientes/portal/mis-citas")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].pacienteId").value(1));
    }

    @Test
    void getMisCitas_debeRetornar401ConTokenInvalido() throws Exception {
        mockMvc.perform(get("/pacientes/portal/mis-citas")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMisCitas_debeRetornar401SinToken() throws Exception {
        mockMvc.perform(get("/pacientes/portal/mis-citas"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void extractPacienteId_debeRetornarNullConTokenNoPaciente() throws Exception {
        // {"sub":"FUNCIONARIO_1"}
        String payload = Base64.getUrlEncoder().encodeToString("{\"sub\":\"FUNCIONARIO_1\"}".getBytes());
        String token = "Bearer header." + payload + ".signature";

        mockMvc.perform(get("/pacientes/portal/mis-datos")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token inválido o no es de paciente"));
    }

    @Test
    void extractPacienteId_debeRetornarNullConTokenMalformado() throws Exception {
        mockMvc.perform(get("/pacientes/portal/mis-datos")
                        .header("Authorization", "Bearer solo.partes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token inválido o no es de paciente"));
    }
}
