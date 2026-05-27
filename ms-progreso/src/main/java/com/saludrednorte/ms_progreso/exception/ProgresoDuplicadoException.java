package com.saludrednorte.ms_progreso.exception;

/**
 * Se lanza cuando el progreso del paciente ya existe.
 */
public class ProgresoDuplicadoException extends RuntimeException {

    public ProgresoDuplicadoException(Long pacienteId) {
        super("El progreso para el paciente " + pacienteId + " ya existe");
    }
}

