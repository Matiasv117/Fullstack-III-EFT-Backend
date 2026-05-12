import httpClient from './httpClient';

const BASE_PATH = '/api/notificaciones';

export const obtenerNotificacionesPendientes = async () => {
  const { data } = await httpClient.get(`${BASE_PATH}/pendientes`);
  return data;
};

export const enviarNotificacion = async (id) => {
  const { data } = await httpClient.post(`${BASE_PATH}/${id}/enviar`);
  return data;
};

export const enviarNotificacionPorCanal = async (id, canal) => {
  const { data } = await httpClient.post(`${BASE_PATH}/${id}/enviar-canal`, null, {
    params: { canal },
  });
  return data;
};

export const enviarTodasLasNotificaciones = async () => {
  const { data } = await httpClient.post(`${BASE_PATH}/enviar-todas`);
  return data;
};

export const obtenerCanalesDisponibles = async () => {
  const { data } = await httpClient.get(`${BASE_PATH}/info/canales`);
  return data;
};

export const obtenerEstadoServicio = async () => {
  const { data } = await httpClient.get(`${BASE_PATH}/info/estado`);
  return data;
};

