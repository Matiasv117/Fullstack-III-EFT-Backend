# ms-progreso

Microservicio para gestionar el estado de progreso del paciente.

## Endpoints

- `POST /progreso/pacientes/{pacienteId}`: registra el progreso inicial.
- `PUT /progreso/pacientes/{pacienteId}`: actualiza el estado.
- `GET /progreso/pacientes/{pacienteId}`: obtiene el estado actual.

## Estados soportados

- `SINTOMAS_REGISTRADOS`
- `EVALUANDO_PRIORIDAD`
- `EN_LISTA_ACTIVA`
- `CITA_ASIGNADA`

## Ejecutar local

```powershell
Set-Location "C:\Users\W608-PCXX\IdeaProjects\Fullstack-III-EFT-Backend\ms-progreso"
mvn spring-boot:run
```

## Swagger

- `http://localhost:8086/swagger-ui.html`

