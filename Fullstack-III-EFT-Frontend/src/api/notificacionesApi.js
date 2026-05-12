import axios from 'axios'

/** Facade para el microservicio de notificaciones (rutas en español). */
export const notificacionesApi = {
  listarPendientes: () => axios.get('/api/notificaciones/pendientes'),

  enviar: (id) => axios.post(`/api/notificaciones/${id}/enviar`),
}
