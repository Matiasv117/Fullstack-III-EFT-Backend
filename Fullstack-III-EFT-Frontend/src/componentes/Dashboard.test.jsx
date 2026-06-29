import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

vi.mock('../api/portalApi', () => ({
  obtenerResumenPortal: vi.fn(),
}));

import Dashboard from './Dashboard';
import * as portalApi from '../api/portalApi';

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    portalApi.obtenerResumenPortal.mockResolvedValue({
      resumen: {
        totalPacientes: 10,
        totalNotificacionesPendientes: 3,
      },
    });
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

  it('should show loading text when resumen is null', async () => {
    portalApi.obtenerResumenPortal.mockResolvedValue(null);
    render(<Dashboard user={{ username: 'test', role: 'ROLE_FUNCIONARIO' }} />);
    await waitFor(() => {
      expect(screen.getByText(/Cargando indicadores/)).toBeInTheDocument();
    });
  });

  it('should handle API error gracefully', async () => {
    portalApi.obtenerResumenPortal.mockRejectedValue(new Error('Network error'));
    render(<Dashboard user={{ username: 'test', role: 'ROLE_FUNCIONARIO' }} />);
    await waitFor(() => {
      expect(screen.getByText('SISTEMA DE SALUD PÚBLICA')).toBeInTheDocument();
    });
  });

  it('should show sync error alert', () => {
    render(<Dashboard user={{ username: 'test', role: 'ROLE_FUNCIONARIO' }} />);
    expect(screen.getByText('Error de sincronización detectado')).toBeInTheDocument();
  });

  it('should handle sync retry click', async () => {
    const user = userEvent.setup();
    render(<Dashboard user={{ username: 'test', role: 'ROLE_FUNCIONARIO' }} />);
    const retryButton = screen.getByText('Reintentar Sincronización');
    await user.click(retryButton);
    expect(screen.getByText('Sincronizando...')).toBeInTheDocument();
  });
});
