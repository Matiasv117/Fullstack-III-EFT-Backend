import httpClient from './httpClient';

const API_BASE_URL = 'http://localhost:8080/api/auth';

export const authApi = {
  /**
   * Inicia sesión con las credenciales proporcionadas
   * @param {string} username - Nombre de usuario
   * @param {string} password - Contraseña
   * @returns {Promise} Respuesta con token JWT y datos del usuario
   */
  login: async (username, password) => {
    const response = await httpClient.post(`${API_BASE_URL}/login`, {
      username,
      password,
    });
    return response.data;
  },

  /**
   * Inicia sesión como paciente usando datos personales
   * @param {string} nombre - Nombre del paciente
   * @param {string} apellido - Apellido del paciente
   * @param {string} rut - RUT del paciente
   * @returns {Promise} Respuesta con token JWT y datos del paciente
   */
  loginPaciente: async (nombre, apellido, rut) => {
    const response = await httpClient.post(`${API_BASE_URL}/login-paciente`, {
      nombre,
      apellido,
      rut,
    });
    return response.data;
  },

  /**
   * Valida un token JWT
   * @param {string} token - Token JWT a validar
   * @returns {Promise} Respuesta indicando si el token es válido
   */
  validateToken: async (token) => {
    const response = await httpClient.post(
      `${API_BASE_URL}/validate`,
      {},
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
    return response.data;
  },

  /**
   * Obtiene información del usuario autenticado
   * @param {string} token - Token JWT
   * @returns {Promise} Datos del usuario autenticado
   */
  getMe: async (token) => {
    const response = await httpClient.get(`${API_BASE_URL}/me`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  },
};

export default authApi;
