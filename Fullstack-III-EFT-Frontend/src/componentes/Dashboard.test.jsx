import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';

vi.mock('../api/portalApi', () => ({
  obtenerResumenPortal: vi.fn(),
}));

vi.mock('../api/reportesApi', () => ({
  default: {
    obtenerMetricasListaEspera: vi.fn(),
  },
}));

vi.mock('../api/gestionPacientesApi', () => ({
  obtenerPacientes: vi.fn(),
}));

vi.mock('../api/notificacionesApi', () => ({
  obtenerNotificacionesPendientes: vi.fn(),
}));

import Dashboard from './Dashboard';
import * as portalApi from '../api/portalApi';
import reportesApi from '../api/reportesApi';
import { obtenerPacientes } from '../api/gestionPacientesApi';
import { obtenerNotificacionesPendientes } from '../api/notificacionesApi';

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    portalApi.obtenerResumenPortal.mockResolvedValue({
      resumen: {
        totalPacientes: 10,
        totalNotificacionesPendientes: 3,
      },
    });
    reportesApi.obtenerMetricasListaEspera.mockResolvedValue({
      totalPendientes: 5,
      pacientesGravedadAlta: 2,
      pacientesGravedadMedia: 2,
      pacientesGravedadBaja: 1,
    });
    obtenerPacientes.mockResolvedValue([
      { id: 1, nombre: 'Ana', apellido: 'Pérez', dni: '12345678-9' },
      { id: 2, nombre: 'Luis', apellido: 'González', dni: '98765432-1' },
    ]);
    obtenerNotificacionesPendientes.mockResolvedValue([
      { id: 1, tipo: 'CITA_CONFIRMADA', mensaje: 'Su cita ha sido confirmada', estado: 'PENDIENTE' },
      { id: 2, tipo: 'RECORDATORIO_CITA', mensaje: 'Recuerde su cita mañana', estado: 'PENDIENTE' },
    ]);
  });

  it('should render hero banner for funcionario', async () => {
    render(<Dashboard user={{ username: 'test', role: 'ROLE_FUNCIONARIO' }} />);
    expect(screen.getByText('SISTEMA DE SALUD PÚBLICA')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText('Bienvenido, test')).toBeInTheDocument();
    });
  });

  it('should render paciente portal view', async () => {
    render(<Dashboard user={{ username: 'paciente', role: 'ROLE_PACIENTE' }} />);
    expect(screen.getByText('PORTAL DEL PACIENTE')).toBeInTheDocument();
    expect(screen.getByText('Bienvenido a tu Portal de Salud')).toBeInTheDocument();
    expect(screen.getByText('Próxima Cita Médica')).toBeInTheDocument();
    expect(screen.getByText('Tu Estado en Lista de Espera')).toBeInTheDocument();
    expect(screen.getByText('Mensajes Recientes')).toBeInTheDocument();
  });

  it('should show loading text when data is null', async () => {
    portalApi.obtenerResumenPortal.mockResolvedValue(null);
    render(<Dashboard user={{ username: 'test', role: 'ROLE_FUNCIONARIO' }} />);
    await waitFor(() => {
      expect(screen.getByText(/Cargando indicadores/)).toBeInTheDocument();
    });
  });

  it('should handle API error gracefully', async () => {
    portalApi.obtenerResumenPortal.mockRejectedValue(new Error('Network error'));
    reportesApi.obtenerMetricasListaEspera.mockRejectedValue(new Error('Network error'));
    obtenerPacientes.mockRejectedValue(new Error('Network error'));
    obtenerNotificacionesPendientes.mockRejectedValue(new Error('Network error'));
    render(<Dashboard user={{ username: 'test', role: 'ROLE_FUNCIONARIO' }} />);
    await waitFor(() => {
      expect(screen.getByText('SISTEMA DE SALUD PÚBLICA')).toBeInTheDocument();
    });
  });

  it('should render quick stats row with real data', async () => {
    render(<Dashboard user={{ username: 'test', role: 'ROLE_FUNCIONARIO' }} />);
    await waitFor(() => {
      expect(screen.getByText('Total Pacientes')).toBeInTheDocument();
      expect(screen.getByText('En Lista de Espera')).toBeInTheDocument();
      expect(screen.getByText('Prioridad ALTA')).toBeInTheDocument();
      expect(screen.getByText('Notificaciones Pendientes')).toBeInTheDocument();
    });
  });

  it('should render lista de espera severity cards', async () => {
    render(<Dashboard user={{ username: 'test', role: 'ROLE_FUNCIONARIO' }} />);
    await waitFor(() => {
      expect(screen.getByText('Lista de Espera por Gravedad')).toBeInTheDocument();
    });
  });

  it('should render ultimos pacientes registrados', async () => {
    render(<Dashboard user={{ username: 'test', role: 'ROLE_FUNCIONARIO' }} />);
    await waitFor(() => {
      expect(screen.getByText('Últimos Pacientes Registrados')).toBeInTheDocument();
      expect(screen.getByText('Ana Pérez')).toBeInTheDocument();
      expect(screen.getByText('Luis González')).toBeInTheDocument();
    });
  });

  it('should render notificaciones recientes', async () => {
    render(<Dashboard user={{ username: 'test', role: 'ROLE_FUNCIONARIO' }} />);
    await waitFor(() => {
      expect(screen.getByText('Notificaciones Recientes')).toBeInTheDocument();
      expect(screen.getByText('CITA CONFIRMADA')).toBeInTheDocument();
      expect(screen.getByText('RECORDATORIO CITA')).toBeInTheDocument();
    });
  });


});
