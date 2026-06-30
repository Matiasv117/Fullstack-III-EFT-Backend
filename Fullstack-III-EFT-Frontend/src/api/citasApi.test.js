import { describe, it, expect, vi, beforeEach } from 'vitest';
import { obtenerTodasCitas, obtenerCitasPorEstado, cancelarCita } from './citasApi';
import httpClient from './httpClient';

vi.mock('./httpClient');

describe('citasApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('obtenerTodasCitas', () => {
    it('should fetch all citas', async () => {
      const mockCitas = [{ id: 1, pacienteId: 1, estado: 'CONFIRMADA' }];
      httpClient.get.mockResolvedValue({ data: mockCitas });

      const result = await obtenerTodasCitas();

      expect(result).toEqual(mockCitas);
      expect(httpClient.get).toHaveBeenCalledWith('/api/citas');
    });

    it('should handle error', async () => {
      const error = new Error('Fetch failed');
      httpClient.get.mockRejectedValue(error);

      await expect(obtenerTodasCitas()).rejects.toThrow('Fetch failed');
    });
  });

  describe('obtenerCitasPorEstado', () => {
    it('should fetch citas by estado', async () => {
      const mockCitas = [{ id: 1, pacienteId: 1, estado: 'CONFIRMADA' }];
      httpClient.get.mockResolvedValue({ data: mockCitas });

      const result = await obtenerCitasPorEstado('CONFIRMADA');

      expect(result).toEqual(mockCitas);
      expect(httpClient.get).toHaveBeenCalledWith('/api/citas/estado/CONFIRMADA');
    });

    it('should handle error', async () => {
      const error = new Error('Fetch failed');
      httpClient.get.mockRejectedValue(error);

      await expect(obtenerCitasPorEstado('CONFIRMADA')).rejects.toThrow('Fetch failed');
    });
  });

  describe('cancelarCita', () => {
    it('should cancel a cita by id', async () => {
      httpClient.delete.mockResolvedValue({ data: { success: true } });

      const result = await cancelarCita(1);

      expect(result).toEqual({ success: true });
      expect(httpClient.delete).toHaveBeenCalledWith('/api/citas/1');
    });

    it('should handle error when canceling', async () => {
      const error = new Error('Delete failed');
      httpClient.delete.mockRejectedValue(error);

      await expect(cancelarCita(1)).rejects.toThrow('Delete failed');
    });
  });
});
