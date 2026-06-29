import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import AyudaModal from './AyudaModal';

describe('AyudaModal', () => {
  it('should not render when isOpen is false', () => {
    const { container } = render(<AyudaModal isOpen={false} onClose={vi.fn()} />);
    expect(container.innerHTML).toBe('');
  });

  it('should render when isOpen is true', () => {
    render(<AyudaModal isOpen={true} onClose={vi.fn()} />);
    expect(screen.getByText('Ayuda del Sistema')).toBeInTheDocument();
  });

  it('should call onClose when clicking close button', () => {
    const onClose = vi.fn();
    render(<AyudaModal isOpen={true} onClose={onClose} />);
    fireEvent.click(screen.getByRole('button'));
    expect(onClose).toHaveBeenCalled();
  });

  it('should call onClose when clicking backdrop', () => {
    const onClose = vi.fn();
    const { container } = render(<AyudaModal isOpen={true} onClose={onClose} />);
    fireEvent.click(container.firstChild);
    expect(onClose).toHaveBeenCalled();
  });

  it('should not call onClose when clicking modal content', () => {
    const onClose = vi.fn();
    render(<AyudaModal isOpen={true} onClose={onClose} />);
    const modalContent = screen.getByText('Ayuda del Sistema').closest('div');
    fireEvent.click(modalContent);
    expect(onClose).not.toHaveBeenCalled();
  });

  it('should display module list', () => {
    render(<AyudaModal isOpen={true} onClose={vi.fn()} />);
    expect(screen.getByText('Módulos Principales')).toBeInTheDocument();
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Pacientes')).toBeInTheDocument();
  });
});
