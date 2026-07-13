package com.saludrednorte.ms_listas_espera.service;

import com.saludrednorte.ms_listas_espera.client.CitaClient;
import com.saludrednorte.ms_listas_espera.messaging.AuditEventPublisher;
import com.saludrednorte.ms_listas_espera.messaging.NotificacionEventPublisher;
import com.saludrednorte.ms_listas_espera.entity.Paciente;
import com.saludrednorte.ms_listas_espera.repository.PacienteRepository;
import com.saludrednorte.ms_listas_espera.repository.ListaEsperaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static com.saludrednorte.ms_listas_espera.config.CacheConfig.CACHE_PACIENTES;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de pacientes.
 * <p>
 * Este servicio proporciona operaciones CRUD para pacientes, incluyendo registro
 * y notificaciones cuando se registra un nuevo paciente.
 * </p>
 */
@Service
public class PacienteService {

    private static final Logger logger = LoggerFactory.getLogger(PacienteService.class);

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ListaEsperaRepository listaEsperaRepository;

    @Autowired
    private CitaClient citaClient;

    @Autowired
    private NotificacionEventPublisher notificacionEventPublisher;

    @Autowired
    private AuditEventPublisher auditEventPublisher;

    /**
     * Registra un nuevo paciente en el sistema.
     * <p>
     * Valida que no exista un paciente con el mismo DNI antes de registrar.
     * Envía una notificación de bienvenida.
     * </p>
     *
     * @param paciente el paciente a registrar
     * @return el paciente registrado con ID asignado
     * @throws ResponseStatusException si ya existe un paciente con el mismo DNI
     */
    @CacheEvict(value = CACHE_PACIENTES, allEntries = true)
    public Paciente registrarPaciente(Paciente paciente) {
        if (paciente.getDni() != null && pacienteRepository.existsByDniIgnoreCase(paciente.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un paciente con el DNI indicado");
        }

        Paciente savedPaciente = pacienteRepository.save(paciente);

        // Publicar notificación de forma asíncrona vía RabbitMQ
        try {
            notificacionEventPublisher.publicar(
                    savedPaciente.getId(),
                    "PACIENTE_ASIGNADO",
                    "Paciente " + savedPaciente.getNombre() + " " +
                            savedPaciente.getApellido() + " registrado en el sistema"
            );
            logger.info("Evento de notificación publicado para paciente {}", savedPaciente.getId());
        } catch (Exception e) {
            logger.warn("Fallo al publicar notificación pero paciente registrado: {}", e.getMessage());
        }

        auditEventPublisher.publicar("PACIENTE_REGISTRADO",
                "Paciente ID " + savedPaciente.getId() + " - " + savedPaciente.getNombre() + " " + savedPaciente.getApellido());

        return savedPaciente;
    }

    /**
     * Obtiene todos los pacientes registrados en el sistema.
     *
     * @return lista de todos los pacientes
     */
    @Cacheable(CACHE_PACIENTES)
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
    @CacheEvict(value = CACHE_PACIENTES, allEntries = true)
    public Paciente actualizarPaciente(Paciente paciente) {
        if (paciente.getId() == null || !pacienteRepository.existsById(paciente.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }

        Paciente updatedPaciente = pacienteRepository.save(paciente);

        // Publicar notificación de actualización vía RabbitMQ
        try {
            notificacionEventPublisher.publicar(
                    updatedPaciente.getId(),
                    "ACTUALIZACION_ESTADO",
                    "Datos del paciente " + updatedPaciente.getNombre() + " actualizados"
            );
            logger.info("Evento de actualización publicado para paciente {}", updatedPaciente.getId());
        } catch (Exception e) {
            logger.warn("Fallo al publicar notificación pero paciente actualizado: {}", e.getMessage());
        }

        return updatedPaciente;
    }

    /**
     * Elimina un paciente del sistema por su ID.
     *
     * @param id el ID del paciente a eliminar
     * @throws ResponseStatusException si el paciente no existe
     */
    @CacheEvict(value = CACHE_PACIENTES, allEntries = true)
    @Transactional
    public void eliminarPaciente(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
        try {
            // Eliminar entidades relacionadas primero
            listaEsperaRepository.deleteByPacienteId(id);
            try {
                citaClient.eliminarCitasPorPaciente(id);
            } catch (Exception e) {
                logger.warn("No se pudieron eliminar citas del paciente {}: {}", id, e.getMessage());
            }
            pacienteRepository.deleteById(id);
        } catch (Exception e) {
            logger.warn("Error eliminando paciente {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo eliminar el paciente: " + e.getMessage());
        }
    }

    /**
     * Busca un paciente por nombre, apellido y RUT.
     *
     * @param nombre el nombre del paciente
     * @param apellido el apellido del paciente
     * @param dni el RUT/DNI del paciente
     * @return Optional con el paciente si existe, vacío si no existe
     */
    public Optional<Paciente> buscarPorNombreApellidoDni(String nombre, String apellido, String dni) {
        return pacienteRepository.findByNombreIgnoreCaseAndApellidoIgnoreCaseAndDniIgnoreCase(nombre, apellido, dni);
    }
}
