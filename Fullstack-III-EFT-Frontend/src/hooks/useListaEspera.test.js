import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { useListaEspera } from './useListaEspera';
import * as api from '../api/gestionPacientesApi';

vi.mock('../api/gestionPacientesApi');

describe('useListaEspera', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    api.obtenerListaEspera.mockResolvedValue([]);
  });

  it('should initialize with empty state', () => {
    const { result } = renderHook(() => useListaEspera());

    expect(result.current.listaEspera).toEqual([]);
    expect(result.current.cargando).toBe(false);
    expect(result.current.mensaje).toBe('');
    expect(result.current.error).toBe('');
  });

  it('should load waiting list on mount', async () => {
    const mockLista = [{ id: 1, pacienteId: 1, estado: 'PENDIENTE' }];
    api.obtenerListaEspera.mockResolvedValue(mockLista);

    const { result } = renderHook(() => useListaEspera());

    await waitFor(() => {
      expect(result.current.listaEspera).toEqual(mockLista);
      expect(result.current.cargando).toBe(false);
    }, { timeout: 3000 });
  });

  it('should handle error when loading list', async () => {
    api.obtenerListaEspera.mockRejectedValue(new Error('Load error'));

    const { result } = renderHook(() => useListaEspera());

    await waitFor(() => {
      expect(result.current.error).toBe('Load error');
    });
  });

  it('should delete from waiting list', async () => {
    const mockLista = [
      { id: 1, pacienteId: 1 },
      { id: 2, pacienteId: 2 },
    ];
    api.obtenerListaEspera.mockResolvedValue(mockLista);
    api.eliminarDelListaEspera.mockResolvedValue({});

    const { result } = renderHook(() => useListaEspera());

    await waitFor(() => {
      expect(result.current.listaEspera).toEqual(mockLista);
    });

    await act(async () => {
      await result.current.eliminarDeListaEspera(1);
    });

    expect(result.current.listaEspera).toEqual([{ id: 2, pacienteId: 2 }]);
    expect(result.current.mensaje).toBe('Paciente 1 eliminado de la lista de espera.');
  });

  it('should handle error when deleting', async () => {
    const mockLista = [{ id: 1, pacienteId: 1 }];
    api.obtenerListaEspera.mockResolvedValue(mockLista);
    api.eliminarDelListaEspera.mockRejectedValue(new Error('Delete failed'));

    const { result } = renderHook(() => useListaEspera());

    await waitFor(() => {
      expect(result.current.listaEspera).toEqual(mockLista);
    });

    await act(async () => {
      await result.current.eliminarDeListaEspera(1);
    });

    expect(result.current.error).toBe('Delete failed');
  });

  it('should update status', async () => {
    const mockLista = [{ id: 1, pacienteId: 1, estado: 'PENDIENTE' }];
    const updatedItem = { id: 1, pacienteId: 1, estado: 'ATENDIDO' };
    api.obtenerListaEspera.mockResolvedValue(mockLista);
    api.actualizarEstadoListaEspera.mockResolvedValue(updatedItem);

    const { result } = renderHook(() => useListaEspera());

    await waitFor(() => {
      expect(result.current.listaEspera).toEqual(mockLista);
    });

    await act(async () => {
      await result.current.actualizarEstado(1, 'ATENDIDO');
    });

    expect(result.current.listaEspera[0].estado).toBe('ATENDIDO');
    expect(result.current.mensaje).toBe('Estado actualizado a ATENDIDO');
  });

  it('should handle error when updating status', async () => {
    const mockLista = [{ id: 1, pacienteId: 1 }];
    api.obtenerListaEspera.mockResolvedValue(mockLista);
    api.actualizarEstadoListaEspera.mockRejectedValue(new Error('Update failed'));

    const { result } = renderHook(() => useListaEspera());

    await waitFor(() => {
      expect(result.current.listaEspera).toEqual(mockLista);
    });

    await act(async () => {
      await result.current.actualizarEstado(1, 'ATENDIDO');
    });

    expect(result.current.error).toBe('Update failed');
  });

  it('should reload waiting list', async () => {
    const mockLista = [{ id: 1, pacienteId: 1 }];
    api.obtenerListaEspera.mockResolvedValue(mockLista);

    const { result } = renderHook(() => useListaEspera());

    await waitFor(() => {
      expect(result.current.listaEspera).toEqual(mockLista);
    });

    await act(async () => {
      await result.current.cargarListaEspera();
    });

    expect(result.current.listaEspera).toEqual(mockLista);
  });

  it('should clear messages when loading', async () => {
    const mockLista = [{ id: 1, pacienteId: 1 }];
    api.obtenerListaEspera.mockResolvedValue(mockLista);

    const { result } = renderHook(() => useListaEspera());

    await waitFor(() => {
      expect(result.current.listaEspera).toEqual(mockLista);
    });

    act(() => {
      result.current.cargarListaEspera();
    });

    expect(result.current.mensaje).toBe('');
    expect(result.current.error).toBe('');
  });
});

