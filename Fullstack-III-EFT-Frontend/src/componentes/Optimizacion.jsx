import axios from 'axios'
import { useState, useEffect } from 'react'

function Optimizacion() {
  const [listaEspera, setListaEspera] = useState([])

  useEffect(() => {
    const fetchLista = async () => {
      try {
        const response = await axios.get('/optimizacion/lista-espera')
        setListaEspera(response.data)
      } catch (error) {
        console.error('Error al obtener lista de espera:', error)
      }
    }
    fetchLista()
  }, [])

  return (
    <div className="salud-panel">
      <h2 className="salud-panel__title">Optimización de lista de espera</h2>
      <p className="salud-lead">Vista desde el microservicio de optimización (datos de apoyo).</p>

      {listaEspera.length === 0 ? (
        <p className="salud-muted">No hay pacientes en la lista de espera.</p>
      ) : (
        <ul className="salud-list">
          {listaEspera.map((item) => (
            <li key={item.id} className="salud-list__item">
              <div>
                <strong>Paciente:</strong> {item.pacienteNombre || 'N/A'}
                <br />
                <strong>Prioridad:</strong>{' '}
                <span className="salud-pill">{item.prioridad || 'NORMAL'}</span>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default Optimizacion
