package com.saludrednorte.ms_notificaciones.controller;

import com.saludrednorte.ms_notificaciones.dto.ChannelInfoDTO;
import com.saludrednorte.ms_notificaciones.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationInfoControllerTest {

    @Mock
    private NotificationService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new NotificationInfoController(service)).build();
    }

    @Test
    void testCanalesDisponibles() throws Exception {
        when(service.getAvailableChannels()).thenReturn(List.of("EMAIL", "SMS", "PUSH"));

        mockMvc.perform(get("/api/notificaciones/info/canales"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("EMAIL"))
                .andExpect(jsonPath("$[0].description").value("Canal de notificación: EMAIL"))
                .andExpect(jsonPath("$[1].name").value("SMS"))
                .andExpect(jsonPath("$[2].name").value("PUSH"));
    }

    @Test
    void testEstado() throws Exception {
        mockMvc.perform(get("/api/notificaciones/info/estado"))
                .andExpect(status().isOk())
                .andExpect(content().string("Microservicio de notificaciones operacional"));
    }
}
