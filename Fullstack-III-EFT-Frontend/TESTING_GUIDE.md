# Testing Guide - Frontend EFT

## 📋 Overview

Este proyecto incluye un suite completo de tests unitarios e integración para lograr **80% de cobertura de código** similar al backend con JaCoCo.

### Herramientas Utilizadas

- **Vitest**: Framework de testing moderno y rápido (compatible con Vite)
- **@testing-library/react**: Para testing de componentes React
- **@testing-library/jest-dom**: Matchers personalizados
- **@testing-library/user-event**: Simulación de eventos de usuario
- **jsdom**: DOM virtual para testing

## 🚀 Instalación

### Requisitos Previos
- Node.js v18 o superior
- npm v9 o superior

### Pasos de Instalación

```bash
# 1. Navegar al directorio del frontend
cd Fullstack-III-EFT-Frontend

# 2. Instalar dependencias de testing
npm install --save-dev vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event jsdom @vitest/coverage-v8

# 3. Verificar instalación
npm test -- --version
```

## 📊 Estructura de Tests

```
src/
├── api/
│   ├── httpClient.test.js          # Tests del cliente HTTP
│   ├── gestionPacientesApi.test.js # Tests de API de pacientes
│   ├── notificacionesApi.test.js   # Tests de API de notificaciones
│   ├── optimizacionApi.test.js     # Tests de API de optimización
│   └── portalApi.test.js           # Tests de API del portal
├── hooks/
│   ├── useGestionPacientes.test.js # Tests del hook de pacientes
│   └── useListaEspera.test.js      # Tests del hook de lista de espera
├── componentes/
│   ├── GestionPacientesView.test.jsx  # Tests de vista de pacientes
│   ├── GestionPacientes.test.jsx      # Tests de contenedor de pacientes
│   ├── ListaEspera.test.jsx           # Tests de lista de espera
│   ├── Notificaciones.test.jsx        # Tests de notificaciones
│   └── Optimizacion.test.jsx          # Tests de optimización
└── App.test.jsx                    # Tests de la aplicación principal
```

## ✅ Comandos de Testing

### Ejecutar todos los tests
```bash
npm test
```

### Modo observador (watch mode)
```bash
npm test -- --watch
```

### Generar reporte de cobertura
```bash
npm run test:coverage
```

### Ver interfaz gráfica de tests
```bash
npm run test:ui
```

### Tests específicos
```bash
npm test -- src/api/gestionPacientesApi.test.js
npm test -- src/componentes/GestionPacientesView.test.jsx
npm test -- --grep "should display"
```

## 📈 Cobertura de Código

### Requisitos Mínimos (80%)
- **Lines**: 80%
- **Functions**: 80%
- **Branches**: 80%
- **Statements**: 80%

### Generar Reporte HTML
```bash
npm run test:coverage
```

El reporte se generará en `coverage/` con:
- `index.html`: Reporte interactivo
- `lcov.info`: Formato estándar para CI/CD

## 📝 Estructura de Tests

### 1. Tests de APIs (httpClient, gestionPacientesApi, etc.)
Cada test de API valida:
- ✅ Llamadas HTTP correctas
- ✅ Manejo de errores
- ✅ Transformación de datos
- ✅ Parámetros correctos

**Ejemplo:**
```javascript
import { describe, it, expect, vi } from 'vitest';
import httpClient from './httpClient';

vi.mock('./httpClient');

describe('gestionPacientesApi', () => {
  it('should fetch patients from the API', async () => {
    const mockPacientes = [{ id: 1, nombre: 'Juan' }];
    httpClient.get.mockResolvedValue({ data: mockPacientes });
    
    const result = await obtenerPacientes();
    
    expect(result).toEqual(mockPacientes);
    expect(httpClient.get).toHaveBeenCalledWith('/pacientes');
  });
});
```

### 2. Tests de Hooks (useGestionPacientes, useListaEspera)
Cada test de hook valida:
- ✅ Estado inicial
- ✅ Carga de datos
- ✅ Actualizaciones de estado
- ✅ Manejo de errores
- ✅ Efectos secundarios

**Ejemplo:**
```javascript
import { renderHook, act, waitFor } from '@testing-library/react';
import { useGestionPacientes } from './useGestionPacientes';

describe('useGestionPacientes', () => {
  it('should initialize with empty state', () => {
    const { result } = renderHook(() => useGestionPacientes());
    
    expect(result.current.pacientes).toEqual([]);
    expect(result.current.formValido).toBe(false);
  });

  it('should register a new patient', async () => {
    const { result } = renderHook(() => useGestionPacientes());

    act(() => {
      result.current.actualizarCampo('nombre', 'Juan');
      result.current.actualizarCampo('apellido', 'Pérez');
      result.current.actualizarCampo('dni', '123456789');
    });

    await act(async () => {
      await result.current.registrar();
    });

    expect(result.current.mensaje).toContain('registrado');
  });
});
```

### 3. Tests de Componentes (GestionPacientesView, Notificaciones, etc.)
Cada test de componente valida:
- ✅ Rendering correcto
- ✅ Interacciones de usuario
- ✅ Props y estado
- ✅ Mensajes de feedback
- ✅ Estados de carga

**Ejemplo:**
```javascript
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import GestionPacientesView from './GestionPacientesView';

describe('GestionPacientesView', () => {
  it('should call registrar when button clicked', async () => {
    const user = userEvent.setup();
    const mockRegistrar = vi.fn();
    
    render(
      <GestionPacientesView 
        {...mockProps} 
        formValido={true} 
        registrar={mockRegistrar}
      />
    );

    const button = screen.getByRole('button', { name: /Registrar/i });
    await user.click(button);

    expect(mockRegistrar).toHaveBeenCalled();
  });
});
```

## 🔍 Casos de Prueba Incluidos

### APIs (35+ tests)
- ✅ Obtener datos
- ✅ Crear registros
- ✅ Actualizar estados
- ✅ Eliminar registros
- ✅ Manejo de errores
- ✅ Validación de parámetros

### Hooks (20+ tests)
- ✅ Estado inicial
- ✅ Carga de datos
- ✅ Validación de formularios
- ✅ Operaciones CRUD
- ✅ Manejo de errores
- ✅ Límpieza de mensajes

### Componentes (40+ tests)
- ✅ Rendering
- ✅ Interacciones de usuario
- ✅ Filtrado y búsqueda
- ✅ Confirmaciones
- ✅ Estados de carga
- ✅ Mensajes de feedback

## 📊 Cobertura Esperada

```
Archivo                          Lines  Functions  Branches  Statements
====================================================================
api/httpClient.js                90%    90%        85%       90%
api/gestionPacientesApi.js       95%    100%       90%       95%
api/notificacionesApi.js         90%    90%        90%       90%
api/optimizacionApi.js           85%    90%        80%       85%
api/portalApi.js                 80%    80%        80%       80%

hooks/useGestionPacientes.js     92%    95%        90%       92%
hooks/useListaEspera.js          90%    90%        88%       90%

componentes/GestionPacientes.jsx    85%    85%        80%       85%
componentes/Notificaciones.jsx      88%    90%        85%       88%
componentes/Optimizacion.jsx        85%    85%        80%       85%
componentes/ListaEspera.jsx         87%    90%        85%       87%

App.jsx                          82%    85%        80%        82%
====================================================================
Cobertura Total                  88%    90%        85%        88%
```

## 🛠️ Configuración

### vitest.config.js
```javascript
export default defineConfig({
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/vitest.setup.js',
    coverage: {
      provider: 'v8',
      lines: 80,
      functions: 80,
      branches: 80,
      statements: 80,
    },
  },
});
```

### package.json scripts
```json
{
  "scripts": {
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest run --coverage"
  }
}
```

## 🔧 Troubleshooting

### Error: "Cannot find module"
```bash
# Limpiar node_modules y reinstalar
rm -r node_modules package-lock.json
npm install
```

### Tests lentos
```bash
# Ejecutar en paralelo
npm test -- --reporter=verbose

# Ejecutar archivo específico
npm test -- src/api/httpClient.test.js
```

### Problemas con jsdom
```bash
# Asegurar que jsdom está instalado
npm install --save-dev jsdom
```

## 📚 Referencias

- [Vitest Documentation](https://vitest.dev/)
- [React Testing Library](https://testing-library.com/react)
- [User Event](https://testing-library.com/user-event)
- [Jest DOM Matchers](https://testing-library.com/jest-dom)

## 💡 Best Practices

1. **Test Behavior, Not Implementation**
   - Prueba lo que el usuario ve, no cómo funciona internamente

2. **Mock External Dependencies**
   - Mock APIs, servicios y librerías externas

3. **Use Descriptive Test Names**
   ```javascript
   // ✅ Bueno
   it('should display error message when API call fails')
   
   // ❌ Malo
   it('should handle error')
   ```

4. **Arrange-Act-Assert Pattern**
   ```javascript
   // Arrange
   const mockData = [];
   
   // Act
   const result = await fetchData();
   
   // Assert
   expect(result).toEqual(mockData);
   ```

5. **Test Edge Cases**
   - Datos vacíos
   - Errores de red
   - Estados de carga
   - Valores nulos/undefined

## 🎯 Próximos Pasos

1. Ejecutar `npm test` para validar todos los tests
2. Ejecutar `npm run test:coverage` para ver el reporte de cobertura
3. Integrar tests en el pipeline CI/CD
4. Mantener cobertura en 80% o superior

---

**Última actualización**: 2026-05-26
**Tipo de proyecto**: React + Vite + Vitest
**Cobertura objetivo**: 80%+

