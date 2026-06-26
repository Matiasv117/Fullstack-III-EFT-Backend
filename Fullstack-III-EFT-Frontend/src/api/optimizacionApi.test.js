import { describe, it, expect, vi, beforeEach } from 'vitest';
import { obtenerListaEsperaOptimizada, cancelarCitaConEstrategia } from './optimizacionApi';
import httpClient from './httpClient';

vi.mock('./httpClient');

describe('optimizacionApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('obtenerListaEsperaOptimizada', () => {
    it('should fetch optimized waiting list', async () => {
      const mockLista = [{ id: 1, pacienteId: 1 }];
      httpClient.get.mockResolvedValue({ data: mockLista });

      const result = await obtenerListaEsperaOptimizada();

      expect(result).toEqual(mockLista);
      expect(httpClient.get).toHaveBeenCalledWith('/api/optimizacion/lista-espera');
    });

    it('should handle error', async () => {
      const error = new Error('Fetch failed');
      httpClient.get.mockRejectedValue(error);

      await expect(obtenerListaEsperaOptimizada()).rejects.toThrow('Fetch failed');
    });
  });

  describe('cancelarCitaConEstrategia', () => {
    it('should cancel appointment with default strategy', async () => {
      const citaId = 1;
      httpClient.post.mockResolvedValue({ data: { success: true } });

      await cancelarCitaConEstrategia(citaId);

      expect(httpClient.post).toHaveBeenCalledWith(`/api/optimizacion/cancelar/${citaId}`, null, {
        params: { estrategia: 'fifo' },
      });
    });

    it('should cancel appointment with custom strategy', async () => {
      const citaId = 1;
      const estrategia = 'gravedad';
      httpClient.post.mockResolvedValue({ data: { success: true } });

      await cancelarCitaConEstrategia(citaId, estrategia);

      expect(httpClient.post).toHaveBeenCalledWith(`/api/optimizacion/cancelar/${citaId}`, null, {
        params: { estrategia },
      });
    });

    it('should handle error when canceling', async () => {
      const error = new Error('Cancel failed');
      httpClient.post.mockRejectedValue(error);

      await expect(cancelarCitaConEstrategia(1)).rejects.toThrow('Cancel failed');
    });
  });
});

