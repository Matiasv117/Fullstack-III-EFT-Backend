import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import GestionPacientes from './GestionPacientes';
import * as api from '../api/gestionPacientesApi';

vi.mock('../api/gestionPacientesApi');
vi.mock('./GestionPacientesView', () => ({
  default: () => <div>GestionPacientesView</div>,
}));

describe('GestionPacientes', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    api.obtenerPacientes.mockResolvedValue([]);
  });

  it('should render without crashing', async () => {
    render(<GestionPacientes />);

    // Component should render
    expect(screen.getByText('GestionPacientesView')).toBeInTheDocument();
  });
});

