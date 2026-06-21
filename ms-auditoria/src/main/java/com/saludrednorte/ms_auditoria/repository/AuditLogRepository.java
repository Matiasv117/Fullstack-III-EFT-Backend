package com.saludrednorte.ms_auditoria.repository;

import com.saludrednorte.ms_auditoria.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad AuditLog.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);

    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    List<AuditLog> findAllByOrderByTimestampDesc();
}
