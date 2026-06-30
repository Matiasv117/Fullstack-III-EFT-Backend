/**
 * Test Utilities y Mock Helpers
 * Proporciona funciones reutilizables para tests
 */

import { vi } from 'vitest';

/**
 * Crea un objeto mock de paciente
 */
export const createMockPaciente = (overrides = {}) => ({
  id: 1,
  nombre: 'Juan',
  apellido: 'Pérez',
  dni: '123456789',
  telefono: '1234567890',
  email: 'juan@example.com',
  ...overrides,
});

/**
 * Crea un objeto mock de lista de espera
 */
export const createMockListaEsperaItem = (overrides = {}) => ({
  id: 1,
  pacienteId: 1,
  gravedad: 'MEDIA',
  estado: 'PENDIENTE',
  interconsulta: 'Cardiología',
  fechaCreacion: '2026-05-26',
  ...overrides,
});

/**
 * Crea un objeto mock de notificación
 */
export const createMockNotificacion = (overrides = {}) => ({
  id: 1,
  pacienteId: 1,
  tipo: 'CITA',
  estado: 'PENDIENTE',
  mensaje: 'Test notification message',
  canal: 'email',
  ...overrides,
});

/**
 * Crea un objeto mock del resumen del portal
 */
export const createMockResumenPortal = (overrides = {}) => ({
  totalPacientes: 10,
  totalNotificacionesPendientes: 5,
  totalEnListaEspera: 8,
  tasaOcupacion: 0.65,
  ...overrides,
});

/**
 * Crea props por defecto para GestionPacientesView
 */
export const createMockGestionPacientesProps = (overrides = {}) => ({
  pacientes: [],
  nuevoPaciente: {
    nombre: '',
    apellido: '',
    dni: '',
    telefono: '',
    email: '',
  },
  cargando: false,
  mensaje: '',
  error: '',
  formValido: false,
  actualizarCampo: vi.fn(),
  registrar: vi.fn(),
  borrarPaciente: vi.fn(),
  recargarPacientes: vi.fn(),
  ...overrides,
});

/**
 * Espera a que una función async se complete
 */
export const waitAsync = () => new Promise(resolve => setTimeout(resolve, 0));

/**
 * Crea un mock de axios response
 */
export const createMockAxiosResponse = (data, status = 200) => ({
  data,
  status,
  statusText: 'OK',
  headers: {},
  config: {},
});

/**
 * Crea un mock de axios error
 */
export const createMockAxiosError = (message, code = 'ERR_NETWORK') => ({
  message,
  code,
  response: {
    data: { error: message, message },
    status: 500,
  },
  isAxiosError: true,
});

/**
 * Valida que una función fue llamada con los parámetros correctos
 */
export const expectFunctionCall = (mockFn, expectedArgs) => {
  expect(mockFn).toHaveBeenCalled();
  expect(mockFn).toHaveBeenCalledWith(...expectedArgs);
};

/**
 * Limpia todos los mocks
 */
export const clearAllMocks = () => {
  vi.clearAllMocks();
  vi.restoreAllMocks();
};

export default {
  createMockPaciente,
  createMockListaEsperaItem,
  createMockNotificacion,
  createMockResumenPortal,
  createMockGestionPacientesProps,
  waitAsync,
  createMockAxiosResponse,
  createMockAxiosError,
  expectFunctionCall,
  clearAllMocks,
};

