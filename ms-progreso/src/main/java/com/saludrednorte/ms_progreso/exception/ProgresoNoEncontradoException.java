package com.saludrednorte.ms_progreso.exception;

/**
 * Se lanza cuando no existe el progreso del paciente solicitado.
 */
public class ProgresoNoEncontradoException extends RuntimeException {

    public ProgresoNoEncontradoException(Long pacienteId) {
        super("No se encontro progreso para el paciente " + pacienteId);
    }
}

