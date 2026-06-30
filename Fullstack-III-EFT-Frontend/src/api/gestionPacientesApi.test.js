import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  obtenerPacientes,
  registrarPaciente,
  agregarPacienteAListaEspera,
  eliminarPaciente,
  obtenerListaEspera,
  eliminarDelListaEspera,
  actualizarEstadoListaEspera,
  obtenerPacientesPorEstado,
  obtenerPacientesPorGravedad,
} from './gestionPacientesApi';
import httpClient from './httpClient';

vi.mock('./httpClient');

describe('gestionPacientesApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('obtenerPacientes', () => {
    it('should fetch patients from the API', async () => {
      const mockPacientes = [
        { id: 1, nombre: 'Juan', apellido: 'Pérez', dni: '123456789' },
      ];
      httpClient.get.mockResolvedValue({ data: mockPacientes });

      const result = await obtenerPacientes();

      expect(result).toEqual(mockPacientes);
      expect(httpClient.get).toHaveBeenCalledWith('/api/pacientes');
    });

    it('should handle error when fetching patients', async () => {
      const error = new Error('Network error');
      httpClient.get.mockRejectedValue(error);

      await expect(obtenerPacientes()).rejects.toThrow('Network error');
    });
  });

  describe('registrarPaciente', () => {
    it('should register a new patient', async () => {
      const nuevoPaciente = {
        nombre: 'Juan',
        apellido: 'Pérez',
        dni: '123456789',
        telefono: '1234567890',
        email: 'juan@example.com',
      };
      const mockResponse = {
        id: 1,
        ...nuevoPaciente,
      };
      httpClient.post.mockResolvedValue({ data: mockResponse });

      const result = await registrarPaciente(nuevoPaciente);

      expect(result).toEqual(mockResponse);
      expect(httpClient.post).toHaveBeenCalledWith('/api/pacientes', nuevoPaciente);
    });

    it('should handle error when registering patient', async () => {
      const error = new Error('Registration failed');
      httpClient.post.mockRejectedValue(error);

      await expect(registrarPaciente({})).rejects.toThrow('Registration failed');
    });

    it('should send patient data with required fields', async () => {
      const paciente = { nombre: 'Test', apellido: 'User', dni: '999888777' };
      httpClient.post.mockResolvedValue({ data: { id: 1, ...paciente } });

      await registrarPaciente(paciente);

      expect(httpClient.post).toHaveBeenCalledWith('/api/pacientes', paciente);
    });
  });

  describe('agregarPacienteAListaEspera', () => {
    it('should add patient to waiting list with default parameters', async () => {
      const pacienteId = 1;
      const mockResponse = { id: 1, paciente: { id: pacienteId } };
      httpClient.post.mockResolvedValue({ data: mockResponse });

      await agregarPacienteAListaEspera(pacienteId);

      expect(httpClient.post).toHaveBeenCalledWith('/api/lista-espera', {
        paciente: { id: pacienteId },
        gravedad: 'MEDIA',
        interconsulta: null,
        estado: 'PENDIENTE',
      });
    });

    it('should add patient with custom parameters', async () => {
      const pacienteId = 1;
      const params = { gravedad: 'ALTA', interconsulta: 'Cardiología', estado: 'ASIGNADA' };
      httpClient.post.mockResolvedValue({ data: {} });

      await agregarPacienteAListaEspera(pacienteId, params);

      expect(httpClient.post).toHaveBeenCalledWith('/api/lista-espera', {
        paciente: { id: pacienteId },
        ...params,
      });
    });

    it('should handle error when adding to waiting list', async () => {
      const error = new Error('Add to list failed');
      httpClient.post.mockRejectedValue(error);

      await expect(agregarPacienteAListaEspera(1)).rejects.toThrow('Add to list failed');
    });
  });

  describe('eliminarPaciente', () => {
    it('should delete a patient', async () => {
      const pacienteId = 1;
      httpClient.delete.mockResolvedValue({ data: { success: true } });

      const result = await eliminarPaciente(pacienteId);

      expect(result).toEqual({ success: true });
      expect(httpClient.delete).toHaveBeenCalledWith(`/api/pacientes/${pacienteId}`);
    });

    it('should handle error when deleting patient', async () => {
      const error = new Error('Delete failed');
      httpClient.delete.mockRejectedValue(error);

      await expect(eliminarPaciente(1)).rejects.toThrow('Delete failed');
    });
  });

  describe('obtenerListaEspera', () => {
    it('should fetch waiting list', async () => {
      const mockLista = [{ id: 1, pacienteId: 1 }];
      httpClient.get.mockResolvedValue({ data: mockLista });

      const result = await obtenerListaEspera();

      expect(result).toEqual(mockLista);
      expect(httpClient.get).toHaveBeenCalledWith('/api/lista-espera');
    });

    it('should handle error when fetching waiting list', async () => {
      const error = new Error('Fetch failed');
      httpClient.get.mockRejectedValue(error);

      await expect(obtenerListaEspera()).rejects.toThrow('Fetch failed');
    });
  });

  describe('eliminarDelListaEspera', () => {
    it('should delete from waiting list', async () => {
      const registroId = 1;
      httpClient.delete.mockResolvedValue({ data: { success: true } });

      await eliminarDelListaEspera(registroId);

      expect(httpClient.delete).toHaveBeenCalledWith(`/api/lista-espera/${registroId}`);
    });

    it('should handle error when deleting from waiting list', async () => {
      const error = new Error('Delete failed');
      httpClient.delete.mockRejectedValue(error);

      await expect(eliminarDelListaEspera(1)).rejects.toThrow('Delete failed');
    });
  });

  describe('actualizarEstadoListaEspera', () => {
    it('should update waiting list status', async () => {
      const registroId = 1;
      const nuevoEstado = 'ASIGNADA';
      httpClient.put.mockResolvedValue({ data: { id: registroId, estado: nuevoEstado } });

      await actualizarEstadoListaEspera(registroId, nuevoEstado);

      expect(httpClient.put).toHaveBeenCalledWith(`/api/lista-espera/${registroId}/estado/${nuevoEstado}`);
    });
  });

  describe('obtenerPacientesPorEstado', () => {
    it('should fetch patients by status', async () => {
      const estado = 'ASIGNADA';
      const mockPacientes = [{ id: 1, estado }];
      httpClient.get.mockResolvedValue({ data: mockPacientes });

      await obtenerPacientesPorEstado(estado);

      expect(httpClient.get).toHaveBeenCalledWith(`/api/lista-espera/estado/${estado}`);
    });
  });

  describe('obtenerPacientesPorGravedad', () => {
    it('should fetch patients by severity', async () => {
      const gravedad = 'ALTA';
      const mockPacientes = [{ id: 1, gravedad }];
      httpClient.get.mockResolvedValue({ data: mockPacientes });

      await obtenerPacientesPorGravedad(gravedad);

      expect(httpClient.get).toHaveBeenCalledWith(`/api/lista-espera/gravedad/${gravedad}`);
    });
  });
});



