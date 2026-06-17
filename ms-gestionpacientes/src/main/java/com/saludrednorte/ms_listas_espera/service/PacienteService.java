package com.saludrednorte.ms_listas_espera.service;

import com.saludrednorte.ms_listas_espera.client.CitaClient;
import com.saludrednorte.ms_listas_espera.client.NotificationClient;
import com.saludrednorte.ms_listas_espera.dto.CitaDTO;
import com.saludrednorte.ms_listas_espera.dto.MedicoDTO;
import com.saludrednorte.ms_listas_espera.dto.NotificationRequestDTO;
import com.saludrednorte.ms_listas_espera.entity.Paciente;
import com.saludrednorte.ms_listas_espera.repository.PacienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de pacientes.
 * <p>
 * Este servicio proporciona operaciones CRUD para pacientes, incluyendo registro automático
 * de citas y notificaciones cuando se registra un nuevo paciente.
 * </p>
 */
@Service
public class PacienteService {

    private static final Logger logger = LoggerFactory.getLogger(PacienteService.class);

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private CitaClient citaClient;

    /**
     * Registra un nuevo paciente en el sistema.
     * <p>
     * Valida que no exista un paciente con el mismo DNI antes de registrar.
     * Crea automáticamente una cita con el primer médico disponible y envía una notificación.
     * </p>
     *
     * @param paciente el paciente a registrar
     * @return el paciente registrado con ID asignado
     * @throws ResponseStatusException si ya existe un paciente con el mismo DNI
     */
    public Paciente registrarPaciente(Paciente paciente) {
        if (paciente.getDni() != null && pacienteRepository.existsByDniIgnoreCase(paciente.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un paciente con el DNI indicado");
        }

        Paciente savedPaciente = pacienteRepository.save(paciente);

        // Crear cita automáticamente con el primer médico disponible
        try {
            List<MedicoDTO> medicos = citaClient.obtenerTodosMedicos();
            if (!medicos.isEmpty()) {
                MedicoDTO medico = medicos.get(0);
                CitaDTO cita = new CitaDTO();
                cita.setPacienteId(savedPaciente.getId());
                cita.setMedicoId(medico.getId());
                cita.setFechaHora(LocalDateTime.now().plusDays(1));
                cita.setEstado("CONFIRMADA");
                citaClient.crearCita(cita);
                logger.info("Cita creada automáticamente para paciente {} con médico {}", 
                           savedPaciente.getId(), medico.getId());
            }
        } catch (Exception e) {
            logger.warn("Fallo al crear cita automática pero paciente registrado: {}", e.getMessage());
        }

        // Crear notificación automáticamente
        try {
            NotificationRequestDTO notif = new NotificationRequestDTO();
            notif.setPacienteId(savedPaciente.getId());
            notif.setTipo("PACIENTE_ASIGNADO");
            notif.setMensaje("Paciente " + savedPaciente.getNombre() + " " +
                            savedPaciente.getApellido() + " registrado en el sistema");
            notificationClient.createNotification(notif);
            logger.info("Notificación creada para paciente {}", savedPaciente.getId());
        } catch (Exception e) {
            logger.warn("Fallo al crear notificación pero paciente registrado: {}", e.getMessage());
        }

        return savedPaciente;
    }

    /**
     * Obtiene todos los pacientes registrados en el sistema.
     *
     * @return lista de todos los pacientes
     */
    public List<Paciente> obtenerTodosPacientes() {
        return pacienteRepository.findAll();
    }

    /**
     * Obtiene un paciente por su ID.
     *
     * @param id el ID del paciente
     * @return Optional con el paciente si existe, vacío si no existe
     */
    public Optional<Paciente> obtenerPacientePorId(Long id) {
        return pacienteRepository.findById(id);
    }

    /**
     * Actualiza los datos de un paciente existente.
     * <p>
     * Valida que el paciente exista antes de actualizar.
     * Envía una notificación automática cuando se actualiza el paciente.
     * </p>
     *
     * @param paciente el paciente con los datos actualizados
     * @return el paciente actualizado
     * @throws ResponseStatusException si el paciente no existe
     */
    public Paciente actualizarPaciente(Paciente paciente) {
        if (paciente.getId() == null || !pacienteRepository.existsById(paciente.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }

        Paciente updatedPaciente = pacienteRepository.save(paciente);

        // Notificar actualización
        try {
            NotificationRequestDTO notif = new NotificationRequestDTO();
            notif.setPacienteId(updatedPaciente.getId());
            notif.setTipo("ACTUALIZACION_ESTADO");
            notif.setMensaje("Datos del paciente " + updatedPaciente.getNombre() + " actualizados");
            notificationClient.createNotification(notif);
            logger.info("Notificación de actualización enviada para paciente {}", updatedPaciente.getId());
        } catch (Exception e) {
            logger.warn("Fallo al notificar actualización pero paciente actualizado: {}", e.getMessage());
        }

        return updatedPaciente;
    }

    /**
     * Elimina un paciente del sistema por su ID.
     *
     * @param id el ID del paciente a eliminar
     * @throws ResponseStatusException si el paciente no existe
     */
    public void eliminarPaciente(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
        pacienteRepository.deleteById(id);
    }
}
