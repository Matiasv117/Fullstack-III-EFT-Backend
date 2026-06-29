import { describe, it, expect, vi, beforeEach } from 'vitest';
import adminApi from './adminApi';
import httpClient from './httpClient';

vi.mock('./httpClient');

describe('adminApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('listarFuncionarios', () => {
    it('should fetch funcionarios', async () => {
      const mockData = [{ id: 1, username: 'admin' }];
      httpClient.get.mockResolvedValue({ data: mockData });

      const result = await adminApi.listarFuncionarios();

      expect(result).toEqual(mockData);
      expect(httpClient.get).toHaveBeenCalledWith('/api/admin/funcionarios');
    });

    it('should handle error', async () => {
      httpClient.get.mockRejectedValue(new Error('Error'));
      await expect(adminApi.listarFuncionarios()).rejects.toThrow('Error');
    });
  });

  describe('crearFuncionario', () => {
    it('should create a funcionario', async () => {
      const mockResponse = { id: 1, username: 'nuevo' };
      httpClient.post.mockResolvedValue({ data: mockResponse });

      const result = await adminApi.crearFuncionario('nuevo', 'pass', 'Nombre', 'mail@test.cl');

      expect(result).toEqual(mockResponse);
      expect(httpClient.post).toHaveBeenCalledWith('/api/admin/funcionarios', {
        username: 'nuevo', password: 'pass', nombreCompleto: 'Nombre', email: 'mail@test.cl',
      });
    });
  });

  describe('modificarFuncionario', () => {
    it('should modify a funcionario', async () => {
      httpClient.put.mockResolvedValue({ data: { id: 1 } });

      await adminApi.modificarFuncionario(1, 'user', null, 'Nombre', 'mail@test.cl');

      expect(httpClient.put).toHaveBeenCalledWith('/api/admin/funcionarios/1', {
        username: 'user', password: undefined, nombreCompleto: 'Nombre', email: 'mail@test.cl',
      });
    });
  });

  describe('eliminarFuncionario', () => {
    it('should delete a funcionario', async () => {
      httpClient.delete.mockResolvedValue({ data: { success: true } });

      const result = await adminApi.eliminarFuncionario(1);

      expect(result).toEqual({ success: true });
      expect(httpClient.delete).toHaveBeenCalledWith('/api/admin/funcionarios/1');
    });
  });

  describe('cambiarEstado', () => {
    it('should change funcionario estado', async () => {
      httpClient.put.mockResolvedValue({ data: { id: 1, activo: false } });

      await adminApi.cambiarEstado(1, false);

      expect(httpClient.put).toHaveBeenCalledWith('/api/admin/funcionarios/1/estado', { activo: false });
    });
  });

  describe('cambiarRol', () => {
    it('should change funcionario rol', async () => {
      httpClient.put.mockResolvedValue({ data: { id: 1, rol: 'ROLE_ADMIN' } });

      await adminApi.cambiarRol(1, 'ROLE_ADMIN');

      expect(httpClient.put).toHaveBeenCalledWith('/api/admin/funcionarios/1/rol', { rol: 'ROLE_ADMIN' });
    });
  });
});
