# 🚀 Guía Rápida: Ejecutar Tests y JaCoCo

## 1️⃣ Ejecutar Tests (Opción Más Común)

```bash
cd C:\Users\Y409-PCXX\Desktop\avances\Fullstack-III-EFT-Backend\ms-optimizacion
.\mvnw clean test
```

**Esto hará:**
- ✅ Compilar el código
- ✅ Ejecutar todos los ~55 tests
- ✅ Generar reporte de cobertura JaCoCo
- ✅ Mostrar resultado (BUILD SUCCESS o BUILD FAILURE)

---

## 2️⃣ Ver el Reporte de Cobertura

Después de ejecutar tests, abre en tu navegador:

```
C:\Users\Y409-PCXX\Desktop\avances\Fullstack-III-EFT-Backend\ms-optimizacion\target\site\jacoco\index.html
```

**En el reporte verás:**
- Cobertura total del proyecto
- Desglose por paquete
- Desglose por clase
- Líneas cubiertas vs no cubiertas

---

## 3️⃣ Verificar Cobertura Mínima

Si quieres que el build falle si no alcanza 80%:

```bash
.\mvnw verify
```

**Resultado:**
- Si coverage ≥ 80%: ✅ BUILD SUCCESS
- Si coverage < 80%: ❌ BUILD FAILURE

---

## 4️⃣ Ejecutar Tests Específicos

```bash
# Solo tests de CitaService
.\mvnw test -Dtest=CitaServiceTest

# Solo tests de controladores
.\mvnw test -Dtest=*ControllerTest

# Omitir tests
.\mvnw clean compile -DskipTests
```

---

## 5️⃣ Limpiar y Empezar de Nuevo

```bash
# Limpiar todos los archivos generados
.\mvnw clean

# Compilar nuevamente
.\mvnw clean compile

# Compilar + tests
.\mvnw clean test
```

---

## 📊 Interpretación de Resultados

### En Consola:

```
[INFO] --- maven-surefire-plugin:3.x.x:test (default-test) @ ms-optimizacion ---
[INFO] Tests run: 55, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

✅ = Todos los tests pasaron
❌ = Algún test falló (revisar el error)

### Errores Comunes:

**Error 1:** `[ERROR] Tests in error: CitaServiceTest`
- Significa: Problema en la estructura del test o dependencia faltante
- **Solución:** Revisar los imports y las anotaciones @Mock

**Error 2:** `Cobertura < 80%`
- Significa: Hay código que no está siendo testeado
- **Solución:** Agregar más tests o revisar qué métodos no están siendo llamados

**Error 3:** `MedicoService not found during @Autowired`
- Significa: Falta configuración o Mock
- **Solución:** Verificar que el Mock está correctamente inyectado con @InjectMocks

---

## 🎯 Tests por Componente

| Clase | Tests | Archivo |
|-------|-------|---------|
| CitaService | 9 | src/test/java/.../service/CitaServiceTest.java |
| OptimizacionService | 6 | src/test/java/.../service/OptimizacionServiceTest.java |
| MedicoService | 5 | src/test/java/.../service/MedicoServiceTest.java |
| HorarioService | 6 | src/test/java/.../service/HorarioServiceTest.java |
| OptimizacionFactory | 5 | src/test/java/.../service/OptimizacionFactoryTest.java |
| EstrategiaFIFO | 3 | src/test/java/.../service/EstrategiaFIFOTest.java |
| CitaController | 6 | src/test/java/.../controller/CitaControllerTest.java |
| MedicoController | 6 | src/test/java/.../controller/MedicoControllerTest.java |
| HorarioController | 7 | src/test/java/.../controller/HorarioControllerTest.java |
| OptimizacionController | 5 | src/test/java/.../controller/OptimizacionControllerTest.java |

**Total: 58 test methods**

---

## 🔧 Configuración en pom.xml

La configuración de JaCoCo está en:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <!-- ... configuración ... -->
</plugin>
```

**Ubicación en archivo:** Líneas 97-143 de pom.xml

**Reglas actuales:**
- Cobertura LINE: ≥ 80%
- Cobertura BRANCH: ≥ 80%

---

## 📝 Archivos Generados Después de Tests

```
target/
├── test-classes/              (Tests compilados)
├── classes/                    (Código compilado)
├── jacoco.exec                 (Datos de cobertura)
└── site/
    └── jacoco/
        ├── index.html          ← ABRE ESTO EN NAVEGADOR
        ├── *.html              (Detalles por clase)
        └── ...
```

---

## 🎓 Qué Miden los Tests

### CitaServiceTest
- Validación de campos obligatorios
- CRUD completo (Create, Read, Update, Delete)
- Manejo de excepciones

### OptimizacionServiceTest
- Procesamiento de cancelaciones
- Reasignación de citas
- Notificaciones
- Circuit Breaker fallback

### MedicoServiceTest
- Registro de médico
- Búsqueda por ID
- Actualización y eliminación

### HorarioServiceTest
- Gestión de disponibilidad de horarios
- Filtrado por médico y fecha
- Validación de existencia

### ControllerTests
- Endpoints HTTP (GET, POST, PUT, DELETE)
- Códigos de respuesta (200, 404, etc.)
- Serialización JSON

---

## ⏱️ Tiempo Estimado

- **Primera ejecución:** 2-3 minutos (descarga dependencias)
- **Ejecuciones posteriores:** 30-60 segundos

---

## 💡 Tips Útiles

### Para desarrollo rápido durante los tests:
```bash
# Solo compilar sin tests (más rápido)
.\mvnw clean compile

# Ejecutar un test específico mientras desarrollas
.\mvnw test -Dtest=CitaServiceTest -DfailIfNoTests=false
```

### Para CI/CD:
```bash
# Esto es lo que deberías executestar en tu pipeline
./mvnw clean verify
```

---

¡Ahora estás listo para ejecutar tests con JaCoCo! 🎉

**Próximo paso:** Ejecuta `.\mvnw clean test` y abre el reporte en `target/site/jacoco/index.html`

