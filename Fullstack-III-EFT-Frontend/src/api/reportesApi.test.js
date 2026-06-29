import { describe, it, expect, vi, beforeEach } from 'vitest';
import reportesApi from './reportesApi';
import httpClient from './httpClient';

vi.mock('./httpClient');

describe('reportesApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('obtenerMetricasListaEspera', () => {
    it('should fetch metrics', async () => {
      const mockData = { totalPendientes: 5, pacientesGravedadAlta: 2 };
      httpClient.get.mockResolvedValue({ data: mockData });

      const result = await reportesApi.obtenerMetricasListaEspera();

      expect(result).toEqual(mockData);
      expect(httpClient.get).toHaveBeenCalledWith('/api/lista-espera/metricas');
    });

    it('should handle error', async () => {
      httpClient.get.mockRejectedValue(new Error('Error'));
      await expect(reportesApi.obtenerMetricasListaEspera()).rejects.toThrow('Error');
    });
  });

  describe('listarPacientes', () => {
    it('should fetch pacientes', async () => {
      const mockData = [{ id: 1, nombre: 'Juan' }];
      httpClient.get.mockResolvedValue({ data: mockData });

      const result = await reportesApi.listarPacientes();

      expect(result).toEqual(mockData);
      expect(httpClient.get).toHaveBeenCalledWith('/api/pacientes');
    });
  });

  describe('listarEventosAuditoria', () => {
    it('should fetch auditoria eventos', async () => {
      const mockData = [{ id: 1, action: 'LOGIN' }];
      httpClient.get.mockResolvedValue({ data: mockData });

      const result = await reportesApi.listarEventosAuditoria();

      expect(result).toEqual(mockData);
      expect(httpClient.get).toHaveBeenCalledWith('/api/auditoria/eventos');
    });
  });
});
