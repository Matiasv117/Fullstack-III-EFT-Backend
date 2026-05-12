import { useState, useEffect, useCallback } from 'react'
import { notificacionesApi } from '../api/notificacionesApi'

function Notificaciones() {
  const [notificaciones, setNotificaciones] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [mensaje, setMensaje] = useState(null)

  const cargar = useCallback(async () => {
    setError(null)
    setMensaje(null)
    setLoading(true)
    try {
      const { data } = await notificacionesApi.listarPendientes()
      setNotificaciones(data)
    } catch (e) {
      setError('No se pudieron cargar las notificaciones pendientes.')
      console.error(e)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    cargar()
  }, [cargar])

  const enviarNotificacion = async (id) => {
    setError(null)
    setMensaje(null)
    try {
      await notificacionesApi.enviar(id)
      setMensaje('Notificación marcada como enviada.')
      setNotificaciones((prev) => prev.filter((n) => n.id !== id))
    } catch (e) {
      setError('No se pudo enviar la notificación.')
      console.error(e)
    }
  }

  return (
    <div className="salud-panel">
      <h2 className="salud-panel__title">Notificaciones pendientes</h2>
      <p className="salud-lead">Avisos generados por el sistema (canales simulados en esta versión).</p>

      {error && (
        <div className="salud-alert salud-alert--error" role="alert">
          {error}
        </div>
      )}
      {mensaje && (
        <div className="salud-alert salud-alert--ok" role="status">
          {mensaje}
        </div>
      )}

      {loading ? (
        <p className="salud-muted">Cargando…</p>
      ) : notificaciones.length === 0 ? (
        <p className="salud-muted">No hay notificaciones pendientes.</p>
      ) : (
        <ul className="salud-list">
          {notificaciones.map((n) => (
            <li key={n.id} className="salud-list__item">
              <div>
                <strong>ID {n.id}</strong>
                <span className="salud-muted"> — {n.mensaje}</span>
              </div>
              <button type="button" className="salud-btn salud-btn--secondary" onClick={() => enviarNotificacion(n.id)}>
                Marcar enviada
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default Notificaciones
