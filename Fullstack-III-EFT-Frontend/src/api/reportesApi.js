import httpClient from './httpClient';

const reportesApi = {
  obtenerMetricasListaEspera: async () => {
    const response = await httpClient.get('/api/lista-espera/metricas');
    return response.data;
  },

  listarPacientes: async () => {
    const response = await httpClient.get('/api/pacientes');
    return response.data;
  },

  listarEventosAuditoria: async () => {
    const response = await httpClient.get('/api/auditoria/eventos');
    return response.data;
  },
};

export default reportesApi;
