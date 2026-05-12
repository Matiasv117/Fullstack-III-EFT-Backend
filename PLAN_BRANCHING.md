# Plan de branching

## Objetivo
Mantener un flujo de trabajo simple, trazable y fácil de defender durante la exposición oral.

## Estrategia propuesta

### Ramas principales
- `main`: versión estable lista para entrega.
- `develop`: integración de cambios validados antes de llegar a `main`.
- `feature/<tema>`: trabajo de una funcionalidad concreta.
- `fix/<tema>`: correcciones puntuales sobre una funcionalidad existente.
- `hotfix/<tema>`: correcciones urgentes sobre `main` cuando algo bloquea la demo.

### Flujo recomendado
1. Crear una rama desde `develop`.
2. Implementar el cambio en una sola funcionalidad.
3. Ejecutar pruebas locales.
4. Abrir PR o merge hacia `develop`.
5. Resolver conflictos en la rama de integración, no en `main`.
6. Cuando `develop` esté estable, fusionar a `main` para la entrega.

## Criterios de trabajo
- Un cambio lógico por rama.
- Commits pequeños y descriptivos.
- No mezclar refactors grandes con cambios funcionales.
- Revisar dependencias cruzadas entre frontend y backend antes de mergear.

## Gestión de conflictos
Cuando dos ramas modifican el mismo archivo:
- revisar la intención de ambos cambios;
- conservar la versión que respete la API real del proyecto;
- volver a probar después del merge;
- documentar el conflicto resuelto con una nota breve en el PR o commit.

## Evidencia útil para la defensa
Para mostrar la estrategia al docente conviene enseñar:
- historial de commits;
- ramas creadas por funcionalidad;
- merges de integración;
- capturas o notas de conflictos resueltos.

## Recomendación práctica para este proyecto
Durante la entrega final, usar:
- `feature/frontend-facade`
- `feature/container-presenter-pacientes`
- `feature/flyway-notificaciones`
- `feature/docs-entrega`

Eso permite explicar claramente qué hizo cada integrante y cómo se fue integrando el trabajo.

