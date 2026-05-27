import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

vi.mock('./api/portalApi', () => ({
  obtenerResumenPortal: vi.fn(),
}));
vi.mock('./componentes/GestionPacientes', () => ({
  default: () => <div>GestionPacientes</div>,
}));
vi.mock('./componentes/ListaEspera', () => ({
  default: () => <div>ListaEspera</div>,
}));
vi.mock('./componentes/Notificaciones', () => ({
  default: () => <div>Notificaciones</div>,
}));
vi.mock('./componentes/Optimizacion', () => ({
  default: () => <div>Optimizacion</div>,
}));
vi.mock('./assets/parguelas.jpg', () => ({ default: 'mock-image.jpg' }));

import App from './App';
import * as portalApi from './api/portalApi';

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    portalApi.obtenerResumenPortal.mockResolvedValue({
      resumen: {
        totalPacientes: 10,
        totalNotificacionesPendientes: 3,
      },
    });
  });

  it('should render the app', async () => {
    render(<App />);

    expect(screen.getByText('Portal RedNorte')).toBeInTheDocument();
    expect(screen.getByText(/Gestión de pacientes/)).toBeInTheDocument();
  });

  it('should display header with badges', () => {
    render(<App />);

    expect(screen.getByText('RedNorte · Sistema de salud pública')).toBeInTheDocument();
    expect(screen.getByText('Atención primaria')).toBeInTheDocument();
    expect(screen.getByText('Derivación asistida')).toBeInTheDocument();
    expect(screen.getByText('Portal unificado')).toBeInTheDocument();
  });

  it('should load portal summary', async () => {
    render(<App />);

    await waitFor(() => {
      expect(portalApi.obtenerResumenPortal).toHaveBeenCalled();
    });
  });

  it('should display metrics from portal', async () => {
    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('Pacientes registrados')).toBeInTheDocument();
      expect(screen.getByText('Notificaciones pendientes')).toBeInTheDocument();
    });
  });

  it('should show patient count metric', async () => {
    render(<App />);

    await waitFor(() => {
      const elements = screen.queryAllByText('10');
      expect(elements.length).toBeGreaterThan(0);
    }, { timeout: 1000 });
  });

  it('should show notifications count metric', async () => {
    render(<App />);

    await waitFor(() => {
      const elements = screen.queryAllByText('3');
      expect(elements.length).toBeGreaterThan(0);
    }, { timeout: 1000 });
  });

  it('should display navigation buttons', () => {
    render(<App />);

    expect(screen.getByRole('button', { name: /Pacientes/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Lista de Espera/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Notificaciones/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Optimización/i })).toBeInTheDocument();
  });

  it('should navigate to patients section', async () => {
    const user = userEvent.setup();
    render(<App />);

    const pacientesButton = screen.getByRole('button', { name: /Pacientes/i });
    await user.click(pacientesButton);

    expect(screen.getByText('GestionPacientes')).toBeInTheDocument();
  });

  it('should navigate to waiting list section', async () => {
    const user = userEvent.setup();
    render(<App />);

    const listaButton = screen.getByRole('button', { name: /Lista de Espera/i });
    await user.click(listaButton);

    expect(screen.getByText('ListaEspera')).toBeInTheDocument();
  });

  it('should navigate to notifications section', async () => {
    const user = userEvent.setup();
    render(<App />);

    const notificacionesButton = screen.getByRole('button', { name: /Notificaciones/i });
    await user.click(notificacionesButton);

    expect(screen.getAllByText('Notificaciones').length).toBeGreaterThan(0);
  });

  it('should navigate to optimization section', async () => {
    const user = userEvent.setup();
    render(<App />);

    const optimizacionButton = screen.getByRole('button', { name: /Optimización/i });
    await user.click(optimizacionButton);

    expect(screen.getByText('Optimizacion')).toBeInTheDocument();
  });

  it('should display footer', () => {
    render(<App />);

    expect(screen.getByText(/© 2026 RedNorte/)).toBeInTheDocument();
  });

  it('should mark active section button', async () => {
    const user = userEvent.setup();
    render(<App />);

    const pacientesButton = screen.getByRole('button', { name: /Pacientes/i });
    const listaButton = screen.getByRole('button', { name: /Lista de Espera/i });
    
    // Primera selección debe ser pacientes
    expect(pacientesButton).toHaveClass('active');

    await user.click(listaButton);

    // Después del click, lista debe estar activa
    expect(listaButton).toHaveClass('active');
    expect(pacientesButton).not.toHaveClass('active');
  });

  it('should handle portal summary error gracefully', async () => {
    portalApi.obtenerResumenPortal.mockRejectedValue(new Error('Fetch error'));

    render(<App />);

    await waitFor(() => {
      expect(portalApi.obtenerResumenPortal).toHaveBeenCalled();
    });

    // Should still render even with error
    expect(screen.getByText('Portal RedNorte')).toBeInTheDocument();
  });

  it('should display metrics with default values', async () => {
    portalApi.obtenerResumenPortal.mockResolvedValue(null);

    render(<App />);

    await waitFor(() => {
      expect(portalApi.obtenerResumenPortal).toHaveBeenCalled();
    }, { timeout: 1000 });

    // Should show 0 when data is null
    const zeros = screen.queryAllByText('0');
    expect(zeros.length).toBeGreaterThanOrEqual(1);
  });
});

