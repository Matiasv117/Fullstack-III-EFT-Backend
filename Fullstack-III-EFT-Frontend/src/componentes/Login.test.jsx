import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Login from './Login';

vi.mock('../api/authApi', () => ({
  default: {
    login: vi.fn(),
    loginPaciente: vi.fn(),
  },
}));

import authApi from '../api/authApi';

describe('Login', () => {
  const onLoginSuccess = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('should render login form with title', () => {
    render(<Login onLoginSuccess={onLoginSuccess} />);
    expect(screen.getByText('Sistema de Salud')).toBeInTheDocument();
    expect(screen.getByText('Inicia sesión para continuar')).toBeInTheDocument();
  });

  it('should show paciente fields by default', () => {
    render(<Login onLoginSuccess={onLoginSuccess} />);
    expect(screen.getByText('Soy Paciente')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Ingresa tu nombre')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Ingresa tu apellido')).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Ej:/)).toBeInTheDocument();
  });

  it('should switch to funcionario form', async () => {
    const user = userEvent.setup();
    render(<Login onLoginSuccess={onLoginSuccess} />);
    await user.click(screen.getByText('Soy Funcionario'));
    expect(screen.getByPlaceholderText('Ingresa tu usuario')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Ingresa tu contraseña')).toBeInTheDocument();
  });

  it('should show demo credentials for funcionario', async () => {
    const user = userEvent.setup();
    render(<Login onLoginSuccess={onLoginSuccess} />);
    await user.click(screen.getByText('Soy Funcionario'));
    expect(screen.getByText('Credenciales de demostración:')).toBeInTheDocument();
    expect(screen.getByText(/funcionario \/ funcionario123/)).toBeInTheDocument();
    expect(screen.getByText(/admin \/ admin123/)).toBeInTheDocument();
  });

  it('should show info for pacientes', () => {
    render(<Login onLoginSuccess={onLoginSuccess} />);
    expect(screen.getByText('Información para pacientes:')).toBeInTheDocument();
    expect(screen.getByText(/Ingresa tu nombre, apellido y RUT para acceder/)).toBeInTheDocument();
  });

  it('should submit paciente login', async () => {
    const user = userEvent.setup();
    const mockResponse = { token: 'jwt-paciente', username: 'Juan', role: 'ROLE_PACIENTE' };
    authApi.loginPaciente.mockResolvedValue(mockResponse);
    render(<Login onLoginSuccess={onLoginSuccess} />);
    await user.type(screen.getByPlaceholderText('Ingresa tu nombre'), 'Juan');
    await user.type(screen.getByPlaceholderText('Ingresa tu apellido'), 'Perez');
    await user.type(screen.getByPlaceholderText(/Ej:/), '12345678-5');
    await user.click(screen.getByRole('button', { name: /Ingresar como Paciente/i }));
    await waitFor(() => {
      expect(authApi.loginPaciente).toHaveBeenCalled();
    });
  }, 10000);

  it('should submit funcionario login', async () => {
    const user = userEvent.setup();
    const mockResponse = { token: 'jwt-admin', username: 'admin', role: 'ROLE_ADMIN' };
    authApi.login.mockResolvedValue(mockResponse);
    render(<Login onLoginSuccess={onLoginSuccess} />);
    await user.click(screen.getByText('Soy Funcionario'));
    await user.type(screen.getByPlaceholderText('Ingresa tu usuario'), 'admin');
    await user.type(screen.getByPlaceholderText('Ingresa tu contraseña'), 'admin123');
    await user.click(screen.getByRole('button', { name: /Iniciar Sesión/i }));
    await waitFor(() => {
      expect(authApi.login).toHaveBeenCalled();
    });
  });

  it('should display API error message', async () => {
    const user = userEvent.setup();
    authApi.login.mockRejectedValue(new Error('Credenciales incorrectas'));
    render(<Login onLoginSuccess={onLoginSuccess} />);
    await user.click(screen.getByText('Soy Funcionario'));
    await user.type(screen.getByPlaceholderText('Ingresa tu usuario'), 'admin');
    await user.type(screen.getByPlaceholderText('Ingresa tu contraseña'), 'admin123');
    await user.click(screen.getByRole('button', { name: /Iniciar Sesión/i }));
    await waitFor(() => {
      expect(screen.getByText(/Credenciales incorrectas/)).toBeInTheDocument();
    });
  });

  it('should handle generic error when no message', async () => {
    const user = userEvent.setup();
    authApi.login.mockRejectedValue(new Error());
    render(<Login onLoginSuccess={onLoginSuccess} />);
    await user.click(screen.getByText('Soy Funcionario'));
    await user.type(screen.getByPlaceholderText('Ingresa tu usuario'), 'admin');
    await user.type(screen.getByPlaceholderText('Ingresa tu contraseña'), 'admin123');
    await user.click(screen.getByRole('button', { name: /Iniciar Sesión/i }));
    await waitFor(() => {
      expect(screen.getByText(/Error al iniciar sesión/)).toBeInTheDocument();
    });
  });

  it('should store token and user info in localStorage on login', async () => {
    const user = userEvent.setup();
    const mockResponse = { token: 'jwt', username: 'admin', role: 'ROLE_ADMIN' };
    authApi.login.mockResolvedValue(mockResponse);
    render(<Login onLoginSuccess={onLoginSuccess} />);
    await user.click(screen.getByText('Soy Funcionario'));
    await user.type(screen.getByPlaceholderText('Ingresa tu usuario'), 'admin');
    await user.type(screen.getByPlaceholderText('Ingresa tu contraseña'), 'admin123');
    await user.click(screen.getByRole('button', { name: /Iniciar Sesión/i }));
    await waitFor(() => {
      expect(onLoginSuccess).toHaveBeenCalled();
    });
    expect(localStorage.getItem('token')).toBe('jwt');
    const storedUser = JSON.parse(localStorage.getItem('user'));
    expect(storedUser.username).toBe('admin');
    expect(storedUser.role).toBe('ROLE_ADMIN');
  });
});
