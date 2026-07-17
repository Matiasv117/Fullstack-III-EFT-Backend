import httpClient from './httpClient';

export const obtenerResumenPortal = async () => {
  try {
    const { data } = await httpClient.get('/api/portal/resumen');
    return data;
  } catch {
    return {
      totalPacientes: 0,
      totalNotificacionesPendientes: 0,
    };
  }
};

