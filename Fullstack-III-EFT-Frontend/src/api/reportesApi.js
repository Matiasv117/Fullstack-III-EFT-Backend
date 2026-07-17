import httpClient from './httpClient';

const reportesApi = {
  obtenerMetricasListaEspera: async () => {
    try {
      const response = await httpClient.get('/api/lista-espera/metricas');
      return response.data;
    } catch (err) {
      throw new Error(err.message || 'Error al obtener métricas de lista de espera');
    }
  },

  listarPacientes: async () => {
    try {
      const response = await httpClient.get('/api/pacientes');
      return response.data;
    } catch (err) {
      throw new Error(err.message || 'Error al listar pacientes');
    }
  },

  listarEventosAuditoria: async () => {
    try {
      const response = await httpClient.get('/api/auditoria/eventos');
      return response.data;
    } catch {
      return [];
    }
  },
};

export default reportesApi;
