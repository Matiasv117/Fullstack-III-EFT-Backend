-- Renombra la tabla legada (ingles) al nombre en español usado por JPA.
-- Idempotente: solo corre si existe 'notifications' y aun no existe 'notificaciones'.
DO $body$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'notifications'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'notificaciones'
    ) THEN
        ALTER TABLE notifications RENAME TO notificaciones;
    END IF;
END
$body$;
