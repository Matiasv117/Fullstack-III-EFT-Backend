package com.saludrednorte.ms_notificaciones.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private ServletWebRequest webRequest;

    @BeforeEach
    void setUp() throws Exception {
        handler = new GlobalExceptionHandler();

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/mi-path");
        webRequest = mock(ServletWebRequest.class);
        when(webRequest.getRequest()).thenReturn(req);

        // set serviceName field via reflection to have predictable value
        Field f = GlobalExceptionHandler.class.getDeclaredField("serviceName");
        f.setAccessible(true);
        f.set(handler, "ms-notificaciones-test");
    }

    @Test
    void handleIllegalArgumentException_returnsBadRequest() {
        var ex = new IllegalArgumentException("dato invalido");
        var resp = handler.handleIllegalArgumentException(ex, webRequest);

        assertEquals(400, resp.getStatusCodeValue());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals("dato invalido", body.get("message"));
        assertEquals("/mi-path", body.get("path"));
        assertEquals("ms-notificaciones-test", body.get("service"));
    }

    @Test
    void handleResponseStatus_returnsConfiguredStatusAndReason() {
        var ex = new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "ya existe");
        var resp = handler.handleResponseStatus(ex, webRequest);

        assertEquals(409, resp.getStatusCodeValue());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals("ya existe", body.get("message"));
    }

    @Test
    void handleGlobalException_returnsInternalServerError() {
        var ex = new RuntimeException("boom");
        var resp = handler.handleGlobalException(ex, webRequest);

        assertEquals(500, resp.getStatusCodeValue());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals("Error interno del servidor", body.get("message"));
    }
}


