import httpClient from './httpClient';

const BASE_PATH = '/api/pacientes/portal';

export const obtenerMisDatos = async () => {
  const { data } = await httpClient.get(`${BASE_PATH}/mis-datos`);
  return data;
};

export const obtenerMiPosicion = async () => {
  const { data } = await httpClient.get(`${BASE_PATH}/mi-posicion`);
  return data;
};

export const obtenerMisCitas = async () => {
  const { data } = await httpClient.get(`${BASE_PATH}/mis-citas`);
  return data;
};

export const obtenerNotificacionesPorPaciente = async (pacienteId) => {
  const { data } = await httpClient.get(`/api/notificaciones/paciente/${pacienteId}`);
  return data;
};

export const obtenerMedicos = async () => {
  const { data } = await httpClient.get('/api/medicos');
  return data;
};

export const crearCitaMedica = async (citaData) => {
  const { data } = await httpClient.post('/api/citas', citaData);
  return data;
};

export const cancelarCitaPaciente = async (id) => {
  const { data } = await httpClient.delete(`/api/citas/${id}`);
  return data;
};

export const actualizarMisDatos = async (pacienteData) => {
  const { data } = await httpClient.put('/api/pacientes/portal/mis-datos', pacienteData);
  return data;
};
