import { describe, it, expect, vi, beforeEach } from 'vitest';
import { obtenerResumenPortal } from './portalApi';
import httpClient from './httpClient';

vi.mock('./httpClient');

describe('portalApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('obtenerResumenPortal', () => {
    it('should fetch portal summary', async () => {
      const mockResumen = {
        totalPacientes: 10,
        totalNotificacionesPendientes: 5,
      };
      httpClient.get.mockResolvedValue({ data: mockResumen });

      const result = await obtenerResumenPortal();

      expect(result).toEqual(mockResumen);
      expect(httpClient.get).toHaveBeenCalledWith('/api/portal/resumen');
    });

    it('should return fallback data when endpoint is unavailable', async () => {
      const error = new Error('Fetch failed');
      httpClient.get.mockRejectedValue(error);

      const result = await obtenerResumenPortal();

      expect(result).toEqual({
        totalPacientes: 0,
        totalNotificacionesPendientes: 0,
      });
    });

    it('should return data structure with expected fields', async () => {
      const mockResumen = {
        resumen: {
          totalPacientes: 20,
          totalNotificacionesPendientes: 3,
        },
      };
      httpClient.get.mockResolvedValue({ data: mockResumen });

      const result = await obtenerResumenPortal();

      expect(result).toHaveProperty('resumen');
      expect(result.resumen).toHaveProperty('totalPacientes');
      expect(result.resumen).toHaveProperty('totalNotificacionesPendientes');
    });

    it('should handle null response gracefully', async () => {
      httpClient.get.mockResolvedValue({ data: null });

      const result = await obtenerResumenPortal();

      expect(result).toBeNull();
    });
  });
});

