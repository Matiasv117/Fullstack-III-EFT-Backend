import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/vitest.setup.js',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'src/vitest.setup.js',
        '**/index.js',
        '**/*.test.jsx',
        '**/*.test.js',
        'src/main.jsx',
      ],
      thresholds: {
        lines: 85,
        functions: 80,
        branches: 65,
        statements: 80,
      },
    },
  },
});

