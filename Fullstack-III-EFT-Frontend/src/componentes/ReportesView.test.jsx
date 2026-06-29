import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import ReportesView from './ReportesView';

const mockMetricas = {
  totalPendientes: 10,
  pacientesGravedadAlta: 5,
  pacientesGravedadMedia: 3,
  pacientesGravedadBaja: 2,
};

const mockPacientes = [
  { id: 1, nombre: 'Juan', apellido: 'Pérez' },
  { id: 2, nombre: 'María', apellido: 'González' },
  { id: 3, nombre: 'Carlos', apellido: 'López' },
];

const mockEventos = [
  { id: 1, timestamp: '2026-06-28T10:00:00Z', username: 'admin', action: 'LOGIN', details: 'Inicio de sesión' },
  { id: 2, timestamp: '2026-06-28T11:00:00Z', username: 'func1', action: 'CREATE', details: 'Creación de paciente' },
];

vi.mock('../api/reportesApi', () => ({
  default: {
    obtenerMetricasListaEspera: vi.fn(),
    listarPacientes: vi.fn(),
    listarEventosAuditoria: vi.fn(),
  },
}));

import reportesApi from '../api/reportesApi';

describe('ReportesView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    reportesApi.obtenerMetricasListaEspera.mockResolvedValue(mockMetricas);
    reportesApi.listarPacientes.mockResolvedValue(mockPacientes);
    reportesApi.listarEventosAuditoria.mockResolvedValue(mockEventos);
  });

  it('should show loading state initially', () => {
    reportesApi.obtenerMetricasListaEspera.mockImplementation(() => new Promise(() => {}));
    render(<ReportesView />);
    expect(document.querySelector('.animate-spin')).toBeInTheDocument();
  });

  it('should display report title', async () => {
    render(<ReportesView />);
    await waitFor(() => {
      expect(screen.getByText('Reportes y Auditoría')).toBeInTheDocument();
    });
  });

  it('should display metric cards with data', async () => {
    render(<ReportesView />);
    await waitFor(() => {
      expect(screen.getByText('Total Pacientes')).toBeInTheDocument();
    });
    expect(screen.getByText('En Lista de Espera')).toBeInTheDocument();
    expect(screen.getAllByText('10').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('Prioridad Alta')).toBeInTheDocument();
    expect(screen.getAllByText('5').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('Tiempo Promedio (est.)')).toBeInTheDocument();
  });

  it('should display distribution by gravity section', async () => {
    render(<ReportesView />);
    await waitFor(() => {
      expect(screen.getByText('Distribución por Gravedad')).toBeInTheDocument();
    });
    expect(screen.getAllByText(/Alta/).length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText(/Baja/).length).toBeGreaterThanOrEqual(1);
  });

  it('should display system summary', async () => {
    render(<ReportesView />);
    await waitFor(() => {
      expect(screen.getByText('Resumen del Sistema')).toBeInTheDocument();
    });
    expect(screen.getByText('Total pacientes registrados')).toBeInTheDocument();
    expect(screen.getByText('Pendientes en lista de espera')).toBeInTheDocument();
  });

  it('should display audit table', async () => {
    render(<ReportesView />);
    await waitFor(() => {
      expect(screen.getByText('Auditoría Reciente')).toBeInTheDocument();
    });
    expect(screen.getByText('admin')).toBeInTheDocument();
    expect(screen.getByText('func1')).toBeInTheDocument();
    expect(screen.getByText('LOGIN')).toBeInTheDocument();
    expect(screen.getByText('CREATE')).toBeInTheDocument();
    expect(screen.getByText('Inicio de sesión')).toBeInTheDocument();
  });

  it('should show no audit events message when empty', async () => {
    reportesApi.listarEventosAuditoria.mockResolvedValue([]);
    render(<ReportesView />);
    await waitFor(() => {
      expect(screen.getByText('No hay eventos de auditoría registrados')).toBeInTheDocument();
    });
  });

  it('should show no waiting list message when empty', async () => {
    reportesApi.obtenerMetricasListaEspera.mockResolvedValue({
      totalPendientes: 0,
      pacientesGravedadAlta: 0,
      pacientesGravedadMedia: 0,
      pacientesGravedadBaja: 0,
    });
    render(<ReportesView />);
    await waitFor(() => {
      expect(screen.getByText('No hay pacientes en lista de espera')).toBeInTheDocument();
    });
  });

  it('should handle partial API failures gracefully', async () => {
    reportesApi.listarPacientes.mockRejectedValue(new Error('Error'));
    render(<ReportesView />);
    await waitFor(() => {
      expect(screen.getByText('Total Pacientes')).toBeInTheDocument();
    });
    const dashes = screen.getAllByText('—');
    expect(dashes.length).toBeGreaterThanOrEqual(1);
  });

  it('should show only first 20 events if more exist', async () => {
    const manyEventos = Array.from({ length: 25 }, (_, i) => ({
      id: i + 1, timestamp: '2026-06-28T10:00:00Z', username: 'u', action: 'ACT', details: 'd',
    }));
    reportesApi.listarEventosAuditoria.mockResolvedValue(manyEventos);
    render(<ReportesView />);
    await waitFor(() => {
      expect(screen.getByText('Mostrando los últimos 20 eventos')).toBeInTheDocument();
    });
  });

  it('should call all three APIs on mount', async () => {
    render(<ReportesView />);
    await waitFor(() => {
      expect(reportesApi.obtenerMetricasListaEspera).toHaveBeenCalled();
      expect(reportesApi.listarPacientes).toHaveBeenCalled();
      expect(reportesApi.listarEventosAuditoria).toHaveBeenCalled();
    });
  });
});
