  import { defineConfig } from 'vite'
  import react from '@vitejs/plugin-react'

  export default defineConfig({
    plugins: [react()],
    server: {
      proxy: {
        '/pacientes': 'http://localhost:8080',
        '/lista-espera': 'http://localhost:8080',
        '/api': 'http://localhost:8080',
        '/optimizacion': 'http://localhost:8080',
        '/citas': 'http://localhost:8080',
        '/medicos': 'http://localhost:8080',
        '/horarios': 'http://localhost:8080',
      },
    },
  })
