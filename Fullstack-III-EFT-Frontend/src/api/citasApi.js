import httpClient from './httpClient';

export const obtenerTodasCitas = async () => {
  const { data } = await httpClient.get('/api/citas');
  return data;
};

export const obtenerCitasPorEstado = async (estado) => {
  const { data } = await httpClient.get(`/api/citas/estado/${estado}`);
  return data;
};

export const cancelarCita = async (id) => {
  const { data } = await httpClient.delete(`/api/citas/${id}`);
  return data;
};

export const crearCita = async (citaData) => {
  const { data } = await httpClient.post('/api/citas', citaData);
  return data;
};
