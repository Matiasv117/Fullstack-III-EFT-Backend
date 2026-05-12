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
| **React.js** | Biblioteca principal para la interfaz. |
| **Vite** | Herramienta de construcción rápida. |
| **Bootstrap** | Para el diseño responsivo. |

### **Backend & Database**
| Tecnología | Descripción |
| :--- | :--- |
| **Java 17** | Lenguaje y runtime del backend. |
| **Spring Boot 3** | Microservicios, BFF y API Gateway. |
| **Spring Cloud Gateway** | Punto de entrada HTTP y enrutado. |
| **Eureka** | Descubrimiento de servicios. |
| **PostgreSQL / H2** | PostgreSQL (perfil `postgres`, p. ej. Insforge) o H2 en memoria por defecto. |

### **Componentes backend (carpetas)**
| Carpeta | Rol |
| :--- | :--- |
| `bff` | **Backend for Frontend**: agrega respuestas para el portal (ej. `/api/portal/resumen`). |
| `api-gateway` | Enruta peticiones a microservicios y al BFF. |
| `ms-gestionpacientes` | Pacientes y lista de espera. |
| `ms-notificaciones` | Notificaciones. |
| `ms-optimizacion` | Citas, médicos, horarios y optimización. |
| `eureka-server` | Registro de servicios. |

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

### Insforge / PostgreSQL


1. Copiá `config/local-insforge.env.example` a `config/local-insforge.env` y pegá la **contraseña** (ese archivo no se sube a git).
2. En PowerShell, desde la raíz del repo: **punto espacio** script (carga variables en esa ventana):

   ```powershell
   . .\scripts\load-insforge-env.ps1
   ```

3. En la **misma** ventana, levanta cada microservicio.

Más detalle y la URI JDBC de ejemplo: `config/ejemplo-insforge.env`.

**API de notificaciones:** las rutas REST pasan a **`/api/notificaciones`** (español); la tabla JPA es **`notificaciones`**. Con perfil `postgres`, Flyway en `ms-notificaciones` renombra `notifications` → `notificaciones` si aún existe la tabla antigua en la base. Si en Insforge ves `flyway_ms_*`, esas son **tablas de historial de migraciones** de cada microservicio, no tablas de negocio. Si ves `notifications` y `notificaciones` a la vez, la migración `V2__cleanup_legacy_notifications_table.sql` deja solo la versión actual en español.

**Qué tabla mirar:** al pulsar **Registrar** se inserta en **`paciente`**. Al pulsar **Agregar a lista** se inserta en **`lista_espera`** (referencia al paciente por id). Si Insforge sigue en 0 filas, casi siempre es porque los servicios siguen en **H2** (revisá el log al arrancar: debe decir `jdbc:postgresql://...insforge...`). Usá `config/local-insforge.env` + `start-all.ps1` o cargá variables antes de `mvnw`.

## Documentación de apoyo para la entrega

- `PLAN_BRANCHING.md`: estrategia de ramas y guía para explicar merges/conflictos.
- `PATRONES_Y_ARQUITECTURA.md`: resumen de patrones aplicados en frontend y backend.
- `arquetipo-maven-salud-ms/`: arquetipo base mínimo para nuevos microservicios Spring Boot.
- `repositorios.txt`: índice de repositorios y componentes del monorepo.

