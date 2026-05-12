package com.saludrednorte.ms_listas_espera.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Escribe en log qué base está usando el servicio (H2 vs PostgreSQL/Insforge).
 */
@Component
public class DataSourceUrlLogger {

    private static final Logger log = LoggerFactory.getLogger(DataSourceUrlLogger.class);

    private final DataSource dataSource;

    public DataSourceUrlLogger(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logJdbcUrl() {
        try (Connection c = dataSource.getConnection()) {
            String url = c.getMetaData().getURL();
            log.warn("ms-gestionpacientes — JDBC activa: {}", url);
            if (url != null && url.contains(":h2:")) {
                log.warn("Estás en H2. Los datos no se ven en Insforge. Usa SPRING_PROFILES_ACTIVE=postgres y DB_URL (ver config/local-insforge.env.example).");
            }
        } catch (Exception e) {
            log.debug("No se pudo leer la URL JDBC", e);
        }
    }
}
