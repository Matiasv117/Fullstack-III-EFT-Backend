-- Limpieza del legado: si existen ambas tablas, conserva la versión en español.
-- Si solo existe la antigua, la renombra. Si ya quedó limpia, no hace nada.
DO $body$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'notifications'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = 'public' AND table_name = 'notificaciones'
        ) THEN
            DROP TABLE notifications;
        ELSE
            ALTER TABLE notifications RENAME TO notificaciones;
        END IF;
    END IF;
END
$body$;

