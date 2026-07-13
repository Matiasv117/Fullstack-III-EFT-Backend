package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.entity.Cita;
import com.saludrednorte.ms_optimizacion.entity.EstadoCita;
import com.saludrednorte.ms_optimizacion.repository.CitaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de citas médicas.
 * <p>
 * Este servicio proporciona operaciones CRUD para citas, incluyendo validaciones
 * de disponibilidad de médicos y estados de citas.
 * </p>
 */
@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    /**
     * Crea una nueva cita médica.
     * <p>
     * Valida que el médico y fecha/hora estén informados.
     * Verifica que el médico no tenga otra cita en el mismo horario.
     * Establece el estado como CONFIRMADA por defecto.
     * </p>
     *
     * @param cita la cita a crear
     * @return la cita creada con ID asignado
     * @throws ResponseStatusException si falta médico o fecha/hora, o si el médico ya tiene cita en ese horario
     */
    public Cita crearCita(Cita cita) {
        if (cita.getMedico() == null || cita.getFechaHora() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cita requiere medico y fecha/hora");
        }
        if (citaRepository.existsByMedicoAndFechaHoraAndEstadoNot(cita.getMedico(), cita.getFechaHora(), EstadoCita.CANCELADA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El medico ya tiene una cita en ese horario");
        }
        cita.setEstado(EstadoCita.CONFIRMADA);
        return citaRepository.save(cita);
    }

    /**
     * Obtiene todas las citas médicas.
     *
     * @return lista de todas las citas
     */
    @Transactional(readOnly = true)
    public List<Cita> obtenerTodasCitas() {
        return citaRepository.findAll();
    }

    /**
     * Obtiene las citas filtradas por estado.
     *
     * @param estado el estado de las citas a buscar
     * @return lista de citas con el estado especificado
     */
    public List<Cita> obtenerCitasPorEstado(EstadoCita estado) {
        return citaRepository.findByEstado(estado);
    }

    /**
     * Obtiene las citas de un paciente específico.
     *
     * @param pacienteId el ID del paciente
     * @return lista de citas del paciente
     */
    public List<Cita> obtenerCitasPorPaciente(Long pacienteId) {
        return citaRepository.findByPacienteId(pacienteId);
    }

    /**
     * Obtiene una cita por su ID.
     *
     * @param id el ID de la cita
     * @return Optional con la cita si existe, vacío si no existe
     */
    public Optional<Cita> obtenerCitaPorId(Long id) {
        return citaRepository.findById(id);
    }

    /**
     * Actualiza una cita existente.
     * <p>
     * Valida que la cita exista antes de actualizar.
     * </p>
     *
     * @param cita la cita con los datos actualizados
     * @return la cita actualizada
     * @throws ResponseStatusException si la cita no existe
     */
    public Cita actualizarCita(Cita cita) {
        if (cita.getId() == null || !citaRepository.existsById(cita.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada");
        }
        return citaRepository.save(cita);
    }

    /**
     * Cancela una cita existente.
     * <p>
     * Valida que la cita exista antes de cancelar.
     * Establece el estado como CANCELADA.
     * </p>
     *
     * @param id el ID de la cita a cancelar
     * @throws ResponseStatusException si la cita no existe
     */
    public void cancelarCita(Long id) {
        Optional<Cita> optional = citaRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada");
        }

        Cita cita = optional.get();
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    /**
     * Elimina una cita del sistema.
     * <p>
     * Valida que la cita exista antes de eliminar.
     * </p>
     *
     * @param id el ID de la cita a eliminar
     * @throws ResponseStatusException si la cita no existe
     */
    public void eliminarCita(Long id) {
        if (!citaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada");
        }
        citaRepository.deleteById(id);
    }

    @Transactional
    public void eliminarCitasPorPaciente(Long pacienteId) {
        citaRepository.deleteByPacienteId(pacienteId);
    }
}
