import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AdminGestionUsuarios from './AdminGestionUsuarios';

const mockFuncionarios = [
  { id: 1, username: 'admin', nombreCompleto: 'Admin User', email: 'admin@test.cl', role: 'ROLE_ADMIN', activo: true },
  { id: 2, username: 'func1', nombreCompleto: 'Func User', email: null, role: 'ROLE_FUNCIONARIO', activo: false },
];

vi.mock('../api/adminApi', () => ({
  default: {
    listarFuncionarios: vi.fn(),
    crearFuncionario: vi.fn(),
    eliminarFuncionario: vi.fn(),
    cambiarEstado: vi.fn(),
    cambiarRol: vi.fn(),
  },
}));

import adminApi from '../api/adminApi';

describe('AdminGestionUsuarios', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    adminApi.listarFuncionarios.mockResolvedValue(mockFuncionarios);
  });

  it('should render and load funcionarios on mount', async () => {
    render(<AdminGestionUsuarios />);
    expect(screen.getByText('Gestión de Funcionarios')).toBeInTheDocument();
    await waitFor(() => {
      expect(adminApi.listarFuncionarios).toHaveBeenCalled();
    });
    await waitFor(() => {
      expect(screen.getByText('admin')).toBeInTheDocument();
    });
  });

  it('should display funcionarios count', async () => {
    render(<AdminGestionUsuarios />);
    await waitFor(() => {
      expect(screen.getByText(/Funcionarios \(2\)/)).toBeInTheDocument();
    });
  });

  it('should toggle create form', async () => {
    const user = userEvent.setup();
    render(<AdminGestionUsuarios />);
    const createButton = screen.getByRole('button', { name: /Crear$/i });
    await user.click(createButton);
    expect(screen.getByText('Crear Nuevo Funcionario')).toBeInTheDocument();
    await user.click(createButton);
    expect(screen.queryByText('Crear Nuevo Funcionario')).not.toBeInTheDocument();
  });

  it('should create funcionario and reload list', async () => {
    const user = userEvent.setup();
    adminApi.crearFuncionario.mockResolvedValue({ id: 3 });
    render(<AdminGestionUsuarios />);
    await waitFor(() => {
      expect(screen.getByText('admin')).toBeInTheDocument();
    });
    adminApi.listarFuncionarios.mockClear();
    await user.click(screen.getByRole('button', { name: /Crear$/i }));
    await user.type(screen.getByPlaceholderText('Usuario *'), 'new');
    await user.type(screen.getByPlaceholderText('Contraseña *'), 'pass1234');
    await user.type(screen.getAllByPlaceholderText('Nombre')[0], 'New User');
    await user.type(screen.getByPlaceholderText('Email'), 'e@t.co');
    await user.click(screen.getByRole('button', { name: /Guardar/i }));
    await waitFor(() => {
      expect(adminApi.crearFuncionario).toHaveBeenCalledWith('new', 'pass1234', 'New User', 'e@t.co');
    });
    await waitFor(() => {
      expect(adminApi.listarFuncionarios).toHaveBeenCalled();
    });
    await waitFor(() => {
      expect(screen.queryByText('Crear Nuevo Funcionario')).not.toBeInTheDocument();
    });
  }, 10000);

  it('should show field validation errors on create', async () => {
    const user = userEvent.setup();
    render(<AdminGestionUsuarios />);
    await waitFor(() => screen.getByText('Gestión de Funcionarios'));
    await user.click(screen.getByRole('button', { name: /Crear$/i }));
    await user.click(screen.getByRole('button', { name: /Guardar/i }));
    await waitFor(() => {
      expect(screen.getByText(/usuario es requerido/i)).toBeInTheDocument();
    });
    expect(screen.getByText(/contraseña es requerida/i)).toBeInTheDocument();
  });

  it('should handle create error', async () => {
    const user = userEvent.setup();
    adminApi.crearFuncionario.mockRejectedValue(new Error('Error del servidor'));
    render(<AdminGestionUsuarios />);
    await waitFor(() => screen.getByText('Gestión de Funcionarios'));
    await user.click(screen.getByRole('button', { name: /Crear$/i }));
    await user.type(screen.getByPlaceholderText('Usuario *'), 'nuevo');
    await user.type(screen.getByPlaceholderText('Contraseña *'), 'password123');
    await user.click(screen.getByRole('button', { name: /Guardar/i }));
    await waitFor(() => {
      expect(screen.getByText('Error del servidor')).toBeInTheDocument();
    });
  });

  it('should delete funcionario when confirmed', async () => {
    const user = userEvent.setup();
    global.confirm = vi.fn(() => true);
    adminApi.eliminarFuncionario.mockResolvedValue({});
    render(<AdminGestionUsuarios />);
    await waitFor(() => screen.getByText('admin'));
    const deleteButtons = screen.getAllByRole('button', { name: /Eliminar/i });
    await user.click(deleteButtons[0]);
    expect(global.confirm).toHaveBeenCalled();
    await waitFor(() => {
      expect(adminApi.eliminarFuncionario).toHaveBeenCalledWith(1);
    });
  });

  it('should not delete funcionario when not confirmed', async () => {
    const user = userEvent.setup();
    global.confirm = vi.fn(() => false);
    render(<AdminGestionUsuarios />);
    await waitFor(() => screen.getByText('admin'));
    const deleteButtons = screen.getAllByRole('button', { name: /Eliminar/i });
    await user.click(deleteButtons[0]);
    expect(adminApi.eliminarFuncionario).not.toHaveBeenCalled();
  });

  it('should toggle funcionario estado', async () => {
    const user = userEvent.setup();
    adminApi.cambiarEstado.mockResolvedValue({});
    render(<AdminGestionUsuarios />);
    await waitFor(() => screen.getByText('admin'));
    const stateButtons = screen.getAllByRole('button', { name: /Estado/i });
    await user.click(stateButtons[0]);
    await waitFor(() => {
      expect(adminApi.cambiarEstado).toHaveBeenCalledWith(1, false);
    });
  });

  it('should change funcionario role when confirmed', async () => {
    const user = userEvent.setup();
    global.confirm = vi.fn(() => true);
    adminApi.cambiarRol.mockResolvedValue({});
    render(<AdminGestionUsuarios />);
    await waitFor(() => screen.getByText('admin'));
    const roleButtons = screen.getAllByRole('button', { name: /Rol/i });
    await user.click(roleButtons[0]);
    await waitFor(() => {
      expect(adminApi.cambiarRol).toHaveBeenCalledWith(1, 'ROLE_FUNCIONARIO');
    });
  });

  it('should display error message from API', async () => {
    adminApi.listarFuncionarios.mockRejectedValue(new Error('Error de red'));
    render(<AdminGestionUsuarios />);
    await waitFor(() => {
      expect(screen.getByText('Error de red')).toBeInTheDocument();
    });
  });

  it('should show empty state when no funcionarios', async () => {
    adminApi.listarFuncionarios.mockResolvedValue([]);
    render(<AdminGestionUsuarios />);
    await waitFor(() => {
      expect(screen.getByText(/Funcionarios \(0\)/)).toBeInTheDocument();
    });
  });

  it('should show role and status badges', async () => {
    render(<AdminGestionUsuarios />);
    await waitFor(() => {
      expect(screen.getByText('Admin')).toBeInTheDocument();
      expect(screen.getByText('ON')).toBeInTheDocument();
      expect(screen.getByText('Func')).toBeInTheDocument();
      expect(screen.getByText('OFF')).toBeInTheDocument();
    });
  });

  it('should display user info for funcionario with null fields', async () => {
    render(<AdminGestionUsuarios />);
    await waitFor(() => {
      expect(screen.getByText('admin')).toBeInTheDocument();
      expect(screen.getByText('Admin User')).toBeInTheDocument();
      expect(screen.getByText('admin@test.cl')).toBeInTheDocument();
    });
  });
});
