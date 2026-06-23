import axios from 'axios';

const httpClient = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000,
});

// Interceptor para adjuntar el token JWT en cada petición
httpClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const apiMessage = error?.response?.data?.message || error?.response?.data?.error;
    const fallbackMessage = error?.message || 'Error inesperado al consumir la API';
    return Promise.reject(new Error(apiMessage || fallbackMessage));
  },
);

export default httpClient;

