# Fullstack III 🚀

Este repositorio contiene el desarrollo del Examen para la asignatura de **Fullstack III**. El proyecto consiste en una aplicación web robusta que integra tecnologías modernas de frontend y backend para resolver una problemática de negocio específica.

---

## 📋 Tabla de Contenidos

| Icono | Sección |
| :---: | :--- |
| 📝 | [Descripción del Proyecto](#-descripción-del-proyecto) |
| 🛠️ | [Tecnologías Utilizadas](#️-tecnologías-utilizadas) |
| ⚙️ | [Requisitos Previos](#-requisitos-previos) |
| 🚀 | [Instalación y Configuración](#-instalación-y-configuración) |
| 👤 | [Autores](#-autores) |

---

## 📝 Descripción del Proyecto
La aplicación permite la gestión integral de datos, ofreciendo una interfaz de usuario intuitiva y una API eficiente. Se centra en el cumplimiento de los requerimientos técnicos de escalabilidad, seguridad y buenas prácticas de codificación.

---

## 🛠️ Tecnologías Utilizadas

### **Frontend**
| Tecnología | Descripción |
| :--- | :--- |
| **React 19** | Biblioteca principal para la interfaz. |
| **Vite 8** | Herramienta de construcción rápida. |
| **Tailwind CSS v4** | Framework de estilos utility-first. |
| **Lucide React** | Iconos vectoriales. |
| **Vitest 4 + Testing Library** | Tests unitarios y de integración. |

### **Backend & Database**
| Tecnología | Descripción |
| :--- | :--- |
| **Java 17** | Lenguaje y runtime del backend. |
| **Spring Boot 3.4.1** | Microservicios, BFF y API Gateway. |
| **Spring Cloud Gateway** | Punto de entrada HTTP y enrutado (puerto 8080). |
| **Eureka** | Descubrimiento de servicios (puerto 8761). |
| **Spring Cloud OpenFeign** | Comunicación síncrona entre microservicios. |
| **RabbitMQ** | Mensajería asíncrona (auditoría y notificaciones). |
| **Redis** | Caché distribuida. |
| **PostgreSQL / H2 / Neon** | PostgreSQL local o Neon cloud (perfil `postgres`), H2 en memoria por defecto. |
| **JUnit 5 + Mockito + JaCoCo** | Tests y cobertura (~90%+ en servicios principales). |

### **Componentes backend (carpetas)**
| Carpeta | Rol | Puerto |
| :--- | :--- | :--- |
| `eureka-server` | Registro y descubrimiento de servicios. | 8761 |
| `api-gateway` | Enruta peticiones a microservicios y al BFF. | 8080 |
| `bff` | **Backend for Frontend**: agrega respuestas para el portal (ej. `/api/portal/resumen`). | 8097 |
| `ms-auth` | Autenticación JWT (HMAC-SHA), registro y gestión de funcionarios. | 8087 |
| `ms-gestionpacientes` | Pacientes y lista de espera. | 8083 |
| `ms-notificaciones` | Notificaciones push/email. | 8085 |
| `ms-optimizacion` | Citas, médicos, horarios y optimización (Strategy Pattern). | 8084 |
| `ms-progreso` | Progreso de pacientes. | 8086 |
| `ms-auditoria` | Auditoría de eventos (RabbitMQ). | 8088 |

---

## ⚙️ Requisitos Previos
Antes de comenzar, asegúrate de tener instalado:
* 🔹 **Node.js** (versión 18 o superior recomendada)
* 🔹 **Java 17** y **Maven** (o usar los `mvnw` incluidos)
* 🔹 **Git**
* 🔹 **npm** (para el frontend en el repo `Fullstack-III-EFT-Frontend`)

---

## 🚀 Instalación y Configuración

**Clonar el repositorio backend:**
```bash
git clone https://github.com/Matiasv117/Fullstack-III-EFT.git
```

**Autores**

| Nombre | GitHub |
| :--- | :--- |
| Matías Vargas | [@Matiasv117](https://github.com/Matiasv117) |
| Benjamín Ibañez | [@beibanezv](https://github.com/beibanezv) |
| Fabián Reyes | [@FabianReyes02](https://github.com/FabianReyes02) |

## Tests

### Backend (434 tests, 0 fallas)
```bash
# Cada microservicio
cd ms-auth; .\mvnw test
cd bff; .\mvnw test
# Todos los servicios con cobertura
.\mvnw verify
```
Cobertura JaCoCo: ms-auditoria 100%, ms-progreso 96%, ms-optimizacion 94%, bff 91%, ms-notificaciones 91%, ms-auth 87%, ms-gestionpacientes ≥85%.

### Frontend (159 tests)
```bash
npm test        # Vitest
npm run lint    # ESLint
```

## Smoke Test E2E (microservicios)

Con los servicios levantados, puedes validar integración base con:

```powershell
Set-Location "ruta\a\Fullstack-III-EFT"
.\scripts\smoke-test-e2e.ps1
```

## Arranque y apagado automático

Incluye Eureka, microservicios, API Gateway y **BFF** (`salud-bff` en el puerto **8097**).

### Levantar todo

```powershell
Set-Location "ruta\a\Fullstack-III-EFT"
.\scripts\start-all.ps1 -RestartExisting -RunSmokeTest
```

### Detener todo

```powershell
Set-Location "ruta\a\Fullstack-III-EFT"
.\scripts\stop-all.ps1
```

### Neon (PostgreSQL cloud)

Todos los microservicios usan una misma instancia Neon con esquemas separados por servicio (Flyway).

1. Copiá `config/local-insforge.env.example` a `config/local-insforge.env` y pegá la **contraseña** de Neon (ese archivo no se sube a git).
2. En PowerShell, desde la raíz del repo: **punto espacio** script (carga variables en esa ventana):

   ```powershell
   . .\scripts\load-insforge-env.ps1
   ```

3. En la **misma** ventana, levanta cada microservicio.

Más detalle y la URI JDBC de ejemplo: `config/ejemplo-insforge.env`.

**API de notificaciones:** las rutas REST pasan a **`/api/notificaciones`** (español); la tabla JPA es **`notificaciones`**. Con perfil `postgres`, Flyway en `ms-notificaciones` renombra `notifications` → `notificaciones` si aún existe la tabla antigua en la base. Si en Neon ves `flyway_ms_*`, esas son **tablas de historial de migraciones** de cada microservicio, no tablas de negocio. Si ves `notifications` y `notificaciones` a la vez, la migración `V2__cleanup_legacy_notifications_table.sql` deja solo la versión actual en español.

**Qué tabla mirar:** al pulsar **Registrar** se inserta en **`paciente`**. Al pulsar **Agregar a lista** se inserta en **`lista_espera`** (referencia al paciente por id). Si la BD sigue en 0 filas, casi siempre es porque los servicios siguen en **H2** (revisá el log al arrancar: debe decir `jdbc:postgresql://...`). Usá `config/local-insforge.env` + `start-all.ps1` o cargá variables antes de `mvnw`.

## Documentación de apoyo para la entrega

- `PLAN_BRANCHING.md`: estrategia de ramas y guía para explicar merges/conflictos.
- `PATRONES_Y_ARQUITECTURA.md`: resumen de patrones aplicados en frontend y backend.
- `arquetipo-maven-salud-ms/`: arquetipo base mínimo para nuevos microservicios Spring Boot.
- `repositorios.txt`: índice de repositorios y componentes del monorepo.

## Autenticación

El sistema usa autenticación JWT con clave HMAC-SHA compartida.

### Flujo actual

1. `POST /api/auth/login` (BFF 8097 o Gateway 8080) → valida credenciales contra `ms-auth` → devuelve `{ token, type: "Bearer" }`.
2. El frontend almacena el token en `localStorage` y lo envía como `Authorization: Bearer <token>`.
3. El `api-gateway` valida el token con `JwtTokenValidator` (filtro global).
4. El BFF reenvía el token a los microservicios para mantener la sesión.

### Usuarios por defecto (creados automáticamente al iniciar con BD vacía)

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN` |
| `funcionario` | `funcionario123` | `ROLE_FUNCIONARIO` |
| `paciente` | `paciente123` | `ROLE_PACIENTE` |

### Reglas de acceso

- Sin token: `401 Unauthorized`.
- `ROLE_USER` o `ROLE_FUNCIONARIO` en endpoint de admin: `403 Forbidden`.
- `ROLE_ADMIN`: acceso completo a gestión de funcionarios y administración.

