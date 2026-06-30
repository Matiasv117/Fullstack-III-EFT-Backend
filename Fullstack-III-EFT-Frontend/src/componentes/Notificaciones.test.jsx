import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Notificaciones from './Notificaciones';
import * as api from '../api/notificacionesApi';

vi.mock('../api/notificacionesApi');

describe('Notificaciones', () => {
  const mockNotificaciones = [
    { id: 1, pacienteId: 1, tipo: 'CITA', estado: 'PENDIENTE', mensaje: 'Test notification' },
    { id: 2, pacienteId: 2, tipo: 'RESULTADO', estado: 'PENDIENTE', mensaje: 'Another notification' },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    api.obtenerNotificacionesPendientes.mockResolvedValue(mockNotificaciones);
    api.enviarNotificacion.mockResolvedValue({});
  });

  it('should render the component', async () => {
    render(<Notificaciones />);

    expect(screen.getByText('Notificaciones Pendientes')).toBeInTheDocument();

    await waitFor(() => {
      expect(api.obtenerNotificacionesPendientes).toHaveBeenCalled();
    });
  });

  it('should display loading state initially', async () => {
    api.obtenerNotificacionesPendientes.mockImplementation(() => new Promise(() => {}));

    render(<Notificaciones />);

    await waitFor(() => {
      expect(screen.getByText(/Cargando notificaciones…/)).toBeInTheDocument();
    }, { timeout: 500 });
  });

  it('should display notifications list', async () => {
    render(<Notificaciones />);

    await waitFor(() => {
      expect(screen.getByText(/Test notification/)).toBeInTheDocument();
      expect(screen.getByText(/Another notification/)).toBeInTheDocument();
    });
  });

  it('should display empty state when no notifications', async () => {
    api.obtenerNotificacionesPendientes.mockResolvedValue([]);

    render(<Notificaciones />);

    await waitFor(() => {
      expect(screen.getByText('No hay notificaciones pendientes')).toBeInTheDocument();
    });
  });

  it('should send notification when button clicked', async () => {
    const user = userEvent.setup();
    render(<Notificaciones />);

    await waitFor(() => {
      expect(api.obtenerNotificacionesPendientes).toHaveBeenCalled();
    });

    const sendButtons = screen.getAllByRole('button', { name: /Enviar/i });
    if (sendButtons.length > 0) {
      await user.click(sendButtons[0]);
      expect(api.enviarNotificacion).toHaveBeenCalledWith(1);
    }
  });

  it('should remove notification after sending', async () => {
    const user = userEvent.setup();
    render(<Notificaciones />);

    await waitFor(() => {
      expect(screen.getByText(/Test notification/)).toBeInTheDocument();
    });

    const sendButtons = screen.getAllByRole('button', { name: /Enviar/i });
    if (sendButtons.length > 0) {
      await user.click(sendButtons[0]);

      await waitFor(() => {
        expect(screen.queryByText(/Test notification/)).not.toBeInTheDocument();
      });
    }
  });

  it('should handle error when sending notification', async () => {
    const user = userEvent.setup();
    api.enviarNotificacion.mockRejectedValue(new Error('Send failed'));

    render(<Notificaciones />);

    await waitFor(() => {
      expect(api.obtenerNotificacionesPendientes).toHaveBeenCalled();
    });

    const sendButtons = screen.getAllByRole('button', { name: /Enviar/i });
    if (sendButtons.length > 0) {
      await user.click(sendButtons[0]);

      await waitFor(() => {
        expect(screen.getByText(/Send failed/)).toBeInTheDocument();
      });
    }
  });

  it('should display notification details', async () => {
    render(<Notificaciones />);

    await waitFor(() => {
      expect(screen.getByText(/ID: 1/)).toBeInTheDocument();
      expect(screen.getByText(/Paciente: #1/)).toBeInTheDocument();
      expect(screen.getByText(/Tipo: CITA/)).toBeInTheDocument();
    });
  });

  it('should handle non-array response', async () => {
    api.obtenerNotificacionesPendientes.mockResolvedValue(null);

    render(<Notificaciones />);

    await waitFor(() => {
      expect(api.obtenerNotificacionesPendientes).toHaveBeenCalled();
    });
  });
});

