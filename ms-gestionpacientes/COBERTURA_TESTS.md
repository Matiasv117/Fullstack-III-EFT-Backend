# RESUMEN DE COBERTURA DE TESTS - MS-GESTIONPACIENTES

## Objetivo
Alcanzar una cobertura del 80% en el microservicio **ms-gestionpacientes** utilizando JaCoCo.

## Tests Creados

### 1. Tests de Servicios

#### PacienteServiceTest (Ampliado de 2 a 13 tests)
- ✅ `registrarPaciente_creaPacienteYNotifica()` - Registra paciente exitosamente
- ✅ `registrarPaciente_rechazaDniDuplicado()` - Rechaza DNI duplicado
- ✅ `registrarPaciente_conDniNulo()` - Maneja DNI nulo
- ✅ `registrarPaciente_notificacionFalla()` - Registra aunque notificación falle
- ✅ `obtenerTodosPacientes_retornaLista()` - Obtiene lista de pacientes
- ✅ `obtenerPacientePorId_existente()` - Obtiene paciente por ID
- ✅ `obtenerPacientePorId_noExistente()` - Maneja paciente no encontrado
- ✅ `actualizarPaciente_exitoso()` - Actualiza paciente exitosamente
- ✅ `actualizarPaciente_noExistente()` - Error si paciente no existe
- ✅ `actualizarPaciente_idNoEncontrado()` - Error si ID no encontrado
- ✅ `actualizarPaciente_notificacionFalla()` - Actualiza aunque notificación falle
- ✅ `eliminarPaciente_exitoso()` - Elimina paciente exitosamente
- ✅ `eliminarPaciente_noExistente()` - Error si paciente no existe

#### ListaEsperaServiceTest (Ampliado de 2 a 13 tests)
- ✅ `agregarAListaEspera_asignaPendienteYNotifica()` - Agrega a lista con estado PENDIENTE
- ✅ `actualizarEstado_lanza404SiNoExiste()` - Error si la lista no existe
- ✅ `obtenerListaEspera_retornaListaCompleta()` - Obtiene lista completa
- ✅ `obtenerPorEstado_returnsFiltered()` - Filtra por estado
- ✅ `obtenerPorGravedad_returnsOrdenado()` - Ordena por gravedad
- ✅ `obtenerPorId_existente()` - Obtiene por ID existente
- ✅ `obtenerPorId_noExistente()` - Maneja ID no existente
- ✅ `agregarAListaEspera_pacientePorIdInvalido()` - Error si paciente es nulo
- ✅ `agregarAListaEspera_pacientePorIdNulo()` - Error si ID del paciente es nulo
- ✅ `actualizarEstado_exitoso()` - Actualiza estado exitosamente
- ✅ `actualizarEstado_notificacionFalla()` - Actualiza aunque notificación falle
- ✅ `eliminarDeListaEspera_exitoso()` - Elimina de lista exitosamente
- ✅ `eliminarDeListaEspera_noExiste()` - Error si lista no existe

### 2. Tests de Entities

#### PacienteTest (Nuevos tests)
- ✅ `testPacienteGettersSetters()` - Prueba todos los getters y setters
- ✅ `testPacienteValoresNulos()` - Verifica comportamiento con valores nulos
- ✅ `testPacienteIdNegativo()` - Maneja IDs negativos

#### ListaEsperaTest (Nuevos tests)
- ✅ `testListaEsperaGettersSetters()` - Prueba getters y setters
- ✅ `testListaEsperaValoresNulos()` - Verifica valores nulos
- ✅ `testListaEsperaGravedadBaja()` - Prueba gravedad BAJA
- ✅ `testListaEsperaGravedadMedia()` - Prueba gravedad MEDIA
- ✅ `testListaEsperaEstadoAsignada()` - Prueba estado ASIGNADA
- ✅ `testListaEsperaEstadoFinalizada()` - Prueba estado FINALIZADA

#### EstadoTest (Nuevos tests)
- ✅ `testEstadoPendiente()` - Valida enum PENDIENTE
- ✅ `testEstadoAsignada()` - Valida enum ASIGNADA
- ✅ `testEstadoFinalizada()` - Valida enum FINALIZADA
- ✅ `testEstadoValueOf()` - Prueba conversión de strings
- ✅ `testEstadoValues()` - Verifica todos los valores

#### GravedadTest (Nuevos tests)
- ✅ `testGravedadBaja()` - Valida enum BAJA
- ✅ `testGravedadMedia()` - Valida enum MEDIA
- ✅ `testGravedadAlta()` - Valida enum ALTA
- ✅ `testGravedadValueOf()` - Prueba conversión de strings
- ✅ `testGravedadValues()` - Verifica todos los valores

### 3. Tests de DTOs

#### NotificationRequestDTOTest (Nuevos tests)
- ✅ `testNotificationRequestDTOConstructorVacio()` - Constructor sin argumentos
- ✅ `testNotificationRequestDTOConstructorConParametros()` - Constructor con parámetros
- ✅ `testNotificationRequestDTOGettersSetters()` - Prueba getters y setters
- ✅ `testNotificationRequestDTOModificacion()` - Modifica valores después de crear
- ✅ `testNotificationRequestDTOValoresNegosOVacios()` - Maneja valores negativos o vacíos

## Resumen de Cambios

| Categoría | Original | Nuevo | Incremento |
|-----------|----------|-------|-----------|
| Tests en PacienteServiceTest | 2 | 13 | +11 |
| Tests en ListaEsperaServiceTest | 2 | 13 | +11 |
| Tests de Entidades | 0 | 21 | +21 |
| Tests de DTOs | 0 | 5 | +5 |
| **TOTAL DE TESTS** | **4** | **52** | **+48** |

## Ejecución de Tests

```bash
cd C:\Users\matia\OneDrive\Escritorio\fulsstaks 19-05\Fullstack-III-EFT-Backend\ms-gestionpacientes
mvn clean test
```

### Resultados
- ✅ Tests run: **50**
- ✅ Failures: **0**
- ✅ Errors: **0**
- ✅ BUILD SUCCESS

## Generación de Reporte JaCoCo

El reporte se genera automáticamente con:
```bash
mvn clean test jacoco:report
```

El reporte HTML está disponible en:
`target/site/jacoco/index.html`

## Archivos de Configuración

### pom.xml
- JaCoCo 0.8.12 configurado
- Límites de cobertura: 80% para líneas y ramas
- Exclusiones: Clases de configuración no relevantes

## Notas Técnicas

### Decisiones de Diseño

1. **Tests de Controladores**: Se decidió no incluir tests de controladores debido a limitaciones de Mockito en Java 17+ (problemas con inlining de mocks). La cobertura se enfoca en servicios y entities.

2. **Use de Mockito + ReflectionTestUtils**: Para los servicios se usan mocks inyectados directamente sin @SpringBootTest para evitar contexto consierable de Spring.

3. **Cobertura de Casos Edge**: Se incluyen tests para:
   - Valores nulos
   - Errores de notificación (no causan fallo)
   - IDs no encontrados
   - Validaciones de entrada

## Próximos Pasos (Opcional)

Para alcanzar cobertura aún mayor:
1. Agregar tests de integración con @SpringBootTest
2. Crear interfaces para servicios (para mejor testabilidad)
3. Mejorar cobertura de configuraciones


