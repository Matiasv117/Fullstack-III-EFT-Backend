import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  obtenerNotificacionesPendientes,
  enviarNotificacion,
  enviarNotificacionPorCanal,
  enviarTodasLasNotificaciones,
  obtenerCanalesDisponibles,
  obtenerEstadoServicio,
} from './notificacionesApi';
import httpClient from './httpClient';

vi.mock('./httpClient');

describe('notificacionesApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('obtenerNotificacionesPendientes', () => {
    it('should fetch pending notifications', async () => {
      const mockNotificaciones = [
        { id: 1, mensaje: 'Test' },
      ];
      httpClient.get.mockResolvedValue({ data: mockNotificaciones });

      const result = await obtenerNotificacionesPendientes();

      expect(result).toEqual(mockNotificaciones);
      expect(httpClient.get).toHaveBeenCalledWith('/api/notificaciones/pendientes');
    });

    it('should handle error when fetching notifications', async () => {
      const error = new Error('Fetch failed');
      httpClient.get.mockRejectedValue(error);

      await expect(obtenerNotificacionesPendientes()).rejects.toThrow('Fetch failed');
    });
  });

  describe('enviarNotificacion', () => {
    it('should send a notification', async () => {
      const notificacionId = 1;
      httpClient.post.mockResolvedValue({ data: { success: true } });

      await enviarNotificacion(notificacionId);

      expect(httpClient.post).toHaveBeenCalledWith(`/api/notificaciones/${notificacionId}/enviar`);
    });

    it('should handle error when sending notification', async () => {
      const error = new Error('Send failed');
      httpClient.post.mockRejectedValue(error);

      await expect(enviarNotificacion(1)).rejects.toThrow('Send failed');
    });
  });

  describe('enviarNotificacionPorCanal', () => {
    it('should send notification by channel', async () => {
      const notificacionId = 1;
      const canal = 'email';
      httpClient.post.mockResolvedValue({ data: { success: true } });

      await enviarNotificacionPorCanal(notificacionId, canal);

      expect(httpClient.post).toHaveBeenCalledWith(
        `/api/notificaciones/${notificacionId}/enviar-canal`,
        null,
        { params: { canal } }
      );
    });
  });

  describe('enviarTodasLasNotificaciones', () => {
    it('should send all pending notifications', async () => {
      httpClient.post.mockResolvedValue({ data: { count: 5 } });

      await enviarTodasLasNotificaciones();

      expect(httpClient.post).toHaveBeenCalledWith('/api/notificaciones/enviar-todas');
    });
  });

  describe('obtenerCanalesDisponibles', () => {
    it('should fetch available channels', async () => {
      const mockCanales = ['email', 'sms'];
      httpClient.get.mockResolvedValue({ data: mockCanales });

      const result = await obtenerCanalesDisponibles();

      expect(result).toEqual(mockCanales);
      expect(httpClient.get).toHaveBeenCalledWith('/api/notificaciones/info/canales');
    });
  });

  describe('obtenerEstadoServicio', () => {
    it('should fetch service status', async () => {
      const mockStatus = { status: 'ok', uptime: 3600 };
      httpClient.get.mockResolvedValue({ data: mockStatus });

      const result = await obtenerEstadoServicio();

      expect(result).toEqual(mockStatus);
      expect(httpClient.get).toHaveBeenCalledWith('/api/notificaciones/info/estado');
    });
  });
});

