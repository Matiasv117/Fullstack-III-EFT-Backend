import { useState, useEffect } from 'react'
import { obtenerTodasCitas, cancelarCita } from '../api/citasApi'
import { obtenerPacientes } from '../api/gestionPacientesApi'

const GestionCitas = () => {
  const [citas, setCitas] = useState([])
  const [loading, setLoading] = useState(true)
  const [filtroEstado, setFiltroEstado] = useState('TODAS')
  const [mensaje, setMensaje] = useState('')
  const [pacientesMap, setPacientesMap] = useState({})

  useEffect(() => {
    Promise.all([
      obtenerTodasCitas(),
      obtenerPacientes().catch(() => []),
    ])
      .then(([citasData, pacientesData]) => {
        setCitas(Array.isArray(citasData) ? citasData : [])
        const map = {}
        if (Array.isArray(pacientesData)) {
          pacientesData.forEach(p => { map[p.id] = p })
        }
        setPacientesMap(map)
      })
      .catch(() => setCitas([]))
      .finally(() => setLoading(false))
  }, [])

  const handleCancelar = async (id) => {
    if (!confirm('¿Estás seguro de cancelar esta cita?')) return
    try {
      await cancelarCita(id)
      setMensaje('Cita cancelada exitosamente')
      setCitas((prev) => prev.filter((c) => c.id !== id))
    } catch {
      setMensaje('Error al cancelar la cita')
    }
    setTimeout(() => setMensaje(''), 3000)
  }

  const citasFiltradas = filtroEstado === 'TODAS'
    ? citas
    : citas.filter((c) => c.estado === filtroEstado)

  const estadoColor = (estado) => {
    switch (estado) {
      case 'CONFIRMADA': return 'bg-primary-fixed text-primary border-primary-fixed'
      case 'CANCELADA': return 'bg-error-container text-error border-error-container'
      case 'REASIGNADA': return 'bg-tertiary-container text-tertiary border-tertiary-container'
      default: return 'bg-surface-container-high text-on-surface-variant border-surface-container-high'
    }
  }

  if (loading) {
    return (
      <div className="max-w-[1200px] mx-auto">
        <p className="text-on-surface-variant">Cargando citas...</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <header className="flex items-center justify-between">
          <h1 className="text-2xl font-bold">Gestión de Citas</h1>
          <select
            value={filtroEstado}
            onChange={(e) => setFiltroEstado(e.target.value)}
            className="p-2 rounded-lg border border-outline-variant bg-surface-container-low text-sm"
          >
            <option value="TODAS">Todas</option>
            <option value="CONFIRMADA">Confirmadas</option>
            <option value="CANCELADA">Canceladas</option>
            <option value="REASIGNADA">Reasignadas</option>
          </select>
        </header>

        {mensaje && (
          <p className={`p-3 rounded-lg ${mensaje.includes('Error') ? 'bg-error-container text-error' : 'bg-primary-fixed text-primary'}`}>
            {mensaje}
          </p>
        )}

        {citasFiltradas.length === 0 ? (
          <div className="py-12 text-center text-on-surface-variant border border-dashed border-outline-variant rounded-xl bg-surface-container-low">
            <p className="font-semibold">No hay citas registradas</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-4">
            {citasFiltradas.map((cita) => (
              <div key={cita.id} className="bg-surface-container-lowest p-5 rounded-xl border border-outline-variant flex items-center justify-between gap-4">
                <div className="flex-1 grid grid-cols-4 gap-4">
                  <div>
                    <p className="text-xs text-on-surface-variant">Paciente</p>
                    <p className="font-medium">{pacientesMap[cita.pacienteId] ? `${pacientesMap[cita.pacienteId].nombre} ${pacientesMap[cita.pacienteId].apellido}` : `#${cita.pacienteId}`}</p>
                  </div>
                  <div>
                    <p className="text-xs text-on-surface-variant">Médico</p>
                    <p className="font-medium">{cita.medico?.nombre ?? '—'}</p>
                  </div>
                  <div>
                    <p className="text-xs text-on-surface-variant">Fecha y Hora</p>
                    <p className="font-medium">{cita.fechaHora ? new Date(cita.fechaHora).toLocaleString('es-CL') : '—'}</p>
                  </div>
                  <div>
                    <p className="text-xs text-on-surface-variant">Estado</p>
                    <span className={`inline-block px-3 py-1 rounded-full text-xs font-bold border ${estadoColor(cita.estado)}`}>
                      {cita.estado}
                    </span>
                  </div>
                </div>
                {cita.estado === 'CONFIRMADA' && (
                  <button
                    onClick={() => handleCancelar(cita.id)}
                    className="px-4 py-2 bg-error-container text-error rounded-lg hover:bg-error hover:text-white text-sm font-semibold transition-colors"
                  >
                    Cancelar
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
    </div>
  )
}

export default GestionCitas
