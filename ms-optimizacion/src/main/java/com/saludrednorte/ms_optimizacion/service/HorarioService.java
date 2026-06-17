package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.entity.Horario;
import com.saludrednorte.ms_optimizacion.entity.Medico;
import com.saludrednorte.ms_optimizacion.repository.HorarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de horarios médicos.
 * <p>
 * Este servicio proporciona operaciones CRUD para horarios de médicos,
 * incluyendo consulta de horarios disponibles por médico y fecha.
 * </p>
 */
@Service
public class HorarioService {

    @Autowired
    private HorarioRepository horarioRepository;

    /**
     * Crea un nuevo horario médico.
     *
     * @param horario el horario a crear
     * @return el horario creado con ID asignado
     */
    public Horario crearHorario(Horario horario) {
        return horarioRepository.save(horario);
    }

    /**
     * Obtiene todos los horarios registrados en el sistema.
     *
     * @return lista de todos los horarios
     */
    public List<Horario> obtenerTodosHorarios() {
        return horarioRepository.findAll();
    }

    /**
     * Obtiene los horarios disponibles para un médico en una fecha específica.
     *
     * @param medico el médico
     * @param fecha la fecha a consultar
     * @return lista de horarios disponibles
     */
    public List<Horario> obtenerHorariosDisponibles(Medico medico, LocalDate fecha) {
        return horarioRepository.findByMedicoAndFechaAndDisponible(medico, fecha, true);
    }

    /**
     * Obtiene un horario por su ID.
     *
     * @param id el ID del horario
     * @return Optional con el horario si existe, vacío si no existe
     */
    public Optional<Horario> obtenerHorarioPorId(Long id) {
        return horarioRepository.findById(id);
    }

    /**
     * Actualiza un horario existente.
     * <p>
     * Valida que el horario exista antes de actualizar.
     * </p>
     *
     * @param horario el horario con los datos actualizados
     * @return el horario actualizado
     * @throws ResponseStatusException si el horario no existe
     */
    public Horario actualizarHorario(Horario horario) {
        if (horario.getId() == null || !horarioRepository.existsById(horario.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Horario no encontrado");
        }
        return horarioRepository.save(horario);
    }

    /**
     * Elimina un horario del sistema por su ID.
     *
     * @param id el ID del horario a eliminar
     * @throws ResponseStatusException si el horario no existe
     */
    public void eliminarHorario(Long id) {
        if (!horarioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Horario no encontrado");
        }
        horarioRepository.deleteById(id);
    }
}
