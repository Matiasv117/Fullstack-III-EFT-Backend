import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import TopNavBar from './TopNavBar';

describe('TopNavBar', () => {
  it('should render username', () => {
    render(<TopNavBar user={{ username: 'admin', role: 'ROLE_ADMIN' }} onLogout={vi.fn()} />);
    expect(screen.getByText('admin')).toBeInTheDocument();
  });

  it('should display role badge', () => {
    render(<TopNavBar user={{ username: 'admin', role: 'ROLE_ADMIN' }} onLogout={vi.fn()} />);
    expect(screen.getByText('ADMIN')).toBeInTheDocument();
  });

  it('should show generic Usuario when no username', () => {
    render(<TopNavBar user={null} onLogout={vi.fn()} />);
    expect(screen.getByText('Usuario')).toBeInTheDocument();
  });

  it('should call onLogout when logout button clicked', () => {
    const onLogout = vi.fn();
    render(<TopNavBar user={{ username: 'admin' }} onLogout={onLogout} />);
    const logoutButton = screen.getByTitle('Cerrar sesión');
    fireEvent.click(logoutButton);
    expect(onLogout).toHaveBeenCalled();
  });

  it('should have search input', () => {
    render(<TopNavBar user={{ username: 'admin' }} onLogout={vi.fn()} />);
    const searchInput = screen.getByPlaceholderText('Buscar pacientes, citas o historiales...');
    expect(searchInput).toBeInTheDocument();
  });

  it('should update search value on typing', () => {
    render(<TopNavBar user={{ username: 'admin' }} onLogout={vi.fn()} />);
    const searchInput = screen.getByPlaceholderText('Buscar pacientes, citas o historiales...');
    fireEvent.change(searchInput, { target: { value: 'test query' } });
    expect(searchInput.value).toBe('test query');
  });

  it('should show USER badge when no role', () => {
    render(<TopNavBar user={{ username: 'test' }} onLogout={vi.fn()} />);
    expect(screen.getByText('USER')).toBeInTheDocument();
  });
});
