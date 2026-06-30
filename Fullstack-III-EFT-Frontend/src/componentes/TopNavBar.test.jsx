import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import TopNavBar from './TopNavBar';

vi.mock('../api/searchApi', () => ({
  buscarGlobal: vi.fn().mockResolvedValue({ pacientes: [], citas: [], listaEspera: [], funcionarios: [] }),
}));

const defaultProps = {
  user: { username: 'admin', role: 'ROLE_ADMIN' },
  searchQuery: '',
  onSearchChange: vi.fn(),
  onSectionChange: vi.fn(),
  onLogout: vi.fn(),
};

describe('TopNavBar', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render username', () => {
    render(<TopNavBar {...defaultProps} />);
    expect(screen.getByText('admin')).toBeInTheDocument();
  });

  it('should display role badge', () => {
    render(<TopNavBar {...defaultProps} />);
    expect(screen.getByText('Administrador')).toBeInTheDocument();
  });

  it('should show generic Usuario when no username', () => {
    render(<TopNavBar {...defaultProps} user={null} />);
    expect(screen.getByText('Usuario')).toBeInTheDocument();
  });

  it('should call onLogout when logout button clicked', () => {
    const onLogout = vi.fn();
    render(<TopNavBar {...defaultProps} onLogout={onLogout} />);
    const logoutButton = screen.getByTitle('Cerrar sesión');
    fireEvent.click(logoutButton);
    expect(onLogout).toHaveBeenCalled();
  });

  it('should have search input', () => {
    render(<TopNavBar {...defaultProps} />);
    const searchInput = screen.getByPlaceholderText('Buscar pacientes, citas o historiales...');
    expect(searchInput).toBeInTheDocument();
  });

  it('should call onSearchChange on typing', () => {
    const onSearchChange = vi.fn();
    render(<TopNavBar {...defaultProps} onSearchChange={onSearchChange} />);
    const searchInput = screen.getByPlaceholderText('Buscar pacientes, citas o historiales...');
    fireEvent.change(searchInput, { target: { value: 'test' } });
    expect(onSearchChange).toHaveBeenCalledWith('test');
  });

  it('should show USER badge when no role', () => {
    render(<TopNavBar {...defaultProps} user={{ username: 'test' }} />);
    expect(screen.getByText('USER')).toBeInTheDocument();
  });
});
