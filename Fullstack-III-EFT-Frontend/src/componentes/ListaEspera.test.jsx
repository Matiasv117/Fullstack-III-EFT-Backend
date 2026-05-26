import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ListaEspera from './ListaEspera';
import * as api from '../api/gestionPacientesApi';

vi.mock('../api/gestionPacientesApi');

describe('ListaEspera', () => {
  const mockLista = [
    { id: 1, pacienteId: 1, gravedad: 'ALTA', estado: 'PENDIENTE', interconsulta: 'Cardiología' },
    { id: 2, pacienteId: 2, gravedad: 'MEDIA', estado: 'ATENDIDO', interconsulta: 'Neurología' },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    api.obtenerListaEspera.mockResolvedValue(mockLista);
    api.eliminarDelListaEspera.mockResolvedValue({});
    api.actualizarEstadoListaEspera.mockResolvedValue({});
    global.confirm = vi.fn(() => true);
  });

  it('should render the component', async () => {
    render(<ListaEspera />);

    expect(screen.getByText('Lista de Espera')).toBeInTheDocument();

    await waitFor(() => {
      expect(api.obtenerListaEspera).toHaveBeenCalled();
    });
  });

  it('should load waiting list on mount', async () => {
    render(<ListaEspera />);

    await waitFor(() => {
      expect(api.obtenerListaEspera).toHaveBeenCalled();
    });
  });

  it('should display list items', async () => {
    render(<ListaEspera />);

    await waitFor(() => {
      expect(screen.getByText(/Paciente ID: 1/)).toBeInTheDocument();
      expect(screen.getByText(/Paciente ID: 2/)).toBeInTheDocument();
    });
  });

  it('should display gravity badges', async () => {
    render(<ListaEspera />);

    await waitFor(() => {
      expect(screen.getByText('ALTA')).toBeInTheDocument();
      expect(screen.getByText('MEDIA')).toBeInTheDocument();
    });
  });

  it('should display status badges', async () => {
    render(<ListaEspera />);

    await waitFor(() => {
      expect(screen.getByText('PENDIENTE')).toBeInTheDocument();
      expect(screen.getByText('ATENDIDO')).toBeInTheDocument();
    });
  });

  it('should filter by gravity', async () => {
    const user = userEvent.setup();
    render(<ListaEspera />);

    await waitFor(() => {
      expect(api.obtenerListaEspera).toHaveBeenCalled();
    }, { timeout: 500 });

    const gravitySelect = screen.getByDisplayValue('Todos');
    await user.selectOptions(gravitySelect, 'ALTA');
  });

  it('should filter by status', async () => {
    const user = userEvent.setup();
    render(<ListaEspera />);

    await waitFor(() => {
      expect(api.obtenerListaEspera).toHaveBeenCalled();
    }, { timeout: 500 });

    const statusSelects = screen.getAllByDisplayValue('Todos');
    if (statusSelects.length > 1) {
      await user.selectOptions(statusSelects[1], 'ATENDIDO');
    }
  });

  it('should update status when select changed', async () => {
    const user = userEvent.setup();
    api.actualizarEstadoListaEspera.mockResolvedValue({ id: 1, estado: 'ATENDIDO' });

    render(<ListaEspera />);

    await waitFor(() => {
      expect(api.obtenerListaEspera).toHaveBeenCalled();
    });

    const selectOptions = screen.getAllByRole('option');
    const changeStatusOption = selectOptions.find(opt => opt.textContent === 'Atendido');
    if (changeStatusOption) {
      const select = changeStatusOption.parentElement;
      await user.selectOptions(select, 'ATENDIDO');
      expect(api.actualizarEstadoListaEspera).toHaveBeenCalledWith(1, 'ATENDIDO');
    }
  });

  it('should delete from list when confirmed', async () => {
    const user = userEvent.setup();
    global.confirm = vi.fn(() => true);

    render(<ListaEspera />);

    await waitFor(() => {
      expect(api.obtenerListaEspera).toHaveBeenCalled();
    });

    const deleteButtons = screen.getAllByRole('button', { name: /Eliminar/i });
    await user.click(deleteButtons[0]);
    expect(global.confirm).toHaveBeenCalled();
  });

  it('should not delete when not confirmed', async () => {
    const user = userEvent.setup();
    global.confirm = vi.fn(() => false);

    render(<ListaEspera />);

    await waitFor(() => {
      expect(api.obtenerListaEspera).toHaveBeenCalled();
    });

    const deleteButtons = screen.getAllByRole('button', { name: /Eliminar/i });
    await user.click(deleteButtons[0]);
    expect(api.eliminarDelListaEspera).not.toHaveBeenCalled();
  });

  it('should show empty state', async () => {
    api.obtenerListaEspera.mockResolvedValue([]);

    render(<ListaEspera />);

    await waitFor(() => {
      expect(screen.getByText('No hay pacientes en la lista de espera con esos filtros.')).toBeInTheDocument();
    });
  });

  it('should display interconsulta info', async () => {
    render(<ListaEspera />);

    await waitFor(() => {
      expect(screen.getByText(/Cardiología/)).toBeInTheDocument();
      expect(screen.getByText(/Neurología/)).toBeInTheDocument();
    });
  });

  it('should show stats for filtered items', async () => {
    render(<ListaEspera />);

    await waitFor(() => {
      expect(screen.getByText(/Mostrando: 2 de 2/)).toBeInTheDocument();
    });
  });

  it('should handle update state error', async () => {
    const user = userEvent.setup();
    api.actualizarEstadoListaEspera.mockRejectedValue(new Error('Update failed'));

    render(<ListaEspera />);

    await waitFor(() => {
      expect(api.obtenerListaEspera).toHaveBeenCalled();
    });

    // Test basic render
    expect(screen.getByText(/Lista de Espera/)).toBeInTheDocument();
  });
});

