import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Mock de window.confirm
global.confirm = vi.fn(() => true);

// Mock de process.env
if (typeof process === 'undefined') {
  global.process = {
    env: {},
  };
}

