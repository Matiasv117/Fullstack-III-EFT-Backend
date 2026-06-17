package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.entity.Medico;
import com.saludrednorte.ms_optimizacion.repository.MedicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de médicos.
 * <p>
 * Este servicio proporciona operaciones CRUD para médicos del sistema.
 * </p>
 */
@Service
public class MedicoService {

    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * Registra un nuevo médico en el sistema.
     *
     * @param medico el médico a registrar
     * @return el médico registrado con ID asignado
     */
    public Medico registrarMedico(Medico medico) {
        return medicoRepository.save(medico);
    }

    /**
     * Obtiene todos los médicos registrados en el sistema.
     *
     * @return lista de todos los médicos
     */
    public List<Medico> obtenerTodosMedicos() {
        return medicoRepository.findAll();
    }

    /**
     * Obtiene un médico por su ID.
     *
     * @param id el ID del médico
     * @return Optional con el médico si existe, vacío si no existe
     */
    public Optional<Medico> obtenerMedicoPorId(Long id) {
        return medicoRepository.findById(id);
    }

    /**
     * Actualiza los datos de un médico existente.
     * <p>
     * Valida que el médico exista antes de actualizar.
     * </p>
     *
     * @param medico el médico con los datos actualizados
     * @return el médico actualizado
     * @throws ResponseStatusException si el médico no existe
     */
    public Medico actualizarMedico(Medico medico) {
        if (medico.getId() == null || !medicoRepository.existsById(medico.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medico no encontrado");
        }
        return medicoRepository.save(medico);
    }

    /**
     * Elimina un médico del sistema por su ID.
     *
     * @param id el ID del médico a eliminar
     * @throws ResponseStatusException si el médico no existe
     */
    public void eliminarMedico(Long id) {
        if (!medicoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medico no encontrado");
        }
        medicoRepository.deleteById(id);
    }
}
