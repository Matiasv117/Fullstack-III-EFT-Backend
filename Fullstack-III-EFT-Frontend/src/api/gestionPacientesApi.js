import httpClient from './httpClient';

const PACIENTES_PATH = '/pacientes';
const LISTA_ESPERA_PATH = '/lista-espera';

export const obtenerPacientes = async () => {
  const { data } = await httpClient.get(PACIENTES_PATH);
  return data;
};

export const registrarPaciente = async (paciente) => {
  const { data } = await httpClient.post(PACIENTES_PATH, paciente);
  return data;
};

export const agregarPacienteAListaEspera = async (
  pacienteId,
  { gravedad = 'MEDIA', interconsulta = null, estado = 'PENDIENTE' } = {},
) => {
  const { data } = await httpClient.post(LISTA_ESPERA_PATH, {
    paciente: { id: pacienteId },
    gravedad,
    interconsulta,
    estado,
  });
  return data;
};

export const eliminarPaciente = async (id) => {
  const { data } = await httpClient.delete(`${PACIENTES_PATH}/${id}`);
  return data;
};

export const obtenerListaEspera = async () => {
  const { data } = await httpClient.get(LISTA_ESPERA_PATH);
  return data;
};

export const eliminarDelListaEspera = async (id) => {
  const { data } = await httpClient.delete(`${LISTA_ESPERA_PATH}/${id}`);
  return data;
};

export const actualizarEstadoListaEspera = async (id, nuevoEstado) => {
  const { data } = await httpClient.put(`${LISTA_ESPERA_PATH}/${id}/estado/${nuevoEstado}`);
  return data;
};

export const obtenerPacientesPorEstado = async (estado) => {
  const { data } = await httpClient.get(`${LISTA_ESPERA_PATH}/estado/${estado}`);
  return data;
};

export const obtenerPacientesPorGravedad = async (gravedad) => {
  const { data } = await httpClient.get(`${LISTA_ESPERA_PATH}/gravedad/${gravedad}`);
  return data;
};

