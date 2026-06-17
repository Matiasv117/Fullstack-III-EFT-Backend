package com.saludrednorte.ms_listas_espera.service;

import com.saludrednorte.ms_listas_espera.client.NotificationClient;
import com.saludrednorte.ms_listas_espera.dto.NotificationRequestDTO;
import com.saludrednorte.ms_listas_espera.entity.Estado;
import com.saludrednorte.ms_listas_espera.entity.Gravedad;
import com.saludrednorte.ms_listas_espera.entity.ListaEspera;
import com.saludrednorte.ms_listas_espera.repository.ListaEsperaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de la lista de espera de pacientes.
 * <p>
 * Este servicio proporciona operaciones para agregar, consultar, actualizar y eliminar
 * pacientes de la lista de espera, con envío automático de notificaciones.
 * </p>
 */
@Service
public class ListaEsperaService {

    private static final Logger logger = LoggerFactory.getLogger(ListaEsperaService.class);

    @Autowired
    private ListaEsperaRepository listaEsperaRepository;

    @Autowired
    private NotificationClient notificationClient;

    /**
     * Agrega un paciente a la lista de espera.
     * <p>
     * Valida que el paciente esté informado antes de agregar.
     * Establece el estado como PENDIENTE y envía una notificación automática.
     * </p>
     *
     * @param listaEspera el registro de lista de espera a agregar
     * @return el registro de lista de espera guardado
     * @throws ResponseStatusException si el paciente no está informado
     */
    public ListaEspera agregarAListaEspera(ListaEspera listaEspera) {
        if (listaEspera.getPaciente() == null || listaEspera.getPaciente().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe informar el paciente asociado");
        }
        listaEspera.setEstado(Estado.PENDIENTE);
        ListaEspera listaEsperaGuardada = listaEsperaRepository.save(listaEspera);
        enviarNotificacion(listaEsperaGuardada, "PACIENTE_ASIGNADO");
        return listaEsperaGuardada;
    }

    /**
     * Obtiene todos los registros de la lista de espera.
     *
     * @return lista de todos los registros de lista de espera
     */
    public List<ListaEspera> obtenerListaEspera() {
        return listaEsperaRepository.findAll();
    }

    /**
     * Obtiene los registros de la lista de espera filtrados por estado.
     *
     * @param estado el estado a filtrar
     * @return lista de registros con el estado especificado
     */
    public List<ListaEspera> obtenerPorEstado(Estado estado) {
        return listaEsperaRepository.findByEstado(estado);
    }

    /**
     * Obtiene los registros de la lista de espera filtrados por gravedad.
     * <p>
     * Los resultados se ordenan por ID de forma ascendente.
     * </p>
     *
     * @param gravedad la gravedad a filtrar
     * @return lista de registros con la gravedad especificada
     */
    public List<ListaEspera> obtenerPorGravedad(Gravedad gravedad) {
        return listaEsperaRepository.findByGravedadOrderByIdAsc(gravedad);
    }

    /**
     * Obtiene un registro de la lista de espera por su ID.
     *
     * @param id el ID del registro
     * @return Optional con el registro si existe, vacío si no existe
     */
    public Optional<ListaEspera> obtenerPorId(Long id) {
        return listaEsperaRepository.findById(id);
    }

    /**
     * Actualiza el estado de un registro en la lista de espera.
     * <p>
     * Valida que el registro exista antes de actualizar.
     * Envía una notificación automática cuando se actualiza el estado.
     * </p>
     *
     * @param id el ID del registro a actualizar
     * @param estado el nuevo estado
     * @return el registro actualizado
     * @throws ResponseStatusException si el registro no existe
     */
    public ListaEspera actualizarEstado(Long id, Estado estado) {
        Optional<ListaEspera> optional = listaEsperaRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de lista de espera no encontrado");
        }

        ListaEspera listaEspera = optional.get();
        listaEspera.setEstado(estado);
        listaEsperaRepository.save(listaEspera);
        enviarNotificacion(listaEspera, "ACTUALIZACION_ESTADO");
        return listaEspera;
    }

    /**
     * Elimina un registro de la lista de espera.
     * <p>
     * Valida que el registro exista antes de eliminar.
     * Envía una notificación automática cuando se elimina el registro.
     * </p>
     *
     * @param id el ID del registro a eliminar
     * @throws ResponseStatusException si el registro no existe
     */
    public void eliminarDeListaEspera(Long id) {
        Optional<ListaEspera> optional = listaEsperaRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de lista de espera no encontrado");
        }

        ListaEspera listaEspera = optional.get();
        listaEsperaRepository.deleteById(id);
        enviarNotificacion(listaEspera, "ELIMINACION_LISTA_ESPERA");
    }

    /**
     * Envía una notificación para un registro de lista de espera.
     * <p>
     * Este método es privado y se utiliza internamente para enviar notificaciones
     * cuando ocurren cambios en la lista de espera.
     * </p>
     *
     * @param listaEspera el registro de lista de espera
     * @param tipo el tipo de notificación
     */
    private void enviarNotificacion(ListaEspera listaEspera, String tipo) {
        NotificationRequestDTO requestDTO = new NotificationRequestDTO();
        requestDTO.setPacienteId(listaEspera.getPaciente().getId());
        requestDTO.setTipo(tipo);
        requestDTO.setMensaje("Actualización en la lista de espera: " + listaEspera.getId());
        try {
            notificationClient.createNotification(requestDTO);
        } catch (Exception ex) {
            logger.warn("No se pudo registrar la notificación para listaEsperaId={} tipo={}", listaEspera.getId(), tipo, ex);
        }
    }
}
