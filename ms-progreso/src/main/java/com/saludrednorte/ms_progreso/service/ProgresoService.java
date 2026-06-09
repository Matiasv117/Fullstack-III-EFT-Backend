package com.saludrednorte.ms_progreso.service;

import com.saludrednorte.ms_progreso.dto.ProgresoRequest;
import com.saludrednorte.ms_progreso.dto.ProgresoResponse;
import com.saludrednorte.ms_progreso.entity.EstadoProgreso;
import com.saludrednorte.ms_progreso.entity.ProgresoPaciente;
import com.saludrednorte.ms_progreso.exception.ProgresoDuplicadoException;
import com.saludrednorte.ms_progreso.exception.ProgresoNoEncontradoException;
import com.saludrednorte.ms_progreso.repository.ProgresoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio para gestionar el progreso del paciente.
 */
@Service
public class ProgresoService {

    private final ProgresoRepository progresoRepository;

    public ProgresoService(ProgresoRepository progresoRepository) {
        this.progresoRepository = progresoRepository;
    }

    /**
     * Registra el progreso inicial del paciente.
     *
     * @param pacienteId id del paciente
     * @param request estado inicial
     * @return progreso registrado
     */
    public ProgresoResponse registrarProgreso(Long pacienteId, ProgresoRequest request) {
        if (progresoRepository.findByPacienteId(pacienteId).isPresent()) {
            throw new ProgresoDuplicadoException(pacienteId);
        }

        ProgresoPaciente progreso = new ProgresoPaciente();
        progreso.setPacienteId(pacienteId);
        progreso.setEstado(parseEstado(request.getEstado()));
        progreso.setFechaActualizacion(LocalDateTime.now());

        ProgresoPaciente guardado = progresoRepository.save(progreso);
        return toResponse(guardado);
    }

    /**
     * Actualiza el estado del paciente.
     *
     * @param pacienteId id del paciente
     * @param request nuevo estado
     * @return progreso actualizado
     */
    public ProgresoResponse actualizarProgreso(Long pacienteId, ProgresoRequest request) {
        ProgresoPaciente progreso = progresoRepository.findByPacienteId(pacienteId)
                .orElseThrow(() -> new ProgresoNoEncontradoException(pacienteId));

        progreso.setEstado(parseEstado(request.getEstado()));
        progreso.setFechaActualizacion(LocalDateTime.now());
        ProgresoPaciente guardado = progresoRepository.save(progreso);
        return toResponse(guardado);
    }

    /**
     * Obtiene el progreso actual del paciente.
     *
     * @param pacienteId id del paciente
     * @return progreso actual
     */
    public ProgresoResponse obtenerProgreso(Long pacienteId) {
        ProgresoPaciente progreso = progresoRepository.findByPacienteId(pacienteId)
                .orElseThrow(() -> new ProgresoNoEncontradoException(pacienteId));

        return toResponse(progreso);
    }

    private EstadoProgreso parseEstado(String estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        return EstadoProgreso.valueOf(estado.trim().toUpperCase());
    }

    private ProgresoResponse toResponse(ProgresoPaciente progreso) {
        return new ProgresoResponse(
                progreso.getPacienteId(),
                progreso.getEstado().name(),
                progreso.getFechaActualizacion()
        );
    }
}
