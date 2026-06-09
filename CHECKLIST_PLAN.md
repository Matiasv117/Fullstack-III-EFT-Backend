Checklist y Plan de trabajo para estabilizar el rediseño
=====================================================

Resumen rápido
--------------
Este repositorio está en proceso de reorganización arquitectónica. La meta:

- `ms-gestionpacientes` → core de datos (entidades, repositorios, servicios REST).
- `ms-optimizacion` → motor de negocio, solo lógica, consumiendo datos vía Feign/DTOs.
- `ms-progreso` → microservicio de estados visibles del paciente.
- BFF y api-gateway → orquestación ligera / proxy.

Checklist inmediato (prioridad)
-------------------------------
- [ ] Estabilizar `ms-gestionpacientes` (resolver imports, paquetes y duplicados).
- [ ] Refactorizar `ms-optimizacion` para que use DTOs/Feign y no dependa de entidades JPA.
- [ ] Revisar y corregir: `EstrategiaFIFO`, `EstrategiaPorGravedad`, `EstrategiaOptimizacion`, `OptimizacionService`, `OptimizacionController`, `CitaClient`, `CitaDTO`, `MedicoDTO`.
- [ ] Ejecutar tests en cada microservicio (`mvnw.cmd test`) y corregir fallos de compilación.
- [ ] Añadir/ajustar tests unitarios faltantes y mejorar cobertura.
- [ ] Después de que compile: integrar JWT + Spring Security y Swagger/OpenAPI.

Plan por fases y ramas sugeridas
-------------------------------
Fase 0 — Preparación

- Rama: `feature/plan-checklist` — agregar este checklist y plan general.

Fase 1 — Estabilizar core de datos

- Rama: `feature/ms-core-datos-stabilize`
- Tareas:
  - Revisar `package` en las clases movidas y unificar namespace (ej.: `com.insforge.ms.gestionpacientes`).
  - Eliminar clases duplicadas en otros módulos o marcar como deprecated.
  - Compilar módulo y arreglar imports rotos.
  - Confirmar endpoints públicos y contratos DTO.
  - Commit sugerido: "Corregir paquetes e imports en ms-gestionpacientes"

Fase 2 — Refactor ms-optimizacion

- Rama: `feature/ms-optimizacion-refactor`
- Tareas:
  - Reemplazar referencias a entidades JPA por DTOs y clientes Feign.
  - Actualizar `EstrategiaFIFO`, `EstrategiaPorGravedad` para recibir DTOs.
  - Limpiar `pom.xml` para eliminar dependencias innecesarias al core de datos.
  - Commit sugerido: "Refactor: usar DTOs/Feign en ms-optimizacion"

Fase 3 — Tests y CI local

- Rama: `feature/run-tests`
- Tareas:
  - Agregar script para ejecutar `mvnw.cmd test` en todos los módulos relevantes.
  - Ejecutar tests, corregir errores y aumentar cobertura.
  - Commit sugerido: "Agregar script para ejecutar pruebas en todos los módulos"

Fase 4 — Seguridad y documentación

- Rama: `feature/security-jwt` — JWT y Spring Security.
- Rama: `feature/swagger` — OpenAPI/Swagger en cada microservicio.

Convenciones de commits (en español, claros y pequeños)
----------------------------------------------------
- tipo: mensaje corto (Imperativo)
- ejemplos:
  - "Corregir paquetes e imports en ms-gestionpacientes"
  - "Refactor: usar DTOs/Feign en ms-optimizacion"
  - "Agregar tests unitarios para PrioridadCalculadora"
  - "Agregar script para ejecutar pruebas en todos los módulos"

Guía práctica para trabajar en cada rama
---------------------------------------
1. Crear la rama: `git checkout -b feature/nombre-descriptivo`
2. Hacer cambios pequeños y testear localmente (mvnw.cmd test).
3. Commit con mensaje claro en español.
4. Abrir PR pequeño y pedir revisión.

Plantillas de PR
---------------
- Título: "feature: breve descripción"
- Descripción: 1) Qué cambia 2) Cómo probar localmente 3) Tests incluidos

Notas finales
--------------
Si estás de acuerdo yo puedo: crear las ramas locales y agregar archivos iniciales con las tareas (README/TASKS), y un script de pruebas. Luego iré trabajando por ramas, aplicando cambios y commits pequeños según lo acordado.

Fecha: 2026-06-09

