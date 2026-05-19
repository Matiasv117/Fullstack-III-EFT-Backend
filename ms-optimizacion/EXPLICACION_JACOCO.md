# Explicación: ¿Para qué sirve JaCoCo?

## ¿Qué es JaCoCo?

**JaCoCo** (Java Code Coverage) es una herramienta de análisis de **cobertura de código** para Java. Su propósito es medir qué porcentaje del código fuente está siendo ejecutado por los tests unitarios.

## ¿Para qué sirve?

JaCoCo te ayuda a:

1. **Medir Calidad de Tests**: Determina cuánto código está siendo probado
2. **Identificar código no testeado**: Muestra qué líneas de código no se ejecutan durante los tests
3. **Asegurar confiabilidad**: Ayuda a garantizar que el código crítico tenga una cobertura adecuada
4. **Mejorar la salud del proyecto**: Mantiene estándares mínimos de cobertura (ej: 80%)

## Configuración en el pom.xml

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <configuration>
        <excludes>
            <!-- Clases a excluir del análisis de cobertura -->
            <exclude>com/saludrednorte/ms_optimizacion/MsOptimizacionApplication.class</exclude>
            <exclude>com/saludrednorte/ms_optimizacion/controller/*.class</exclude>
        </excludes>
    </configuration>
    <executions>
        <!-- Configuración de ejecuciones -->
    </executions>
</plugin>
```

## Componentes de la Configuración

### 1. **prepare-agent** (Fase: default)
- Prepara el agente JaCoCo antes de ejecutar los tests
- Instrumenta el código para recolectar datos de cobertura

### 2. **report** (Fase: test)
- Genera reporte HTML de cobertura después de los tests
- El reporte se encuentra en `target/site/jacoco/index.html`

### 3. **check** (Fase: verify)
- Verifica que se cumplan los requisitos mínimos de cobertura
- Puede fallar el build si no se alcanza la cobertura requerida

## Reglas de Cobertura Configuradas

```xml
<rule>
    <element>BUNDLE</element>  <!-- Aplica a todo el proyecto -->
    <limits>
        <limit>
            <counter>LINE</counter>  <!-- Mide líneas de código -->
            <value>COVEREDRATIO</value>
            <minimum>0.80</minimum>  <!-- Mínimo 80% -->
        </limit>
        <limit>
            <counter>BRANCH</counter>  <!-- Mide ramas (if/else) -->
            <value>COVEREDRATIO</value>
            <minimum>0.80</minimum>  <!-- Mínimo 80% -->
        </limit>
    </limits>
</rule>
```

## Exclusiones en este Proyecto

Las siguientes clases están excluidas del análisis:

- **MsOptimizacionApplication.class**: Clase principal (iniciador de Spring)
- **controller/*.class**: Controladores (normalmente se testean de forma diferente)

## Métricas de Cobertura

### Tipos de Cobertura:

1. **Line Coverage (Cobertura de Líneas)**
   - ¿Cuántas líneas de código se ejecutaron?
   - Objetivo: 80%+

2. **Branch Coverage (Cobertura de Ramas)**
   - ¿Cuántas decisiones if/else se probaron?
   - Importante para lógica compleja
   - Objetivo: 80%+

3. **Method Coverage (Cobertura de Métodos)**
   - ¿Cuántos métodos se llamaron?

4. **Instruction Coverage (Cobertura de Instrucciones)**
   - ¿Cuántas instrucciones bytecode se ejecutaron?

## Cómo usar JaCoCo

### Ejecutar tests y generar reporte:
```bash
./mvnw clean test
```

### Generar solo reporte (sin ejecutar):
```bash
./mvnw jacoco:report
```

### Verificar cobertura mínima:
```bash
./mvnw verify
```

### Ver el reporte HTML:
Abre el archivo `target/site/jacoco/index.html` en tu navegador

## Tests Creados para ms-optimizacion

Se han creado **10 clases de test** con un total de **~55 métodos de test**:

1. **CitaServiceTest** (9 tests)
   - Prueba creación, lectura, actualización y eliminación de citas
   - Maneja casos de error y validación

2. **OptimizacionServiceTest** (6 tests)
   - Prueba procesamiento de cancelaciones
   - Obtención de lista de espera
   - Manejo de excepciones con fallback

3. **MedicoServiceTest** (5 tests)
   - Gestión completa de médicos
   - Validación de existencia

4. **HorarioServiceTest** (6 tests)
   - Prueba gestión de horarios
   - Filtrado de horarios disponibles

5. **OptimizacionFactoryTest** (5 tests)
   - Prueba factory pattern
   - Manejo case-insensitive

6. **EstrategiaFIFOTest** (3 tests)
   - Prueba estrategia FIFO

7. **CitaControllerTest** (6 tests)
   - Prueba endpoints REST de citas
   - Validación de respuestas HTTP

8. **MedicoControllerTest** (6 tests)
   - Prueba endpoints REST de médicos

9. **HorarioControllerTest** (7 tests)
   - Prueba endpoints REST de horarios

10. **OptimizacionControllerTest** (5 tests)
    - Prueba endpoints de optimización

## Beneficios en tu Proyecto

✅ **Calidad Mejorada**: Mínimo 80% de cobertura garantiza código confiable
✅ **Detección de Código Muerto**: Identifica código no utilizado
✅ **Regresiones Evitadas**: Cambios futuros no rompen funcionalidad testeada
✅ **Documentación Viva**: Los tests documentan cómo funciona el código

## Interpretación del Reporte

- **Verde (90-100%)**: Excelente cobertura
- **Amarillo (70-90%)**: Bueno, pero mejorable
- **Rojo (<70%)**: Necesita tests adicionales

## Rutas Configuradas para tu Proyecto

### Paquete base:
```
com.saludrednorte.ms_optimizacion
```

### Estructura de test:
```
src/test/java/com/saludrednorte/ms_optimizacion/
├── service/
│   ├── CitaServiceTest.java
│   ├── OptimizacionServiceTest.java
│   ├── MedicoServiceTest.java
│   ├── HorarioServiceTest.java
│   ├── OptimizacionFactoryTest.java
│   └── EstrategiaFIFOTest.java
└── controller/
    ├── CitaControllerTest.java
    ├── MedicoControllerTest.java
    ├── HorarioControllerTest.java
    └── OptimizacionControllerTest.java
```

## Próximos Pasos

1. Ejecutar: `./mvnw clean test`
2. Ver reporte: `target/site/jacoco/index.html`
3. Ajustar tests si la cobertura es < 80%
4. Integrar en CI/CD para verificar en cada build

---

**Total de Tests**: ~55 métodos de test
**Cobertura Esperada**: 80%+ en líneas y ramas

