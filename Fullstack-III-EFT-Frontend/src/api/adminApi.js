import httpClient from './httpClient';

const adminApi = {
  listarFuncionarios: async () => {
    const response = await httpClient.get('/api/admin/funcionarios');
    return response.data;
  },

  crearFuncionario: async (username, password, nombreCompleto, email) => {
    const response = await httpClient.post('/api/admin/funcionarios', {
      username,
      password,
      nombreCompleto,
      email,
    });
    return response.data;
  },

  modificarFuncionario: async (id, username, password, nombreCompleto, email) => {
    const response = await httpClient.put(`/api/admin/funcionarios/${id}`, {
      username: username || undefined,
      password: password || undefined,
      nombreCompleto: nombreCompleto || undefined,
      email: email || undefined,
    });
    return response.data;
  },

  eliminarFuncionario: async (id) => {
    const response = await httpClient.delete(`/api/admin/funcionarios/${id}`);
    return response.data;
  },

  cambiarEstado: async (id, activo) => {
    const response = await httpClient.put(`/api/admin/funcionarios/${id}/estado`, {
      activo,
    });
    return response.data;
  },

  cambiarRol: async (id, rol) => {
    const response = await httpClient.put(`/api/admin/funcionarios/${id}/rol`, {
      rol,
    });
    return response.data;
  },
};

export default adminApi;
