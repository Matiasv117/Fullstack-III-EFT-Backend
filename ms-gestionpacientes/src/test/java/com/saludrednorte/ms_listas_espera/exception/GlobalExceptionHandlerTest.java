package com.saludrednorte.ms_listas_espera.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private ServletWebRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/test-path");
        request = new ServletWebRequest(mockRequest);
    }

    @Test
    void handleResponseStatus_withReason() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso no encontrado");
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResponseStatus(ex, request);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Recurso no encontrado", body.get("message"));
        assertEquals(404, body.get("status"));
    }

    @Test
    void handleResponseStatus_withoutReason() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST);
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResponseStatus(ex, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("400 BAD_REQUEST", body.get("message"));
    }

    @Test
    void handleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Argumento inválido");
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgument(ex, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Argumento inválido", body.get("message"));
        assertEquals("IllegalArgumentException", body.get("error"));
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("Error genérico");
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneric(ex, request);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Error interno del servidor", body.get("message"));
        assertEquals("Exception", body.get("error"));
    }


    @Test
    void responseContainsTimestamp() {
        Exception ex = new Exception("Error");
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneric(ex, request);
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void responseContainsPath() {
        Exception ex = new Exception("Error");
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGeneric(ex, request);
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("/test-path", body.get("path"));
    }

}
