import { describe, it, expect, vi, beforeEach } from 'vitest';
import httpClient from './httpClient';

vi.mock('./httpClient', () => ({
  default: {
    get: vi.fn(),
  },
}));

describe('buscarGlobal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('returns empty results for short query', async () => {
    const { buscarGlobal } = await import('./searchApi');
    const result = await buscarGlobal('a', 'ROLE_FUNCIONARIO');
    expect(result).toEqual({ pacientes: [], citas: [], listaEspera: [], funcionarios: [] });
  });

  it('returns empty results for empty query', async () => {
    const { buscarGlobal } = await import('./searchApi');
    const result = await buscarGlobal('', 'ROLE_FUNCIONARIO');
    expect(result).toEqual({ pacientes: [], citas: [], listaEspera: [], funcionarios: [] });
  });

  it('filters pacientes by nombre', async () => {
    httpClient.get
      .mockResolvedValueOnce({ data: [{ id: 1, nombre: 'Juan', apellido: 'Pérez', dni: '123' }] })
      .mockResolvedValueOnce({ data: [] })
      .mockResolvedValueOnce({ data: [] });

    const { buscarGlobal } = await import('./searchApi');
    const result = await buscarGlobal('Juan', 'ROLE_FUNCIONARIO');
    expect(result.pacientes).toHaveLength(1);
    expect(result.pacientes[0].nombre).toBe('Juan');
  });

  it('returns funcionarios for admin role', async () => {
    httpClient.get
      .mockResolvedValueOnce({ data: [] })
      .mockResolvedValueOnce({ data: [] })
      .mockResolvedValueOnce({ data: [] })
      .mockResolvedValueOnce({ data: [{ id: 1, username: 'admin1', nombreCompleto: 'Admin Uno' }] });

    const { buscarGlobal } = await import('./searchApi');
    const result = await buscarGlobal('admin1', 'ROLE_ADMIN');
    expect(result.funcionarios).toHaveLength(1);
    expect(result.funcionarios[0].username).toBe('admin1');
  });

  it('handles API errors gracefully', async () => {
    httpClient.get
      .mockRejectedValueOnce(new Error('Network error'))
      .mockRejectedValueOnce(new Error('Network error'))
      .mockRejectedValueOnce(new Error('Network error'));

    const { buscarGlobal } = await import('./searchApi');
    const result = await buscarGlobal('test', 'ROLE_FUNCIONARIO');
    expect(result.pacientes).toEqual([]);
    expect(result.citas).toEqual([]);
    expect(result.listaEspera).toEqual([]);
  });
});
