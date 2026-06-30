import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Optimizacion from './Optimizacion';
import * as api from '../api/optimizacionApi';
import * as citasApi from '../api/citasApi';

vi.mock('../api/optimizacionApi');
vi.mock('../api/citasApi');

describe('Optimizacion', () => {
  const mockLista = [
    { id: 1, pacienteId: 1, gravedad: 'ALTA', estado: 'PENDIENTE', interconsulta: 'Cardiología' },
    { id: 2, pacienteId: 2, gravedad: 'MEDIA', estado: 'PENDIENTE', interconsulta: 'Neurología' },
  ];

  const mockCitas = [
    { id: 10, pacienteId: 1, medico: { nombre: 'Dr. Pérez' }, fechaHora: '2026-07-01T10:00:00.000Z', estado: 'CONFIRMADA' },
    { id: 11, pacienteId: 2, medico: { nombre: 'Dra. Gómez' }, fechaHora: '2026-07-02T11:00:00.000Z', estado: 'CONFIRMADA' },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    api.obtenerListaEsperaOptimizada.mockResolvedValue(mockLista);
    api.cancelarCitaConEstrategia.mockResolvedValue({});
    citasApi.obtenerCitasPorEstado.mockResolvedValue(mockCitas);
    global.alert = vi.fn();
  });

  it('should render the component', async () => {
    render(<Optimizacion />);

    expect(screen.getByText('Optimización de Lista de Espera')).toBeInTheDocument();

    await waitFor(() => {
      expect(api.obtenerListaEsperaOptimizada).toHaveBeenCalled();
      expect(citasApi.obtenerCitasPorEstado).toHaveBeenCalledWith('CONFIRMADA');
    });
  });

  it('should load optimized list on mount', async () => {
    render(<Optimizacion />);

    await waitFor(() => {
      expect(api.obtenerListaEsperaOptimizada).toHaveBeenCalled();
      expect(citasApi.obtenerCitasPorEstado).toHaveBeenCalledWith('CONFIRMADA');
    });
  });

  it('should display waiting list items', async () => {
    render(<Optimizacion />);

    await waitFor(() => {
      expect(screen.getByText(/ID: 1/)).toBeInTheDocument();
      expect(screen.getByText(/ID: 2/)).toBeInTheDocument();
    });
  });

  it('should display filter options', async () => {
    render(<Optimizacion />);

    await waitFor(() => {
      const comboboxes = screen.getAllByRole('combobox');
      expect(comboboxes.length).toBeGreaterThan(0);
    });
  });

  it('should filter by gravity', async () => {
    const user = userEvent.setup();
    render(<Optimizacion />);

    await waitFor(() => {
      expect(api.obtenerListaEsperaOptimizada).toHaveBeenCalled();
    }, { timeout: 500 });

    const allSelects = screen.getAllByRole('combobox');
    const gravitySelect = allSelects[1];
    await user.selectOptions(gravitySelect, 'ALTA');
  });

  it('should filter by status', async () => {
    const user = userEvent.setup();
    render(<Optimizacion />);

    await waitFor(() => {
      expect(api.obtenerListaEsperaOptimizada).toHaveBeenCalled();
    });

    const allSelects = screen.getAllByRole('combobox');
    const statusSelect = allSelects[2];
    await user.selectOptions(statusSelect, 'ASIGNADA');
  });

  it('should handle appointment cancellation', async () => {
    const user = userEvent.setup();
    render(<Optimizacion />);

    await waitFor(() => {
      expect(api.obtenerListaEsperaOptimizada).toHaveBeenCalled();
    });

    const citaSelect = screen.getAllByRole('combobox')[0];
    await user.selectOptions(citaSelect, JSON.stringify(mockCitas[0]));

    const cancelButton = screen.getByRole('button', { name: /Cancelar y Reasignar/i });
    expect(cancelButton).not.toBeDisabled();

    await user.click(cancelButton);

    expect(api.cancelarCitaConEstrategia).toHaveBeenCalledWith(10, 'fifo');
  });

  it('should change strategy before canceling', async () => {
    const user = userEvent.setup();
    render(<Optimizacion />);

    await waitFor(() => {
      expect(api.obtenerListaEsperaOptimizada).toHaveBeenCalled();
    });

    const citaSelect = screen.getAllByRole('combobox')[0];
    await user.selectOptions(citaSelect, JSON.stringify(mockCitas[0]));

    const gravedadTab = screen.getByRole('button', { name: /Gravedad/i });
    await user.click(gravedadTab);

    const cancelButton = screen.getByRole('button', { name: /Cancelar y Reasignar/i });
    await user.click(cancelButton);

    expect(api.cancelarCitaConEstrategia).toHaveBeenCalledWith(10, 'gravedad');
  });

  it('should disable cancel button when no cita selected', () => {
    render(<Optimizacion />);

    const cancelButton = screen.getByRole('button', { name: /Cancelar y Reasignar/i });
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
    expect(screen.getAllByText(/FIFO/).length).toBeGreaterThan(0);
    expect(screen.getAllByText(/LIFO/).length).toBeGreaterThan(0);
    expect(screen.getAllByText(/Por Gravedad/).length).toBeGreaterThan(0);
  });
});
