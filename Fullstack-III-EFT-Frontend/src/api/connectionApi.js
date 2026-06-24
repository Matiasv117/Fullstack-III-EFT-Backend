import httpClient from './httpClient';

/**
 * Verifica la conectividad con cada microservicio del backend.
 * Retorna un objeto con el estado de cada servicio.
 */

export const checkPacientesService = async () => {
  try {
    await httpClient.get('/pacientes', { timeout: 5000 });
    return { status: 'connected', name: 'Gestión de Pacientes', port: '8080' };
  } catch {
    return { status: 'disconnected', name: 'Gestión de Pacientes', port: '8080' };
  }
};

export const checkNotificacionesService = async () => {
  try {
    await httpClient.get('/api/notificaciones/info/estado', { timeout: 5000 });
    return { status: 'connected', name: 'Notificaciones', port: '8080' };
  } catch {
    return { status: 'disconnected', name: 'Notificaciones', port: '8080' };
  }
};

export const checkOptimizacionService = async () => {
  try {
    await httpClient.get('/optimizacion/lista-espera', { timeout: 5000 });
    return { status: 'connected', name: 'Optimización', port: '8080' };
  } catch {
    return { status: 'disconnected', name: 'Optimización', port: '8080' };
  }
};

export const checkAuthService = async () => {
  try {
    // Intentar validar un token — si el server responde (incluso con 401), está vivo
    await httpClient.post('http://localhost:8097/api/auth/validate', {}, { timeout: 5000 });
    return { status: 'connected', name: 'Autenticación', port: '8097' };
  } catch (err) {
    // Si recibimos una respuesta HTTP (cualquier código), el servicio está arriba
    if (err.message && !err.message.includes('Network Error') && !err.message.includes('timeout')) {
      return { status: 'connected', name: 'Autenticación', port: '8097' };
    }
    return { status: 'disconnected', name: 'Autenticación', port: '8097' };
  }
};

export const checkAllServices = async () => {
  const results = await Promise.all([
    checkPacientesService(),
    checkNotificacionesService(),
    checkOptimizacionService(),
    checkAuthService(),
  ]);

  return results;
};
