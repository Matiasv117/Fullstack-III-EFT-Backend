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
}))
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

  it('contador de pacientes', async () => {
    render(<App />);

    await waitFor(() => {
      const elements = screen.queryAllByText('10');
      expect(elements.length).toBeGreaterThan(0);
    }, { timeout: 1000 });
  });

  it('contador de notificaciones', async () => {
    render(<App />);

    await waitFor(() => {
      const elements = screen.queryAllByText('3');
      expect(elements.length).toBeGreaterThan(0);
    }, { timeout: 1000 });
  });

  it('debe mostrar botones de navegacion', () => {
    render(<App />);

    expect(screen.getByRole('button', { name: /Gestión de Pacientes/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Lista de Espera/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Notificaciones/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Optimización/i })).toBeInTheDocument();
  });

  it('debe navegar a la sección de pacientes', async () => {
    const user = userEvent.setup();
    render(<App />);

    const pacientesButton = screen.getByRole('button', { name: /Gestión de Pacientes/i });
    await user.click(pacientesButton);

    expect(screen.getByText('GestionPacientes')).toBeInTheDocument();
  });

  it('debe navegar a la sección de lista de espera', async () => {
    const user = userEvent.setup();
    render(<App />);

    const listaButton = screen.getByRole('button', { name: /Lista de Espera/i });
    await user.click(listaButton);

    expect(screen.getByText('ListaEspera')).toBeInTheDocument();
  });

  it('debe navegar a la sección de notificaciones', async () => {
    const user = userEvent.setup();
    render(<App />);

    const notificacionesButton = screen.getByRole('button', { name: /Notificaciones/i });
    await user.click(notificacionesButton);

    expect(screen.getAllByText('Notificaciones').length).toBeGreaterThan(0);
  });

  it('debe navegar a la sección de optimización', async () => {
    const user = userEvent.setup();
    render(<App />);

    const optimizacionButton = screen.getByRole('button', { name: /Optimización/i });
    await user.click(optimizacionButton);

    expect(screen.getByText('Optimizacion')).toBeInTheDocument();
  });

  it('debe mostrar el footer', () => {
    render(<App />);

    expect(screen.getByText(/© 2026 RedNorte/)).toBeInTheDocument();
  });

  it('debe marcar el boton de la sección activa', async () => {
    const user = userEvent.setup();
    render(<App />);

    const pacientesButton = screen.getByRole('button', { name: /Gestión de Pacientes/i });
    const listaButton = screen.getByRole('button', { name: /Lista de Espera/i });

    // Primera selección debe ser pacientes
    expect(pacientesButton).toHaveClass('navItemActive');

    await user.click(listaButton);

    // Después del click, lista debe estar activa
    expect(listaButton).toHaveClass('navItemActive');
    expect(pacientesButton).not.toHaveClass('navItemActive');
  });

  it('debe manejar el error del resumen del portal', async () => {
    portalApi.obtenerResumenPortal.mockRejectedValue(new Error('Fetch error'));

    render(<App />);

    await waitFor(() => {
      expect(portalApi.obtenerResumenPortal).toHaveBeenCalled();
    });

    // Should still render even with error
    expect(screen.getByText('Portal RedNorte')).toBeInTheDocument();
  });

  it('debe mostrar las metricas con valores por defecto', async () => {
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

