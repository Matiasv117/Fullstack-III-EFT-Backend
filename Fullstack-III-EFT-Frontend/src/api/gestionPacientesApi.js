import axios from 'axios'

/**
 * Facade: un solo lugar con las rutas del gateway para pacientes y lista de espera.
 */
export const gestionPacientesApi = {
  listarPacientes: () => axios.get('/pacientes'),

  registrarPaciente: (payload) => axios.post('/pacientes', payload),

  /** payload: { paciente: { id }, gravedad, interconsulta } */
  agregarAListaEspera: (payload) => axios.post('/lista-espera', payload),
}
