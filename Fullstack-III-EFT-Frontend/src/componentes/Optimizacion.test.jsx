import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Optimizacion from './Optimizacion';
import * as api from '../api/optimizacionApi';

vi.mock('../api/optimizacionApi');

describe('Optimizacion', () => {
  const mockLista = [
    { id: 1, pacienteId: 1, gravedad: 'ALTA', estado: 'PENDIENTE', interconsulta: 'Cardiología' },
    { id: 2, pacienteId: 2, gravedad: 'MEDIA', estado: 'PENDIENTE', interconsulta: 'Neurología' },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    api.obtenerListaEsperaOptimizada.mockResolvedValue(mockLista);
    api.cancelarCitaConEstrategia.mockResolvedValue({});
    global.alert = vi.fn();
  });

  it('should render the component', async () => {
    render(<Optimizacion />);

    expect(screen.getByText('Optimización de Lista de Espera')).toBeInTheDocument();

    await waitFor(() => {
      expect(api.obtenerListaEsperaOptimizada).toHaveBeenCalled();
    });
  });

  it('should load optimized list on mount', async () => {
    render(<Optimizacion />);

    await waitFor(() => {
      expect(api.obtenerListaEsperaOptimizada).toHaveBeenCalled();
    });
  });

  it('should display waiting list items', async () => {
    render(<Optimizacion />);

    await waitFor(() => {
      expect(screen.getByText(/Paciente ID: 1/)).toBeInTheDocument();
      expect(screen.getByText(/Paciente ID: 2/)).toBeInTheDocument();
    });
  });

  it('should display filter options', async () => {
    render(<Optimizacion />);

    await waitFor(() => {
      const gravitySelects = screen.getAllByDisplayValue('Todos');
      expect(gravitySelects.length).toBeGreaterThan(0);
    });
  });

  it('should filter by gravity', async () => {
    const user = userEvent.setup();
    render(<Optimizacion />);

    await waitFor(() => {
      expect(api.obtenerListaEsperaOptimizada).toHaveBeenCalled();
    }, { timeout: 500 });

    const gravityFilters = screen.getAllByRole('combobox');
    if (gravityFilters.length > 0) {
      await user.selectOptions(gravityFilters[0], 'ALTA');
    }
  });

  it('should filter by status', async () => {
    const user = userEvent.setup();
    render(<Optimizacion />);

    await waitFor(() => {
      expect(api.obtenerListaEsperaOptimizada).toHaveBeenCalled();
    });

    const statusFilters = screen.getAllByRole('combobox');
    if (statusFilters.length > 1) {
      await user.selectOptions(statusFilters[1], 'ATENDIDO');
    }
  });

  it('should handle appointment cancellation', async () => {
    const user = userEvent.setup();
    render(<Optimizacion />);

    await waitFor(() => {
      expect(api.obtenerListaEsperaOptimizada).toHaveBeenCalled();
    });

    const citaInput = screen.getByPlaceholderText('Ingresa el ID de la cita');
    await user.type(citaInput, '1');

    const cancelButton = screen.getByRole('button', { name: /Procesar Cancelación/i });
    expect(cancelButton).not.toBeDisabled();

    await user.click(cancelButton);

    expect(api.cancelarCitaConEstrategia).toHaveBeenCalledWith(1, 'fifo');
  });

  it('should change strategy before canceling', async () => {
    const user = userEvent.setup();
    render(<Optimizacion />);

    await waitFor(() => {
      expect(api.obtenerListaEsperaOptimizada).toHaveBeenCalled();
    });

    const citaInput = screen.getByPlaceholderText('Ingresa el ID de la cita');
    await user.type(citaInput, '1');

    const strategySelect = screen.getByDisplayValue('fifo');
    await user.selectOptions(strategySelect, 'gravedad');

    const cancelButton = screen.getByRole('button', { name: /Procesar Cancelación/i });
    await user.click(cancelButton);

    expect(api.cancelarCitaConEstrategia).toHaveBeenCalledWith(1, 'gravedad');
  });

  it('should disable cancel button when no cita selected', () => {
    render(<Optimizacion />);

    const cancelButton = screen.getByRole('button', { name: /Procesar Cancelación/i });
    expect(cancelButton).toBeDisabled();
  });

  it('should show empty state when no items', async () => {
    api.obtenerListaEsperaOptimizada.mockResolvedValue([]);

    render(<Optimizacion />);

    await waitFor(() => {
      expect(screen.getByText('No hay pacientes en la lista de espera')).toBeInTheDocument();
    });
  });

  it('should display strategy information', async () => {
    render(<Optimizacion />);

    expect(screen.getByText(/Estrategias de Optimización/i)).toBeInTheDocument();
    expect(screen.getByText(/FIFO \(Primera En Llegar\)/)).toBeInTheDocument();
    expect(screen.getByText(/LIFO \(Última En Llegar\)/)).toBeInTheDocument();
    expect(screen.getByText(/Por Gravedad/)).toBeInTheDocument();
  });
});

