package com.saludrednorte.ms_notificaciones;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.ms_notificaciones.dto.NotificationRequestDTO;
import com.saludrednorte.ms_notificaciones.dto.NotificationResponseDTO;
import com.saludrednorte.ms_notificaciones.entity.TipoNotificacion;
import com.saludrednorte.ms_notificaciones.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para el microservicio de notificaciones.
 * Prueba el flujo completo de la aplicación sin depender de AutoConfigureMockMvc.
 */
@DisplayName("Tests de Integración - Microservicio de Notificaciones")
@SpringBootTest
@ActiveProfiles("test")
class MsNotificacionesIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        repository.deleteAll();
    }

    @Test
    @DisplayName("Flujo completo: Crear y enviar notificación")
    void testCompleteNotificationFlow() throws Exception {
        NotificationRequestDTO requestDTO = new NotificationRequestDTO();
        requestDTO.setPacienteId(123L);
        requestDTO.setTipo(TipoNotificacion.CITA_CONFIRMADA);
        requestDTO.setMensaje("Su cita ha sido confirmada para mañana a las 10:00");

        MvcResult createResult = mockMvc.perform(post("/api/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn();

        NotificationResponseDTO created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                NotificationResponseDTO.class
        );

        assertNotNull(created.getId());
        assertEquals("PENDIENTE", created.getEstado().name());

        mockMvc.perform(post("/api/notificaciones/{id}/enviar", created.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Health check del microservicio")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/notificaciones/info/estado"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("operacional")));
    }
}
