package com.saludrednorte.ms_listas_espera.repository;

import com.saludrednorte.ms_listas_espera.entity.Estado;
import com.saludrednorte.ms_listas_espera.entity.Gravedad;
import com.saludrednorte.ms_listas_espera.entity.ListaEspera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListaEsperaRepository extends JpaRepository<ListaEspera, Long> {
    List<ListaEspera> findByEstado(Estado estado);
    List<ListaEspera> findByGravedadOrderByIdAsc(Gravedad gravedad);
    void deleteByPacienteId(Long pacienteId);
}
