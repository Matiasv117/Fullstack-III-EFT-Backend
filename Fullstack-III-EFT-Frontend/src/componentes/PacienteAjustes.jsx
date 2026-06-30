import { useState, useEffect } from 'react'
import { obtenerMisDatos, obtenerMisCitas, obtenerMedicos, crearCitaMedica, cancelarCitaPaciente, actualizarMisDatos } from '../api/pacienteApi'

const PacienteAjustes = ({ user, section }) => {
  const [datos, setDatos] = useState(null)
  const [citas, setCitas] = useState([])
  const [medicos, setMedicos] = useState([])
  const [medicoId, setMedicoId] = useState('')
  const [fechaHora, setFechaHora] = useState('')
  const [loading, setLoading] = useState(true)
  const [mensaje, setMensaje] = useState('')
  const [editando, setEditando] = useState(false)
  const [editForm, setEditForm] = useState({ nombre: '', apellido: '', telefono: '', email: '' })

  const handleCancelarCita = async (id) => {
    if (!confirm('¿Estás seguro de cancelar esta cita?')) return
    try {
      await cancelarCitaPaciente(id)
      setMensaje('Cita cancelada exitosamente')
      setCitas((prev) => prev.filter((c) => c.id !== id))
    } catch {
      setMensaje('Error al cancelar la cita')
    }
    setTimeout(() => setMensaje(''), 3000)
  }

  useEffect(() => {
    Promise.all([
      obtenerMisDatos().catch(() => null),
      obtenerMisCitas().catch(() => []),
      obtenerMedicos().catch(() => []),
    ])
      .then(([datosRes, citasRes, medicosRes]) => {
        setDatos(datosRes)
        if (datosRes) {
          setEditForm({
            nombre: datosRes.nombre || '',
            apellido: datosRes.apellido || '',
            telefono: datosRes.telefono || '',
            email: datosRes.email || '',
          })
        }
        setCitas(Array.isArray(citasRes) ? citasRes : [])
        setMedicos(Array.isArray(medicosRes) ? medicosRes : [])
      })
      .finally(() => setLoading(false))
  }, [])

  const handleGuardarPerfil = async () => {
    try {
      const updated = await actualizarMisDatos({ ...datos, ...editForm })
      setDatos(updated)
      setEditando(false)
      setMensaje('Datos actualizados exitosamente')
    } catch {
      setMensaje('Error al actualizar datos')
    }
    setTimeout(() => setMensaje(''), 3000)
  }

  const handleCancelarEdicion = () => {
    setEditForm({
      nombre: datos?.nombre || '',
      apellido: datos?.apellido || '',
      telefono: datos?.telefono || '',
      email: datos?.email || '',
    })
    setEditando(false)
  }

  const handleAgendar = async (e) => {
    e.preventDefault()
    setMensaje('')
    try {
      await crearCitaMedica({
        pacienteId: datos?.id,
        medico: { id: Number(medicoId) },
        fechaHora: new Date(fechaHora).toISOString(),
      })
      setMensaje('Cita agendada exitosamente')
      setMedicoId('')
      setFechaHora('')
      const citasRes = await obtenerMisCitas()
      setCitas(Array.isArray(citasRes) ? citasRes : [])
    } catch {
      setMensaje('Error al agendar cita')
    }
  }

  if (loading) {
    return (
      <main className="ml-[260px] pt-24 p-gutter min-h-screen">
        <div className="max-w-[1000px] mx-auto">
          <p className="text-on-surface-variant">Cargando...</p>
        </div>
      </main>
    )
  }

  const ahora = new Date()
  const citasFuturas = citas.filter(c => new Date(c.fechaHora) >= ahora)
  const citasPasadas = citas.filter(c => new Date(c.fechaHora) < ahora)

  return (
    <main className="ml-[260px] pt-24 p-gutter min-h-screen">
      <div className="max-w-[1000px] mx-auto flex flex-col gap-gutter">
        {/* Perfil */}
        {(!section || section === 'perfil') && (
          <section id="perfil" className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-label-bold text-lg">Mi Perfil</h2>
              {!editando && (
                <button
                  onClick={() => setEditando(true)}
                  className="flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium text-primary hover:bg-primary/10 rounded-lg transition-colors"
                >
                  <span className="material-symbols-outlined text-base">edit</span>
                  Editar
                </button>
              )}
            </div>
            {mensaje && (
              <p className={`mb-4 p-3 rounded-lg ${mensaje.includes('Error') ? 'bg-error-container text-error' : 'bg-primary-fixed text-primary'}`}>
                {mensaje}
              </p>
            )}
            {datos && !editando && (
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-on-surface-variant text-sm">Nombre</p>
                  <p className="font-medium">{datos.nombre} {datos.apellido}</p>
                </div>
                <div>
                  <p className="text-on-surface-variant text-sm">RUT</p>
                  <p className="font-medium">{datos.dni ?? 'No registrado'}</p>
                </div>
                <div>
                  <p className="text-on-surface-variant text-sm">Teléfono</p>
                  <p className="font-medium">{datos.telefono ?? 'No registrado'}</p>
                </div>
                <div>
                  <p className="text-on-surface-variant text-sm">Email</p>
                  <p className="font-medium">{datos.email ?? 'No registrado'}</p>
                </div>
              </div>
            )}
            {editando && (
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-1">Nombre</label>
                  <input
                    value={editForm.nombre}
                    onChange={(e) => setEditForm(f => ({ ...f, nombre: e.target.value }))}
                    className="w-full p-2 rounded-lg border border-outline-variant bg-surface-container-low focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Apellido</label>
                  <input
                    value={editForm.apellido}
                    onChange={(e) => setEditForm(f => ({ ...f, apellido: e.target.value }))}
                    className="w-full p-2 rounded-lg border border-outline-variant bg-surface-container-low focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Teléfono</label>
                  <input
                    value={editForm.telefono}
                    onChange={(e) => setEditForm(f => ({ ...f, telefono: e.target.value }))}
                    className="w-full p-2 rounded-lg border border-outline-variant bg-surface-container-low focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Email</label>
                  <input
                    value={editForm.email}
                    onChange={(e) => setEditForm(f => ({ ...f, email: e.target.value }))}
                    className="w-full p-2 rounded-lg border border-outline-variant bg-surface-container-low focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                </div>
                <div className="col-span-2 flex gap-3 mt-2">
                  <button
                    onClick={handleGuardarPerfil}
                    className="px-6 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors text-sm"
                  >
                    Guardar
                  </button>
                  <button
                    onClick={handleCancelarEdicion}
                    className="px-6 py-2 bg-surface-container-high text-on-surface rounded-lg hover:bg-outline-variant transition-colors text-sm"
                  >
                    Cancelar
                  </button>
                </div>
              </div>
            )}
          </section>
        )}

        {/* Citas */}
        {(!section || section === 'citas') && (
          <>
            <section className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant">
              <h2 className="font-label-bold text-lg mb-4">Agendar Nueva Cita</h2>
              {mensaje && (
                <p className={`mb-4 p-3 rounded-lg ${mensaje.includes('Error') ? 'bg-error-container text-error' : 'bg-primary-fixed text-primary'}`}>
                  {mensaje}
                </p>
              )}
              <form onSubmit={handleAgendar} className="flex flex-col gap-4">
                <div>
                  <label className="block text-sm font-medium mb-1">Médico</label>
                  <select
                    value={medicoId}
                    onChange={(e) => setMedicoId(e.target.value)}
                    required
                    className="w-full p-2 rounded-lg border border-outline-variant bg-surface-container-low"
                  >
                    <option value="">Selecciona un médico</option>
                    {medicos.map((m) => (
                      <option key={m.id} value={m.id}>{m.nombre} - {m.especialidad}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Fecha y Hora</label>
                  <input
                    type="datetime-local"
                    value={fechaHora}
                    onChange={(e) => setFechaHora(e.target.value)}
                    required
                    className="w-full p-2 rounded-lg border border-outline-variant bg-surface-container-low"
                  />
                </div>
                <button
                  type="submit"
                  className="self-start px-6 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
                >
                  Agendar Cita
                </button>
              </form>
            </section>

            {/* Próximas Citas */}
            {citasFuturas.length > 0 && (
              <section className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant">
                <div className="flex items-center gap-2 mb-4">
                  <h2 className="font-label-bold text-lg">Próximas Citas</h2>
                  <span className="px-2 py-0.5 bg-primary-fixed text-primary rounded-full text-xs font-bold">{citasFuturas.length}</span>
                </div>
                <div className="flex flex-col gap-3">
                  {citasFuturas.map((cita) => (
                    <div key={cita.id} className="flex justify-between items-center p-4 bg-surface-container-low rounded-lg border border-outline-variant/50">
                      <div>
                        <p className="font-medium">{cita.medico?.nombre ?? 'Médico'}</p>
                        <p className="text-sm text-on-surface-variant">{new Date(cita.fechaHora).toLocaleString('es-CL')}</p>
                      </div>
                      <div className="flex items-center gap-3">
                        <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                          cita.estado === 'CONFIRMADA' ? 'bg-primary-fixed text-primary' : 'bg-error-container text-error'
                        }`}>
                          {cita.estado}
                        </span>
                        {cita.estado === 'CONFIRMADA' && (
                          <button
                            onClick={() => handleCancelarCita(cita.id)}
                            className="px-3 py-1 text-xs font-semibold bg-error-container text-error rounded-lg hover:bg-error hover:text-white transition-colors"
                          >
                            Cancelar
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </section>
            )}

            {/* Historial de Citas Pasadas */}
            {citasPasadas.length > 0 && (
              <section className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant">
                <div className="flex items-center gap-2 mb-4">
                  <h2 className="font-label-bold text-lg">Historial de Citas</h2>
                  <span className="px-2 py-0.5 bg-surface-container text-on-surface-variant rounded-full text-xs font-bold">{citasPasadas.length}</span>
                </div>
                <div className="flex flex-col gap-2">
                  {citasPasadas.map((cita) => (
                    <div key={cita.id} className="flex justify-between items-center p-3 bg-surface-container-low rounded-lg opacity-60 hover:opacity-100 transition-opacity">
                      <div>
                        <p className="font-medium text-sm">{cita.medico?.nombre ?? 'Médico'}</p>
                        <p className="text-xs text-on-surface-variant">{new Date(cita.fechaHora).toLocaleString('es-CL')}</p>
                      </div>
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-surface-container text-on-surface-variant">
                        {cita.estado}
                      </span>
                    </div>
                  ))}
                </div>
              </section>
            )}
          </>
        )}
      </div>
    </main>
  )
}

export default PacienteAjustes
