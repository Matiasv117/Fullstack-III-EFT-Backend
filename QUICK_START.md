# 🚀 QUICK REFERENCE - RedNorte Mejoras

## 📌 LO MÁS IMPORTANTE

**¿Qué cambió?**
- ✅ Botón "Eliminar" en Pacientes
- ✅ Nueva pestaña "Lista de Espera"
- ✅ Botones para cambiar estado y eliminar de lista
- ✅ Simulador de cancelación con estrategias
- ✅ Filtros por gravedad y estado

**¿Cómo se ejecuta?**
```bash
# Terminal 1: Eureka
cd eureka-server; mvn clean spring-boot:run

# Terminal 2: MS Pacientes
cd ms-gestionpacientes; mvn clean spring-boot:run

# Terminal 3: MS Optimización
cd ms-optimizacion; mvn clean spring-boot:run

# Terminal 4: BFF
cd bff; mvn clean spring-boot:run

# Terminal 5: Frontend
cd Fullstack-III-EFT-Frontend; npm install; npm run dev
```

**¿Dónde verificar que funciona?**
- Eureka: http://localhost:8761
- Frontend: http://localhost:5173

---

## 📁 ARCHIVOS CLAVE

| Archivo | Propósito |
|---------|-----------|
| `src/componentes/ListaEspera.jsx` | 🆕 Nuevo componente |
| `src/hooks/useListaEspera.js` | 🆕 Nuevo hook |
| `src/App.jsx` | Agregó nueva pestaña |
| `src/api/gestionPacientesApi.js` | +6 métodos de API |
| `src/componentes/Optimizacion.jsx` | Rediseño completo |
| `src/componentes/GestionPacientesView.jsx` | +Botón Eliminar |

---

## 🎯 CASOS DE USO

### T1: Crear y Eliminar Paciente
```
1. [Pacientes] → Completa formulario → Registrar
2. Click "Eliminar" en paciente
3. Confirma eliminación
✅ Paciente desaparece
```

### T2: Gestionar Lista de Espera
```
1. [Pacientes] → Click "Agregar a lista"
2. [Lista de Espera] → Ver paciente con estado PENDIENTE
3. Selector "Cambiar estado" → ATENDIDO
4. Click "Eliminar" si necesario
✅ Cambios aplicados inmediatamente
```

### T3: Simular Cancelación
```
1. [Optimización] → Ingresa ID cita
2. Selecciona estrategia (FIFO/LIFO/Gravedad)
3. Click "Procesar Cancelación"
✅ Sistema reasigna automáticamente
```

### T4: Filtrar Lista de Espera
```
1. [Lista de Espera] → Selecciona "Gravedad: ALTA"
2. Selecciona "Estado: PENDIENTE"
✅ Muestra solo esos pacientes
```

---

## 🔗 ENDPOINTS NUEVOS EN API FRONTEND

```javascript
// gestionPacientesApi.js
eliminarPaciente(id)                    // DELETE /pacientes/{id}
obtenerListaEspera()                    // GET /lista-espera
eliminarDelListaEspera(id)              // DELETE /lista-espera/{id}
actualizarEstadoListaEspera(id, estado) // PUT /lista-espera/{id}/estado/{estado}
obtenerPacientesPorEstado(estado)       // GET /lista-espera/estado/{estado}
obtenerPacientesPorGravedad(gravedad)   // GET /lista-espera/gravedad/{gravedad}
```

---

## 📊 NAVEGACIÓN NUEVO

```
[Pacientes] [Lista de Espera] [Notificaciones] [Optimización]
    ↓                ↓              ↓                 ↓
  CRUD          Estados        Mensajes        Reasignación
Registrar      Cambiar         Enviar          Cancelación
Eliminar       Eliminar        Recibir         Estrategias
Agregar        Filtrar         Notificar       Filtrar
```

---

## 🎨 COLORES

| Concepto | Antes | Después |
|----------|-------|---------|
| Gravedad ALTA | - | 🔴 #e74c3c |
| Gravedad MEDIA | - | 🟠 #f39c12 |
| Gravedad BAJA | - | 🟢 #27ae60 |
| Estado PENDIENTE | - | 🔴 #e74c3c |
| Estado ATENDIDO | - | 🟢 #27ae60 |
| Botón eliminar | - | 🔴 #e74c3c |

---

## ✅ COMPILACIÓN

```bash
# Verificar build
npm run build

# Resultado esperado:
# ✓ dist/index.html 0.47 kB
# ✓ dist/assets/index-CcgPB8Xa.css 3.55 kB
# ✓ dist/assets/index-0PrOUV75.js 258.78 kB
# ✓ built in 290ms
```

---

## 🐛 Problemas Comunes

| Problema | Solución |
|----------|----------|
| "No se conecta a 8081" | Verificar ms-gestionpacientes corriendo |
| "CORS Error" | Verificar BFF en puerto 8080 |
| "Datos no aparecen" | Revisar consola (F12), buscar errores rojos |
| "npm build falla" | `npm cache clean --force` y reintentar |
| "node_modules corrupto" | Eliminar carpeta, `npm install` de nuevo |

---

## 📝 DOCUMENTACIÓN

Leer en este orden:
1. 👉 **Este archivo** (orientación rápida)
2. **RESUMEN_CAMBIOS.md** (qué se cambió)
3. **GUIA_EJECUCION.md** (cómo ejecutar)
4. **MEJORAS_FRONTEND.md** (detalles técnicos)
5. **SUGERENCIAS_MEJORA.md** (futuro)

---

## 🚀 Próximos Pasos

- [ ] Iniciar todos los servicios
- [ ] Abrir http://localhost:5173
- [ ] Crear paciente de prueba
- [ ] Agregar a lista de espera
- [ ] Ver en "Lista de Espera"
- [ ] Cambiar estado
- [ ] Simular cancelación
- [ ] Verificar filtros

---

## 📞 Contacto / Soporte

Ver sección de troubleshooting en GUIA_EJECUCION.md

---

**Versión:** 1.0 | **Fecha:** 2026-05-11 | **Estado:** ✅ LISTO

