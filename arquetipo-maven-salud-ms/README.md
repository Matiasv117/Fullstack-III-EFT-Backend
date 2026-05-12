# Arquetipo Maven de microservicio de salud

Este directorio contiene un arquetipo mínimo para generar nuevos microservicios Spring Boot con la misma base estructural del proyecto.

## Qué incluye
- `pom.xml` del arquetipo
- plantilla de `application.yml`
- clase principal Spring Boot
- `archetype-metadata.xml`

## Uso orientativo
Desde la carpeta del arquetipo, el equipo puede instalarlo localmente y luego generar proyectos nuevos con Maven.

Ejemplo conceptual:

```powershell
mvn clean install
mvn archetype:generate -DarchetypeCatalog=local
```

## Objetivo académico
- mostrar cómo se estandariza la estructura de un microservicio;
- reducir errores repetitivos al crear nuevos servicios;
- mantener coherencia entre proyectos backend.

> Nota: es un arquetipo base/documental pensado para la entrega académica, no para un generador corporativo complejo.

