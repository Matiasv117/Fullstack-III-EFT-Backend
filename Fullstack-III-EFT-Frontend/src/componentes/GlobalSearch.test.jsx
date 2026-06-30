import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import GlobalSearch from './GlobalSearch';

describe('GlobalSearch', () => {
  const onNavigate = vi.fn();
  const onClose = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders nothing when query is too short', () => {
    const { container } = render(
      <GlobalSearch results={{ pacientes: [{ id: 1, nombre: 'Juan' }] }} query="J" onNavigate={onNavigate} onClose={onClose} />
    );
    expect(container.innerHTML).toBe('');
  });

  it('renders nothing when no results', () => {
    const { container } = render(
      <GlobalSearch results={{ pacientes: [], citas: [], listaEspera: [], funcionarios: [] }} query="test" onNavigate={onNavigate} onClose={onClose} />
    );
    expect(container.innerHTML).toBe('');
  });

  it('renders grouped results', () => {
    render(
      <GlobalSearch
        results={{ pacientes: [{ id: 1, nombre: 'Juan', apellido: 'Pérez', dni: '123' }], citas: [], listaEspera: [], funcionarios: [] }}
        query="Juan"
        onNavigate={onNavigate}
        onClose={onClose}
      />
    );
    expect(screen.getByText('Pacientes')).toBeInTheDocument();
    expect(screen.getByText('Juan Pérez')).toBeInTheDocument();
  });

  it('calls onNavigate and onClose on result click', () => {
    render(
      <GlobalSearch
        results={{ pacientes: [{ id: 1, nombre: 'Juan', apellido: 'Pérez', dni: '123' }], citas: [], listaEspera: [], funcionarios: [] }}
        query="Juan"
        onNavigate={onNavigate}
        onClose={onClose}
      />
    );
    fireEvent.click(screen.getByText('Juan Pérez'));
    expect(onNavigate).toHaveBeenCalledWith('pacientes');
    expect(onClose).toHaveBeenCalled();
  });

  it('renders funcionarios group for admin', () => {
    render(
      <GlobalSearch
        results={{ pacientes: [], citas: [], listaEspera: [], funcionarios: [{ id: 1, username: 'admin1', nombreCompleto: 'Admin Uno' }] }}
        query="admin1"
        onNavigate={onNavigate}
        onClose={onClose}
      />
    );
    expect(screen.getByText('Funcionarios')).toBeInTheDocument();
    expect(screen.getByText('admin1')).toBeInTheDocument();
  });

  it('closes on Escape key', () => {
    render(
      <GlobalSearch
        results={{ pacientes: [{ id: 1, nombre: 'Juan' }], citas: [], listaEspera: [], funcionarios: [] }}
        query="Juan"
        onNavigate={onNavigate}
        onClose={onClose}
      />
    );
    fireEvent.keyDown(document, { key: 'Escape' });
    expect(onClose).toHaveBeenCalled();
  });
});
