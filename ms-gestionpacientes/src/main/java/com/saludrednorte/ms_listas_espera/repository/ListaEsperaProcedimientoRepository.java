package com.saludrednorte.ms_listas_espera.repository;

import com.saludrednorte.ms_listas_espera.dto.ListaEsperaMetricasDTO;
import com.saludrednorte.ms_listas_espera.entity.Gravedad;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * Repositorio que invoca stored procedures de PostgreSQL para lista de espera.
 */
@Repository
@Profile("postgres")
public class ListaEsperaProcedimientoRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Actualiza el estado de un registro en lista de espera mediante stored procedure.
     */
    public void actualizarEstado(Long listaEsperaId, String estado) {
        entityManager.createNativeQuery("SELECT sp_actualizar_estado_lista_espera(:id, :estado)")
                .setParameter("id", listaEsperaId)
                .setParameter("estado", estado)
                .getResultList();
    }

    /**
     * Calcula la gravedad automática según días de espera y nivel clínico.
     */
    public Gravedad calcularGravedad(int diasEspera, int nivelClinico) {
        String resultado = (String) entityManager
                .createNativeQuery("SELECT sp_calcular_gravedad(:dias, :nivel)")
                .setParameter("dias", diasEspera)
                .setParameter("nivel", nivelClinico)
                .getSingleResult();
        return Gravedad.valueOf(resultado);
    }

    /**
     * Obtiene métricas agregadas de la lista de espera.
     */
    public ListaEsperaMetricasDTO obtenerMetricas() {
        Object[] fila = (Object[]) entityManager
                .createNativeQuery("SELECT * FROM sp_metricas_lista_espera()")
                .getSingleResult();

        return new ListaEsperaMetricasDTO(
                ((Number) fila[0]).longValue(),
                ((Number) fila[1]).longValue(),
                ((Number) fila[2]).longValue(),
                ((Number) fila[3]).longValue()
        );
    }
}
