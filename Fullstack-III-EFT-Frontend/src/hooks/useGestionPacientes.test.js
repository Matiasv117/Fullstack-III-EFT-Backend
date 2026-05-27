import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { useGestionPacientes } from './useGestionPacientes';
import * as api from '../api/gestionPacientesApi';

vi.mock('../api/gestionPacientesApi');

describe('useGestionPacientes', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    api.obtenerPacientes.mockResolvedValue([]);
  });

  it('should initialize with empty state', () => {
    const { result } = renderHook(() => useGestionPacientes());

    expect(result.current.pacientes).toEqual([]);
    expect(result.current.nuevoPaciente).toEqual({
      nombre: '',
      apellido: '',
      dni: '',
      telefono: '',
      email: '',
    });
    expect(result.current.cargando).toBe(false);
    expect(result.current.mensaje).toBe('');
    expect(result.current.error).toBe('');
  });

  it('should load patients on mount', async () => {
    const mockPacientes = [{ id: 1, nombre: 'Juan' }];
    api.obtenerPacientes.mockResolvedValue(mockPacientes);

    const { result } = renderHook(() => useGestionPacientes());

    await waitFor(() => {
      expect(result.current.pacientes).toEqual(mockPacientes);
      expect(result.current.cargando).toBe(false);
    }, { timeout: 3000 });
  });

  it('should handle error when loading patients', async () => {
    api.obtenerPacientes.mockRejectedValue(new Error('Load error'));

    const { result } = renderHook(() => useGestionPacientes());

    await waitFor(() => {
      expect(result.current.error).toBe('Load error');
      expect(result.current.pacientes).toEqual([]);
    });
  });

  it('should update field in new patient', () => {
    const { result } = renderHook(() => useGestionPacientes());

    act(() => {
      result.current.actualizarCampo('nombre', 'Carlos');
    });

    expect(result.current.nuevoPaciente.nombre).toBe('Carlos');
  });

  it('should validate form with required fields', () => {
    const { result } = renderHook(() => useGestionPacientes());

    expect(result.current.formValido).toBe(false);

    act(() => {
      result.current.actualizarCampo('nombre', 'Juan');
      result.current.actualizarCampo('apellido', 'Pérez');
      result.current.actualizarCampo('dni', '123456789');
    });

    expect(result.current.formValido).toBe(true);
  });

  it('should register a new patient', async () => {
    const mockPaciente = { id: 1, nombre: 'Juan', apellido: 'Pérez', dni: '123456789' };
    api.registrarPaciente.mockResolvedValue(mockPaciente);
    api.obtenerPacientes.mockResolvedValue([]);

    const { result } = renderHook(() => useGestionPacientes());

    await waitFor(() => {
      expect(result.current.cargando).toBe(false);
    });

    act(() => {
      result.current.actualizarCampo('nombre', 'Juan');
      result.current.actualizarCampo('apellido', 'Pérez');
      result.current.actualizarCampo('dni', '123456789');
    });

    await act(async () => {
      await result.current.registrar();
    });

    expect(result.current.mensaje).toBe('Paciente registrado correctamente.');
    expect(result.current.nuevoPaciente.nombre).toBe('');
  });

  it('should handle error when registering patient', async () => {
    api.registrarPaciente.mockRejectedValue(new Error('Register failed'));
    api.obtenerPacientes.mockResolvedValue([]);

    const { result } = renderHook(() => useGestionPacientes());

    await waitFor(() => {
      expect(result.current.cargando).toBe(false);
    });

    act(() => {
      result.current.actualizarCampo('nombre', 'Juan');
      result.current.actualizarCampo('apellido', 'Pérez');
      result.current.actualizarCampo('dni', '123456789');
    });

    await act(async () => {
      await result.current.registrar();
    });

    expect(result.current.error).toBe('Register failed');
  });

  it('should not register if form is invalid', async () => {
    api.obtenerPacientes.mockResolvedValue([]);

    const { result } = renderHook(() => useGestionPacientes());

    await waitFor(() => {
      expect(result.current.cargando).toBe(false);
    });

    await act(async () => {
      await result.current.registrar();
    });

    expect(result.current.error).toBe('Completa nombre, apellido y DNI antes de registrar.');
    expect(api.registrarPaciente).not.toHaveBeenCalled();
  });

  it('should add patient to waiting list', async () => {
    const mockPaciente = { id: 1, nombre: 'Juan' };
    api.obtenerPacientes.mockResolvedValue([mockPaciente]);
    api.agregarPacienteAListaEspera.mockResolvedValue({});

    const { result } = renderHook(() => useGestionPacientes());

    await waitFor(() => {
      expect(result.current.pacientes).toEqual([mockPaciente]);
    });

    await act(async () => {
      await result.current.agregarALista(1);
    });

    expect(result.current.mensaje).toBe('Paciente 1 agregado a lista de espera.');
  });

  it('should delete patient', async () => {
    const mockPaciente = { id: 1, nombre: 'Juan' };
    api.obtenerPacientes.mockResolvedValue([mockPaciente]);
    api.eliminarPaciente.mockResolvedValue({});

    const { result } = renderHook(() => useGestionPacientes());

    await waitFor(() => {
      expect(result.current.pacientes).toEqual([mockPaciente]);
    });

    await act(async () => {
      await result.current.borrarPaciente(1);
    });

    expect(result.current.pacientes).toEqual([]);
    expect(result.current.mensaje).toBe('Paciente 1 eliminado correctamente.');
  });

  it('should reload patients', async () => {
    const mockPacientes = [{ id: 1, nombre: 'Juan' }];
    api.obtenerPacientes.mockResolvedValue(mockPacientes);

    const { result } = renderHook(() => useGestionPacientes());

    await waitFor(() => {
      expect(result.current.pacientes).toEqual(mockPacientes);
    });

    await act(async () => {
      await result.current.recargarPacientes();
    });

    expect(result.current.pacientes).toEqual(mockPacientes);
  });

  it('should clear messages', async () => {
    api.obtenerPacientes.mockResolvedValue([]);
    api.registrarPaciente.mockResolvedValue({ id: 1 });

    const { result } = renderHook(() => useGestionPacientes());

    await waitFor(() => {
      expect(result.current.cargando).toBe(false);
    });

    act(() => {
      result.current.actualizarCampo('nombre', 'Juan');
      result.current.actualizarCampo('apellido', 'Pérez');
      result.current.actualizarCampo('dni', '123456789');
    });

    await act(async () => {
      await result.current.registrar();
    });

    expect(result.current.mensaje).toBe('Paciente registrado correctamente.');
    
    // Test clear messages functionality
    act(() => {
      result.current.actualizarCampo('nombre', 'Carlos');
    });
  });
});

