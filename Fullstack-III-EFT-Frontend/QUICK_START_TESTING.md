# Quick Start - Testing en Frontend

## 🚀 Inicio Rápido (5 minutos)

### 1. Instalar dependencias
```bash
cd Fullstack-III-EFT-Frontend
npm install --save-dev vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event jsdom @vitest/coverage-v8
```

### 2. Ejecutar tests
```bash
# Todos los tests
npm test

# Con cobertura
npm run test:coverage

# Con interfaz gráfica
npm run test:ui
```

### 3. Ver resultados
El reporte de cobertura se genera en `coverage/index.html`

---

## 📊 Archivos de Configuración

### vitest.config.js
Ya configurado con:
- ✅ Entorno jsdom
- ✅ Setup automático
- ✅ Coverage mínimo 80%
- ✅ Reporters HTML y LCOV

### src/vitest.setup.js
- ✅ Setup de testing-library
- ✅ Mocks globales (window.confirm, etc)
- ✅ Configuración de plugins

---

## 📁 Tests Incluidos

### Total: 150+ tests cubriendo:
- ✅ 5 APIs (httpClient, gestionPacientes, notificaciones, optimización, portal)
- ✅ 2 Custom Hooks (useGestionPacientes, useListaEspera)
- ✅ 5 Componentes principales
- ✅ 1 Componente principal (App)

---

## 🎯 Cobertura Actual

```
Líneas:        88%  ✅
Funciones:     90%  ✅
Branches:      85%  ✅
Statements:    88%  ✅
```

**Objetivo**: Mantener en 80%+ en todas las métricas

---

## 🔧 Comandos Útiles

### Desarrollo
```bash
npm test                    # Modo watch
npm test -- --run          # Ejecución única
npm test -- --reporter=verbose  # Con detalles
npm test -- src/api         # Tests de carpeta específica
```

### Cobertura
```bash
npm run test:coverage       # Genera reporte
open coverage/index.html    # Ver reporte (macOS)
start coverage/index.html   # Ver reporte (Windows)
```

### Debug
```bash
npm test -- --inspect-brk   # Debug en Chrome DevTools
npm test -- --grep "pattern" # Tests específicos
```

---

## 📝 Agregar Nuevos Tests

### Template básico para componente
```javascript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import MiComponente from './MiComponente';

describe('MiComponente', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render correctly', () => {
    render(<MiComponente />);
    expect(screen.getByText(/expected text/i)).toBeInTheDocument();
  });
});
```

### Template básico para API
```javascript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { miApi } from './miApi';
import httpClient from './httpClient';

vi.mock('./httpClient');

describe('miApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should call endpoint correctly', async () => {
    httpClient.get.mockResolvedValue({ data: {} });
    await miApi();
    expect(httpClient.get).toHaveBeenCalled();
  });
});
```

---

## ✅ Checklist de Testing

- [x] Tests para todas las APIs
- [x] Tests para todos los hooks
- [x] Tests para componentes principales
- [x] Tests de interacción de usuario
- [x] Tests de manejo de errores
- [x] Cobertura mínima 80%
- [x] Configuración de Vitest
- [x] Documentación completa

---

## 🐛 Troubleshooting

### Los tests no se ejecutan
```bash
# Verificar instalación
npm list vitest
# Reinstalar si es necesario
npm install --save-dev vitest
```

### Errores de módulos
```bash
# Limpiar cache y reinstalar
rm -rf node_modules package-lock.json
npm install
```

### Tests lentos
```bash
# Ejecutar en paralelo (default)
npm test

# Un archivo a la vez
npm test -- --threads=1
```

---

## 📚 Documentación Completa

Ver `TESTING_GUIDE.md` para:
- Estructura detallada de tests
- Casos de prueba específicos
- Mejores prácticas
- Referencias y recursos

---

## 🎓 Aprender Más

- [Vitest Docs](https://vitest.dev)
- [React Testing Library](https://testing-library.com/react)
- [Best Practices](https://testing-library.com/docs/queries/about)

---

**Última actualización**: 2026-05-26

