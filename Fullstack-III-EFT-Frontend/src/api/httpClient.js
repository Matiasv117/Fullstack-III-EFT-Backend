import axios from 'axios';

const httpClient = axios.create({
  timeout: 15000,
});

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const apiMessage = error?.response?.data?.message || error?.response?.data?.error;
    const fallbackMessage = error?.message || 'Error inesperado al consumir la API';
    return Promise.reject(new Error(apiMessage || fallbackMessage));
  },
);

export default httpClient;

