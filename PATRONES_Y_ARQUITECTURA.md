# Patrones y arquitectura aplicados

## Frontend

### 1) Facade
Se aplica en `src/api/`:
- `gestionPacientesApi.js`
- `notificacionesApi.js`
- `optimizacionApi.js`

Estas capas ocultan las rutas HTTP concretas y centralizan el acceso a backend. Si cambia una URL, se modifica en un solo lugar.

### 2) Container / Presenter
Se aplica en gestión de pacientes:
- `src/hooks/useGestionPacientes.js` contiene la lógica de negocio y llamadas HTTP.
- `src/componentes/GestionPacientesView.jsx` solo renderiza la UI.
- `src/componentes/GestionPacientes.jsx` une ambas piezas.

Esto mejora la mantenibilidad porque la vista queda más simple y la lógica se puede reutilizar o testear mejor.

## Backend

### 3) Strategy
Se aplica en `ms-optimizacion/`:
- `EstrategiaOptimizacion` define el contrato.
- `EstrategiaFIFO` y `EstrategiaPorGravedad` implementan variantes concretas.
- `OptimizacionFactory` elige la estrategia según el parámetro recibido.

Este patrón permite cambiar la lógica de ordenamiento/priorización sin tocar el resto del flujo de negocio.

### 4) Facade / BFF
Se aplica en `bff/` con `/api/portal/resumen`.
El frontend consume una sola respuesta agregada para el dashboard en lugar de consultar varios microservicios directamente.

### 5) Observer / eventos por integración
No hay un bus de eventos explícito, pero sí una reacción distribuida entre servicios mediante Feign:
- `ms-gestionpacientes` notifica a `ms-notificaciones`.
- `ms-optimizacion` también puede notificar cambios de estado.

## Arquetipos y estructura
- Cada backend mantiene estructura Maven independiente.
- El proyecto usa Spring Boot + Spring Cloud para microservicios.
- `ms-notificaciones` incorpora Flyway para migraciones versionadas en PostgreSQL.

## Qué explicar en la defensa
- **Facade**: reduce acoplamiento entre UI y rutas HTTP.
- **Container/Presenter**: separa datos/lógica de presentación.
- **Strategy**: cambia el algoritmo de optimización sin reescribir el servicio.
- **BFF**: concentra datos de varios servicios en una sola respuesta.
- **Flyway**: controla evolución de esquema con scripts versionados.

