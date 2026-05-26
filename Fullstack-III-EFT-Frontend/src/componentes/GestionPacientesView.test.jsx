import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import GestionPacientesView from './GestionPacientesView';

describe('GestionPacientesView', () => {
  const mockProps = {
    pacientes: [],
    nuevoPaciente: { nombre: '', apellido: '', dni: '', telefono: '', email: '' },
    cargando: false,
    mensaje: '',
    error: '',
    formValido: false,
    actualizarCampo: vi.fn(),
    registrar: vi.fn(),
    agregarALista: vi.fn(),
    borrarPaciente: vi.fn(),
    recargarPacientes: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render the component', () => {
    render(<GestionPacientesView {...mockProps} />);

    expect(screen.getByText('Gestión de Pacientes')).toBeInTheDocument();
  });

  it('should display empty state when no patients', () => {
    render(<GestionPacientesView {...mockProps} />);

    expect(screen.getByText('No hay pacientes registrados todavía.')).toBeInTheDocument();
  });

  it('should display patients list', () => {
    const pacientes = [
      { id: 1, nombre: 'Juan', apellido: 'Pérez', dni: '123456789', telefono: '1234567890', email: 'juan@test.com' },
    ];
    render(<GestionPacientesView {...mockProps} pacientes={pacientes} />);

    expect(screen.getByText('Juan Pérez')).toBeInTheDocument();
    expect(screen.getByText(/123456789/)).toBeInTheDocument();
  });

  it('should call actualizarCampo when input changes', async () => {
    const user = userEvent.setup();
    render(<GestionPacientesView {...mockProps} />);

    const nombreInput = screen.getByPlaceholderText('Nombre *');
    await user.type(nombreInput, 'Juan');

    expect(mockProps.actualizarCampo).toHaveBeenCalledWith('nombre', 'Juan');
  });

  it('should display success message', () => {
    render(<GestionPacientesView {...mockProps} mensaje="Paciente registrado" />);

    expect(screen.getByText('Paciente registrado')).toBeInTheDocument();
  });

  it('should display error message', () => {
    render(<GestionPacientesView {...mockProps} error="Error al registrar" />);

    expect(screen.getByText('Error al registrar')).toBeInTheDocument();
  });

  it('should call registrar when button clicked', async () => {
    const user = userEvent.setup();
    const props = { ...mockProps, formValido: true };
    render(<GestionPacientesView {...props} />);

    const registerButton = screen.getByRole('button', { name: /Registrar paciente/i });
    await user.click(registerButton);

    expect(mockProps.registrar).toHaveBeenCalled();
  });

  it('should disable register button when loading', () => {
    const props = { ...mockProps, cargando: true, formValido: true };
    render(<GestionPacientesView {...props} />);

    const registerButton = screen.getByRole('button', { name: /Procesando/i });
    expect(registerButton).toBeDisabled();
  });

  it('should call agregarALista when add button clicked', async () => {
    const user = userEvent.setup();
    const pacientes = [
      { id: 1, nombre: 'Juan', apellido: 'Pérez', dni: '123456789', telefono: '1234567890', email: 'juan@test.com' },
    ];
    render(<GestionPacientesView {...mockProps} pacientes={pacientes} />);

    const agregarButton = screen.getByRole('button', { name: /Agregar a lista/i });
    await user.click(agregarButton);

    expect(mockProps.agregarALista).toHaveBeenCalledWith(1);
  });

  it('should call borrarPaciente when delete button clicked and confirm', async () => {
    const user = userEvent.setup();
    const pacientes = [
      { id: 1, nombre: 'Juan', apellido: 'Pérez', dni: '123456789', telefono: '1234567890', email: 'juan@test.com' },
    ];
    global.confirm = vi.fn(() => true);
    render(<GestionPacientesView {...mockProps} pacientes={pacientes} />);

    const deleteButton = screen.getByRole('button', { name: /Eliminar/i });
    await user.click(deleteButton);

    expect(mockProps.borrarPaciente).toHaveBeenCalledWith(1);
  });

  it('should call recargarPacientes when refresh button clicked', async () => {
    const user = userEvent.setup();
    render(<GestionPacientesView {...mockProps} />);

    const refreshButton = screen.getByRole('button', { name: /Actualizar lista/i });
    await user.click(refreshButton);

    expect(mockProps.recargarPacientes).toHaveBeenCalled();
  });

  it('should handle phone and email fields', async () => {
    const user = userEvent.setup();
    render(<GestionPacientesView {...mockProps} />);

    const phoneInput = screen.getByPlaceholderText('Teléfono (opcional)');
    const emailInput = screen.getByPlaceholderText('Correo electrónico (opcional)');

    await user.type(phoneInput, '1234567890');
    await user.type(emailInput, 'test@example.com');

    expect(mockProps.actualizarCampo).toHaveBeenCalledWith('telefono', '1234567890');
    expect(mockProps.actualizarCampo).toHaveBeenCalledWith('email', 'test@example.com');
  });

  it('should show patient contact info', () => {
    const pacientes = [
      { id: 1, nombre: 'Juan', apellido: 'Pérez', dni: '123456789', telefono: '1234567890', email: 'juan@test.com' },
    ];
    render(<GestionPacientesView {...mockProps} pacientes={pacientes} />);

    expect(screen.getByText(/1234567890/)).toBeInTheDocument();
    expect(screen.getByText(/juan@test.com/)).toBeInTheDocument();
  });

  it('should handle missing phone and email', () => {
    const pacientes = [
      { id: 1, nombre: 'Juan', apellido: 'Pérez', dni: '123456789', telefono: null, email: null },
    ];
    render(<GestionPacientesView {...mockProps} pacientes={pacientes} />);

    expect(screen.getByText(/Sin teléfono/)).toBeInTheDocument();
    expect(screen.getByText(/Sin email/)).toBeInTheDocument();
  });
});

