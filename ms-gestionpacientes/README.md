# ms-gestionpacientes (Spring: `ms-listas-espera`)

Microservicio de **pacientes** y **lista de espera** (puerto **8083**).

## Flyway (perfil `postgres`)

Con PostgreSQL/Insforge, Flyway usa la tabla de historial `flyway_ms_listas_espera` (misma base que otros MS: historiales separados por servicio). Migración `V1__baseline.sql` marca el arranque de migraciones sin tocar tus tablas de negocio.

## Ejecutar

```bash
./mvnw spring-boot:run
```

Variables típicas con Insforge: ver `config/local-insforge.env.example` en la raíz del repo backend.
