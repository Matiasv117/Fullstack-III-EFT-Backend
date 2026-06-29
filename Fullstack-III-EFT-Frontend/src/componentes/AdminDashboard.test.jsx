import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import AdminDashboard from './AdminDashboard';

describe('AdminDashboard', () => {
  it('should render admin panel title', () => {
    render(<AdminDashboard user={{ username: 'admin' }} />);
    expect(screen.getByText('Panel de Administración')).toBeInTheDocument();
  });

  it('should display welcome message with username', () => {
    render(<AdminDashboard user={{ username: 'admin' }} />);
    expect(screen.getByText(/Bienvenido, admin/)).toBeInTheDocument();
  });

  it('should show quick access cards', () => {
    render(<AdminDashboard user={{ username: 'admin' }} />);
    expect(screen.getByText('Gestión de Funcionarios')).toBeInTheDocument();
    expect(screen.getByText('Control de Acceso')).toBeInTheDocument();
    expect(screen.getByText('Configuración del Sistema')).toBeInTheDocument();
    expect(screen.getByText('Reportes y Auditoría')).toBeInTheDocument();
  });

  it('should show restricted access warning', () => {
    render(<AdminDashboard user={{ username: 'admin' }} />);
    expect(screen.getByText(/Acceso Restringido/)).toBeInTheDocument();
  });

  it('should show user info section', () => {
    render(<AdminDashboard user={{ username: 'admin' }} />);
    expect(screen.getByText('Información de Administrador')).toBeInTheDocument();
  });
});
