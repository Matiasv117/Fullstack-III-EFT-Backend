import httpClient from './httpClient';

export const obtenerResumenPortal = async () => {
  const { data } = await httpClient.get('/api/portal/resumen');
  return data;
};

