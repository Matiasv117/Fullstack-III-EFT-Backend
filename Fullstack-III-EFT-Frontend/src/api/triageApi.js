import httpClient from './httpClient';

export const enviarAutotriage = async (datos) => {
  try {
    const { data } = await httpClient.post('/api/autotriage', datos);
    return data;
  } catch {
    return {
      prioridad: { nivel: datos.gravedad },
      listaEspera: { id: null },
      prioridadError: null,
      listaError: 'Servicio de autotriage no disponible en este despliegue',
    };
  }
};
