import httpClient from './httpClient';

export const obtenerListaEsperaOptimizada = async () => {
  const { data } = await httpClient.get('/optimizacion/lista-espera');
  return data;
};

export const cancelarCitaConEstrategia = async (citaId, estrategia = 'fifo') => {
  const { data } = await httpClient.post(`/optimizacion/cancelar/${citaId}`, null, {
    params: { estrategia },
  });
  return data;
};

