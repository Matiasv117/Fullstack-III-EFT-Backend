import { describe, it, expect, vi, beforeEach } from 'vitest';
import { authApi } from './authApi';
import httpClient from './httpClient';

vi.mock('./httpClient');

describe('authApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('login', () => {
    it('should login with username and password', async () => {
      const mockResponse = { token: 'jwt-token', username: 'admin', role: 'ROLE_ADMIN' };
      httpClient.post.mockResolvedValue({ data: mockResponse });

      const result = await authApi.login('admin', 'admin123');

      expect(result).toEqual(mockResponse);
      expect(httpClient.post).toHaveBeenCalledWith('/api/auth/login', {
        username: 'admin', password: 'admin123',
      });
    });

    it('should handle login error', async () => {
      httpClient.post.mockRejectedValue(new Error('Credenciales incorrectas'));
      await expect(authApi.login('admin', 'wrong')).rejects.toThrow('Credenciales incorrectas');
    });
  });

  describe('loginPaciente', () => {
    it('should login paciente with nombre, apellido, rut', async () => {
      const mockResponse = { token: 'jwt', username: 'Juan', role: 'ROLE_PACIENTE' };
      httpClient.post.mockResolvedValue({ data: mockResponse });

      const result = await authApi.loginPaciente('Juan', 'Pérez', '12345678-9');

      expect(result).toEqual(mockResponse);
      expect(httpClient.post).toHaveBeenCalledWith('/api/auth/login-paciente', {
        nombre: 'Juan', apellido: 'Pérez', rut: '12345678-9',
      });
    });

    it('should login paciente with email', async () => {
      const mockResponse = { token: 'jwt', username: 'Juan', role: 'ROLE_PACIENTE' };
      httpClient.post.mockResolvedValue({ data: mockResponse });

      const result = await authApi.loginPaciente('Juan', 'Pérez', '12345678-9', 'juan@correo.cl');

      expect(result).toEqual(mockResponse);
      expect(httpClient.post).toHaveBeenCalledWith('/api/auth/login-paciente', {
        nombre: 'Juan', apellido: 'Pérez', rut: '12345678-9', email: 'juan@correo.cl',
      });
    });
  });

  describe('validateToken', () => {
    it('should validate a JWT token', async () => {
      const mockResponse = { valido: true };
      httpClient.post.mockResolvedValue({ data: mockResponse });

      const result = await authApi.validateToken('some-token');

      expect(result).toEqual(mockResponse);
      expect(httpClient.post).toHaveBeenCalledWith(
        '/api/auth/validate',
        {},
        { headers: { Authorization: 'Bearer some-token' } },
      );
    });
  });

  describe('getMe', () => {
    it('should get authenticated user info', async () => {
      const mockResponse = { username: 'admin', role: 'ROLE_ADMIN' };
      httpClient.get.mockResolvedValue({ data: mockResponse });

      const result = await authApi.getMe('some-token');

      expect(result).toEqual(mockResponse);
      expect(httpClient.get).toHaveBeenCalledWith(
        '/api/auth/me',
        { headers: { Authorization: 'Bearer some-token' } },
      );
    });
  });
});
