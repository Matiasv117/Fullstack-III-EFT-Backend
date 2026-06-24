import axios from 'axios';

const httpClient = axios.create({
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
    const apiMessage = error?.response?.data?.error || error?.response?.data?.message;
    const statusCode = error?.response?.status;
    
    // Si el backend ya envía un mensaje de error amigable, usarlo
    if (apiMessage) {
      return Promise.reject(new Error(apiMessage));
    }
    
    // Si no hay mensaje del backend, generar uno amigable según el código de estado
    let userMessage = 'Error inesperado al procesar tu solicitud';
    
    switch (statusCode) {
      case 400:
        userMessage = 'Los datos ingresados no son válidos. Por favor, verifica la información.';
        break;
      case 401:
        userMessage = 'Credenciales incorrectas. Por favor, verifica tu usuario y contraseña.';
        break;
      case 403:
        userMessage = 'No tienes permisos para realizar esta acción.';
        break;
      case 404:
        userMessage = 'El recurso solicitado no fue encontrado.';
        break;
      case 500:
        userMessage = 'Error del servidor. Por favor, intenta nuevamente más tarde.';
        break;
      case 503:
        userMessage = 'El servicio no está disponible temporalmente. Por favor, intenta más tarde.';
        break;
      default:
        userMessage = error?.message || 'Error inesperado al consumir la API';
    }
    
    return Promise.reject(new Error(userMessage));
  },
);

export default httpClient;

