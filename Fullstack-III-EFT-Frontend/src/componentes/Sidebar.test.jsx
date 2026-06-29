import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import Sidebar from './Sidebar';

describe('Sidebar', () => {
  const baseProps = {
    user: { username: 'test', role: 'ROLE_FUNCIONARIO' },
    activeSection: 'dashboard',
    onSectionChange: vi.fn(),
    isDarkMode: false,
    onToggleDarkMode: vi.fn(),
    onLogout: vi.fn(),
  };

  it('should render funcionario menu items', () => {
    render(<Sidebar {...baseProps} />);
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Pacientes')).toBeInTheDocument();
    expect(screen.getByText('Lista de Espera')).toBeInTheDocument();
    expect(screen.getByText('Notificaciones')).toBeInTheDocument();
    expect(screen.getByText('Optimización')).toBeInTheDocument();
    expect(screen.getByText('Clínicas')).toBeInTheDocument();
    expect(screen.getByText('Reportes')).toBeInTheDocument();
    expect(screen.getByText('Ajustes')).toBeInTheDocument();
  });

  it('should render admin menu items', () => {
    render(<Sidebar {...baseProps} user={{ username: 'admin', role: 'ROLE_ADMIN' }} />);
    expect(screen.getByText('Panel Admin')).toBeInTheDocument();
    expect(screen.getByText('Gestión de Usuarios')).toBeInTheDocument();
    expect(screen.getByText('Reportes y Auditoría')).toBeInTheDocument();
    expect(screen.getByText('Configuración')).toBeInTheDocument();
    expect(screen.queryByText('Pacientes')).not.toBeInTheDocument();
  });

  it('should render paciente menu items', () => {
    render(<Sidebar {...baseProps} user={{ username: 'paciente', role: 'ROLE_PACIENTE' }} />);
    expect(screen.getByText('Mi Portal')).toBeInTheDocument();
    expect(screen.queryByText('Pacientes')).not.toBeInTheDocument();
  });

  it('should call onSectionChange when menu item clicked', () => {
    const onSectionChange = vi.fn();
    render(<Sidebar {...baseProps} onSectionChange={onSectionChange} />);
    fireEvent.click(screen.getByText('Pacientes'));
    expect(onSectionChange).toHaveBeenCalledWith('pacientes');
  });

  it('should call onToggleDarkMode when dark mode button clicked', () => {
    const onToggleDarkMode = vi.fn();
    render(<Sidebar {...baseProps} onToggleDarkMode={onToggleDarkMode} />);
    fireEvent.click(screen.getByText('Modo Oscuro'));
    expect(onToggleDarkMode).toHaveBeenCalled();
  });

  it('should show Modo Claro when dark mode is on', () => {
    render(<Sidebar {...baseProps} isDarkMode={true} />);
    expect(screen.getByText('Modo Claro')).toBeInTheDocument();
  });

  it('should call onLogout when logout button clicked', () => {
    const onLogout = vi.fn();
    render(<Sidebar {...baseProps} onLogout={onLogout} />);
    fireEvent.click(screen.getByText('Cerrar Sesión'));
    expect(onLogout).toHaveBeenCalled();
  });

  it('should open ayuda modal on help button click', () => {
    render(<Sidebar {...baseProps} />);
    fireEvent.click(screen.getByText('Ayuda'));
    expect(screen.getByText('Ayuda del Sistema')).toBeInTheDocument();
  });

  it('should show sidebar subtitle for paciente', () => {
    render(<Sidebar {...baseProps} user={{ username: 'paciente', role: 'ROLE_PACIENTE' }} />);
    expect(screen.getByText('Portal del Paciente')).toBeInTheDocument();
  });

  it('should show sidebar subtitle for admin', () => {
    render(<Sidebar {...baseProps} user={{ username: 'admin', role: 'ROLE_ADMIN' }} />);
    expect(screen.getByText('Administración del Sistema')).toBeInTheDocument();
  });

  it('should show sidebar subtitle for funcionario', () => {
    render(<Sidebar {...baseProps} />);
    expect(screen.getByText('Administración Médica')).toBeInTheDocument();
  });
});
