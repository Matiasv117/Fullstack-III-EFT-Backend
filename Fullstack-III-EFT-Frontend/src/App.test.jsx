import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

vi.mock('./api/portalApi', () => ({
  obtenerResumenPortal: vi.fn(),
}));
vi.mock('./api/reportesApi', () => ({
  default: {
    obtenerMetricasListaEspera: vi.fn(),
  },
}));
vi.mock('./api/gestionPacientesApi', () => ({
  obtenerPacientes: vi.fn(),
}));
vi.mock('./api/notificacionesApi', () => ({
  obtenerNotificacionesPendientes: vi.fn(),
}));
vi.mock('./componentes/Login', () => ({
  default: ({ onLoginSuccess }) => <div data-testid="login-component">Login</div>,
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
import reportesApi from './api/reportesApi';
import { obtenerPacientes } from './api/gestionPacientesApi';
import { obtenerNotificacionesPendientes } from './api/notificacionesApi';

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('token', 'fake-token');
    localStorage.setItem('user', JSON.stringify({ username: 'test', role: 'ROLE_USER' }));
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
    ]);
    obtenerNotificacionesPendientes.mockResolvedValue([
      { id: 1, tipo: 'CITA_CONFIRMADA', mensaje: 'Su cita ha sido confirmada', estado: 'PENDIENTE' },
    ]);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should render the app', async () => {
    render(<App />);

    expect(screen.getByText('RedNorte')).toBeInTheDocument();
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
  });

  it('should display header with stats', async () => {
    render(<App />);

    expect(screen.getByText('SISTEMA DE SALUD PÚBLICA')).toBeInTheDocument();
    expect(screen.getByText('Bienvenido, test')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText('Total Pacientes')).toBeInTheDocument();
      expect(screen.getByText('En Lista de Espera')).toBeInTheDocument();
      expect(screen.getByText('Prioridad ALTA')).toBeInTheDocument();
      expect(screen.getByText('Notificaciones Pendientes')).toBeInTheDocument();
    });
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

    expect(screen.getByRole('button', { name: 'Pacientes' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Lista de Espera' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Notificaciones' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Optimización' })).toBeInTheDocument();
  });

  it('debe navegar a la sección de pacientes', async () => {
    const user = userEvent.setup();
    render(<App />);

    const pacientesButton = screen.getByRole('button', { name: 'Pacientes' });
    await user.click(pacientesButton);

    expect(screen.getByText('GestionPacientes')).toBeInTheDocument();
  });

  it('debe navegar a la sección de lista de espera', async () => {
    const user = userEvent.setup();
    render(<App />);

    const listaButton = screen.getByRole('button', { name: 'Lista de Espera' });
    await user.click(listaButton);

    expect(screen.getByText('ListaEspera')).toBeInTheDocument();
  });

  it('debe navegar a la sección de notificaciones', async () => {
    const user = userEvent.setup();
    render(<App />);

    const notificacionesButton = screen.getByRole('button', { name: 'Notificaciones' });
    await user.click(notificacionesButton);

    expect(screen.getAllByText('Notificaciones').length).toBeGreaterThan(0);
  });

  it('debe navegar a la sección de optimización', async () => {
    const user = userEvent.setup();
    render(<App />);

    const optimizacionButton = screen.getByRole('button', { name: 'Optimización' });
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

    const pacientesButton = screen.getByRole('button', { name: 'Pacientes' });
    const listaButton = screen.getByRole('button', { name: 'Lista de Espera' });

    // Primera selección debe ser dashboard (por defecto)
    expect(screen.getByRole('button', { name: 'Dashboard' })).toHaveClass('sidebar-item-active');

    await user.click(pacientesButton);

    // Después del click, pacientes debe estar activa
    expect(pacientesButton).toHaveClass('sidebar-item-active');
    expect(listaButton).not.toHaveClass('sidebar-item-active');
  });

  it('debe manejar el error del resumen del portal', async () => {
    portalApi.obtenerResumenPortal.mockRejectedValue(new Error('Fetch error'));

    render(<App />);

    await waitFor(() => {
      expect(portalApi.obtenerResumenPortal).toHaveBeenCalled();
    });

    // Should still render even with error
    expect(screen.getByText('RedNorte')).toBeInTheDocument();
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

  it('debe mostrar Login cuando no hay token', () => {
    localStorage.clear();
    render(<App />);
    expect(screen.getByTestId('login-component')).toBeInTheDocument();
  });

  it('debe mostrar admin dashboard para admin', () => {
    localStorage.setItem('user', JSON.stringify({ username: 'admin', role: 'ROLE_ADMIN' }));
    render(<App />);
    expect(screen.getByText('Panel de Administración')).toBeInTheDocument();
  });

  it('debe navegar a gestion de usuarios como admin', async () => {
    localStorage.setItem('user', JSON.stringify({ username: 'admin', role: 'ROLE_ADMIN' }));
    const user = userEvent.setup();
    render(<App />);
    await user.click(screen.getByRole('button', { name: 'Gestión de Usuarios' }));
    expect(screen.getByText('Gestión de Funcionarios')).toBeInTheDocument();
  });

  it('debe cerrar sesion', async () => {
    const user = userEvent.setup();
    render(<App />);
    const logoutButtons = screen.getAllByRole('button', { name: /Cerrar Sesión|Cerrar sesión/i });
    await user.click(logoutButtons[0]);
    await waitFor(() => {
      expect(screen.getByTestId('login-component')).toBeInTheDocument();
    });
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('debe navegar a ajustes como admin', async () => {
    localStorage.setItem('user', JSON.stringify({ username: 'admin', role: 'ROLE_ADMIN' }));
    const user = userEvent.setup();
    render(<App />);
    await user.click(screen.getByRole('button', { name: 'Configuración' }));
    expect(screen.getByText('Configuración del Sistema')).toBeInTheDocument();
  });

  it('debe alternar modo oscuro', async () => {
    const user = userEvent.setup();
    render(<App />);
    const darkModeButton = screen.getByText('Modo Oscuro');
    await user.click(darkModeButton);
    expect(screen.getByText('Modo Claro')).toBeInTheDocument();
  });
});

