-- Stored procedures / functions para métricas y actualización de lista de espera

CREATE OR REPLACE FUNCTION sp_actualizar_estado_lista_espera(
    p_lista_espera_id BIGINT,
    p_estado VARCHAR
) RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE lista_espera
    SET estado = p_estado
    WHERE id = p_lista_espera_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Lista de espera no encontrada: %', p_lista_espera_id;
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION sp_calcular_gravedad(
    p_dias_espera INTEGER,
    p_nivel_clinico INTEGER DEFAULT 1
) RETURNS VARCHAR
LANGUAGE plpgsql
AS $$
BEGIN
    IF p_dias_espera >= 30 OR p_nivel_clinico >= 4 THEN
        RETURN 'ALTA';
    ELSIF p_dias_espera >= 14 OR p_nivel_clinico >= 2 THEN
        RETURN 'MEDIA';
    ELSE
        RETURN 'BAJA';
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION sp_metricas_lista_espera()
RETURNS TABLE(
    total_pendientes BIGINT,
    pacientes_gravedad_alta BIGINT,
    pacientes_gravedad_media BIGINT,
    pacientes_gravedad_baja BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        COUNT(*) FILTER (WHERE estado = 'PENDIENTE'),
        COUNT(*) FILTER (WHERE gravedad = 'ALTA'),
        COUNT(*) FILTER (WHERE gravedad = 'MEDIA'),
        COUNT(*) FILTER (WHERE gravedad = 'BAJA')
    FROM lista_espera;
END;
$$;
