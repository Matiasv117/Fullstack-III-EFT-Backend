import { useState, useEffect } from 'react'
import { obtenerResumenPortal } from '../api/portalApi'
import reportesApi from '../api/reportesApi'
import { obtenerPacientes } from '../api/gestionPacientesApi'
import { obtenerNotificacionesPendientes } from '../api/notificacionesApi'
import { obtenerMisDatos, obtenerMiPosicion, obtenerMisCitas, obtenerNotificacionesPorPaciente } from '../api/pacienteApi'

const Dashboard = ({ user, onSectionChange }) => {
  const isPaciente = user?.role === 'ROLE_PACIENTE';
  const [resumen, setResumen] = useState(null)
  const [metricas, setMetricas] = useState(null)
  const [ultimosPacientes, setUltimosPacientes] = useState([])
  const [notificaciones, setNotificaciones] = useState([])
  const [loading, setLoading] = useState(true)
  const [pacienteDatos, setPacienteDatos] = useState(null)
  const [pacientePosicion, setPacientePosicion] = useState(null)
  const [pacienteCitas, setPacienteCitas] = useState([])
  const [pacienteNotifs, setPacienteNotifs] = useState([])
  const [expandNotifs, setExpandNotifs] = useState(false)

  useEffect(() => {
    if (isPaciente) {
      Promise.all([
        obtenerMisDatos(),
        obtenerMiPosicion().catch(() => null),
        obtenerMisCitas().catch(() => []),
      ])
        .then(([datos, posicion, citas]) => {
          setPacienteDatos(datos)
          setPacientePosicion(posicion)
          setPacienteCitas(Array.isArray(citas) ? citas : [])
          if (datos?.id) {
            obtenerNotificacionesPorPaciente(datos.id)
              .then((notifs) => setPacienteNotifs(Array.isArray(notifs) ? notifs : []))
              .catch(() => {})
          }
        })
        .catch(() => {})
        .finally(() => setLoading(false))
    } else {
      Promise.all([
        obtenerResumenPortal(),
        reportesApi.obtenerMetricasListaEspera(),
        obtenerPacientes(),
        obtenerNotificacionesPendientes(),
      ])
        .then(([res, met, pac, notif]) => {
          setResumen(res?.resumen ?? null)
          setMetricas(met ?? null)
          const sorted = Array.isArray(pac) ? pac.slice(-5).reverse() : []
          setUltimosPacientes(sorted)
          setNotificaciones(Array.isArray(notif) ? notif.slice(0, 4) : [])
        })
        .catch(() => {
          setResumen(null)
          setMetricas(null)
        })
        .finally(() => setLoading(false))
    }
  }, [isPaciente])

  if (isPaciente) {
    const proximaCita = pacienteCitas.find((c) => c.estado === 'CONFIRMADA')
    const badgeEstado = pacientePosicion ? 'EN PROCESO' : 'NO REGISTRADO'
    const badgeColor = pacientePosicion ? 'bg-tertiary-container text-on-tertiary-container' : 'bg-surface-container text-on-surface-variant'

    return (
      <main className="ml-[260px] pt-24 p-gutter min-h-screen">
        <div className="max-w-[1400px] mx-auto flex flex-col gap-gutter">
          
          {/* Hero Banner del Paciente */}
          <section className="fade-in-up stagger-1 relative overflow-hidden rounded-xl bg-primary-fixed-dim min-h-[200px] flex items-center">
            <div className="absolute inset-0 bg-gradient-to-br from-blue-600 to-[#00174b] opacity-95"></div>
            <div className="relative z-10 px-12 py-10 flex justify-between w-full items-center">
              <div className="max-w-2xl">
                <span className="inline-block px-3 py-1 bg-white/10 text-white rounded-full font-label-bold text-label-bold mb-4 backdrop-blur-md">
                  PORTAL DEL PACIENTE
                </span>
                <h2 className="font-display-hero text-display-hero text-white mb-2">
                  Bienvenido{ pacienteDatos ? `, ${pacienteDatos.nombre}` : ' a tu Portal de Salud' }
                </h2>
                <p className="font-body-lg text-body-lg text-white/80">
                  Aquí puedes revisar el estado de tu ficha, tus citas médicas programadas y tu situación en la lista de espera.
                </p>
              </div>
            </div>
          </section>

          {/* Grid de Contenido Bento del Paciente */}
          <section className="grid grid-cols-1 md:grid-cols-3 gap-gutter fade-in-up stagger-3">
            
            {/* Card Cita */}
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant hover:border-primary/50 transition-all duration-500 group cursor-pointer hover:scale-105">
              <div className="flex justify-between items-start mb-4">
                <div className="w-12 h-12 bg-primary-fixed rounded-lg flex items-center justify-center text-primary group-hover:bg-primary group-hover:text-white transition-all duration-500">
                  <span className="material-symbols-outlined">event_available</span>
                </div>
                <span className={`px-3 py-1 rounded-full font-label-bold text-[11px] font-bold ${proximaCita ? 'bg-primary/10 text-primary' : 'bg-surface-container text-on-surface-variant'}`}>
                  {proximaCita ? proximaCita.estado : 'SIN CITA'}
                </span>
              </div>
              <h3 className="text-on-surface-variant font-label-bold mb-1">Próxima Cita Médica</h3>
              {proximaCita ? (
                <div className="flex flex-col gap-1 mt-2">
                  <span className="font-headline-md text-on-surface text-xl font-bold">{proximaCita.medico?.nombre ?? 'Consulta'}</span>
                  <span className="text-on-surface-variant text-body-md">{new Date(proximaCita.fechaHora).toLocaleString('es-CL')}</span>
                </div>
              ) : (
                <div className="flex flex-col gap-1 mt-2">
                  <span className="text-on-surface-variant text-body-md">No tienes citas programadas.</span>
                </div>
              )}
            </div>

            {/* Card Lista de Espera */}
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant hover:border-tertiary/50 transition-all duration-500 group cursor-pointer hover:scale-105">
              <div className="flex justify-between items-start mb-4">
                <div className="w-12 h-12 bg-tertiary-fixed rounded-lg flex items-center justify-center text-tertiary group-hover:bg-tertiary group-hover:text-white transition-all duration-500">
                  <span className="material-symbols-outlined">list_alt</span>
                </div>
                <span className={`px-3 py-1 rounded-full font-label-bold text-[11px] font-bold ${badgeColor}`}>{badgeEstado}</span>
              </div>
              <h3 className="text-on-surface-variant font-label-bold mb-1">Tu Estado en Lista de Espera</h3>
              {pacientePosicion ? (
                <div className="flex flex-col gap-1 mt-2">
                  <span className="font-headline-md text-on-surface text-xl font-bold">Posición {pacientePosicion.posicion} de {pacientePosicion.total}</span>
                  <span className="text-on-surface-variant text-body-md">Gravedad: {pacientePosicion.registro?.gravedad ?? 'Media'}</span>
                </div>
              ) : (
                <div className="flex flex-col gap-1 mt-2">
                  <span className="text-on-surface-variant text-body-md">No estás en la lista de espera.</span>
                </div>
              )}
            </div>

            {/* Card Notificaciones — Expandable Mini-Feed */}
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant transition-all duration-500 group">
              <div className="flex justify-between items-start mb-4">
                <div className="w-12 h-12 bg-secondary-container rounded-lg flex items-center justify-center text-primary transition-all duration-500">
                  <span className="material-symbols-outlined">notifications_active</span>
                </div>
                <span className={`px-2 py-0.5 rounded font-label-bold text-[10px] font-bold ${pacienteNotifs.length > 0 ? 'bg-green-100 text-green-800' : 'bg-surface-container text-on-surface-variant'}`}>
                  {pacienteNotifs.length > 0 ? pacienteNotifs.length + ' NUEVOS' : 'VACÍO'}
                </span>
              </div>
              <div className="flex items-center justify-between mb-1">
                <h3 className="text-on-surface-variant font-label-bold">Mensajes Recientes</h3>
                {pacienteNotifs.length > 1 && (
                  <button
                    onClick={() => setExpandNotifs(!expandNotifs)}
                    className="text-primary text-xs font-semibold hover:underline cursor-pointer flex items-center gap-1"
                  >
                    {expandNotifs ? 'Ver menos' : `Ver ${pacienteNotifs.length}`}
                    <span className={`material-symbols-outlined text-sm transition-transform duration-300 ${expandNotifs ? 'rotate-180' : ''}`}>
                      expand_more
                    </span>
                  </button>
                )}
              </div>
              <div className="overflow-hidden transition-all duration-500 ease-in-out" style={{ maxHeight: expandNotifs ? `${pacienteNotifs.length * 80}px` : '80px' }}>
                {pacienteNotifs.length > 0 ? (
                  <ul className="space-y-2 mt-2">
                    {pacienteNotifs.map((n, i) => (
                      <li
                        key={n.id || i}
                        className={`flex items-start gap-3 p-3 rounded-lg transition-all duration-300 ${
                          i === 0 && !expandNotifs
                            ? ''
                            : 'bg-surface-container-low hover:bg-surface-container-high'
                        }`}
                        style={{
                          animation: expandNotifs ? `fadeIn 0.3s ease ${i * 0.05}s both` : undefined,
                        }}
                      >
                        <div className="w-7 h-7 rounded-full bg-secondary-container flex items-center justify-center text-primary shrink-0 mt-0.5">
                          <span className="material-symbols-outlined text-sm">mail</span>
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="font-bold text-on-surface text-xs">{n.tipo?.replace(/_/g, ' ') || 'Notificación'}</p>
                          <p className="text-on-surface-variant text-[11px] leading-tight line-clamp-2 mt-0.5">{n.mensaje}</p>
                        </div>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <div className="flex flex-col gap-1 mt-2">
                    <span className="text-on-surface-variant text-body-md">No tienes notificaciones recientes.</span>
                  </div>
                )}
              </div>
            </div>

          </section>

        </div>
      </main>
    );
  }

  return (
    <main className="ml-[260px] pt-24 p-gutter min-h-screen">
      <div className="max-w-[1400px] mx-auto flex flex-col gap-gutter">
        
        {/* Hero Banner */}
        <section className="fade-in-up stagger-1 relative overflow-hidden rounded-xl bg-primary-fixed-dim min-h-[240px] flex items-center">
          <div className="absolute inset-0 bg-gradient-to-br from-primary to-[#00174b] opacity-95"></div>
          <div className="relative z-10 px-12 py-10 flex justify-between w-full items-center">
            <div className="max-w-2xl">
              <span className="inline-block px-3 py-1 bg-white/10 text-white rounded-full font-label-bold text-label-bold mb-4 backdrop-blur-md">
                SISTEMA DE SALUD PÚBLICA
              </span>
              <h2 className="font-display-hero text-display-hero text-white mb-2">
                Bienvenido, {user?.username || 'Dr. Benjamín Ibañez'}
              </h2>
              <p className="font-body-lg text-body-lg text-white/80">
                {resumen
                  ? `Resumen del sistema — ${resumen.totalPacientes} pacientes registrados, ${
                      metricas?.totalPendientes ?? 0
                    } en lista de espera, ${resumen.totalNotificacionesPendientes} notificaciones pendientes.`
                  : loading
                    ? 'Cargando indicadores del sistema...'
                    : 'Bienvenido al sistema de salud.'}
              </p>
            </div>
            <div className="hidden lg:block">
              <div
                className="bg-white/10 backdrop-blur-xl p-6 rounded-2xl border border-white/20 hover:scale-105 transition-transform duration-300 cursor-pointer"
                onClick={() => onSectionChange?.('pacientes')}
              >
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 bg-primary-container/40 rounded-full flex items-center justify-center">
                    <span className="material-symbols-outlined text-white">bolt</span>
                  </div>
                  <div>
                    <p className="text-white/60 font-label-bold text-label-bold">ACCESO RÁPIDO</p>
                    <p className="text-white font-headline-md">Nueva Consulta</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="absolute right-0 top-0 h-full w-1/3 opacity-20 pointer-events-none overflow-hidden">
            <img
              alt="Scientific glass and medical laboratory aesthetics"
              className="object-cover h-full w-full"
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuBx5bunCgizfuKi2mLIC8Ret6blT5VnDaiv2WmZZvU7KfAMHork2ZXz0BiaqBxHbsdZabAn5Noty3kD2mfXkMPA95r2YBH8v2FyzSebMCFB2bSB8lXHJeW75q3M-yrr9OasoeAT9ijfDFAK05xO3m6w3Cl4PmM0LPbU4qx_HuH2djBI1WlV1GMEDovP2hgwTGF-vuiKTeu38rQmyKtWYL9b9O3aCTwGkezoxulDjeDDw5TWuM0ygljHuMjPDumweP9k8IT4OuQFfFE7"
            />
          </div>
        </section>

        {/* Quick Stats Row */}
        {!loading && (
          <section className="grid grid-cols-2 md:grid-cols-4 gap-gutter fade-in-up stagger-2">
            <div className="bg-surface-container-lowest p-4 rounded-xl border border-outline-variant text-center">
              <span className="material-symbols-outlined text-primary text-2xl">people</span>
              <p className="font-display-hero text-display-hero text-on-surface mt-1">{resumen?.totalPacientes ?? 0}</p>
              <p className="text-on-surface-variant font-label-bold text-label-bold">Total Pacientes</p>
            </div>
            <div className="bg-surface-container-lowest p-4 rounded-xl border border-outline-variant text-center">
              <span className="material-symbols-outlined text-tertiary text-2xl">list_alt</span>
              <p className="font-display-hero text-display-hero text-on-surface mt-1">{metricas?.totalPendientes ?? 0}</p>
              <p className="text-on-surface-variant font-label-bold text-label-bold">En Lista de Espera</p>
            </div>
            <div className="bg-surface-container-lowest p-4 rounded-xl border border-outline-variant text-center">
              <span className="material-symbols-outlined text-error text-2xl">priority_high</span>
              <p className="font-display-hero text-display-hero text-on-surface mt-1">{metricas?.pacientesGravedadAlta ?? 0}</p>
              <p className="text-on-surface-variant font-label-bold text-label-bold">Prioridad ALTA</p>
            </div>
            <div className="bg-surface-container-lowest p-4 rounded-xl border border-outline-variant text-center">
              <span className="material-symbols-outlined text-secondary text-2xl">notifications</span>
              <p className="font-display-hero text-display-hero text-on-surface mt-1">{resumen?.totalNotificacionesPendientes ?? 0}</p>
              <p className="text-on-surface-variant font-label-bold text-label-bold">Notificaciones Pendientes</p>
            </div>
          </section>
        )}

        {/* Content Cards Grid */}
        {!loading && (
          <section className="grid grid-cols-1 md:grid-cols-3 gap-gutter fade-in-up stagger-3">

            {/* Lista de Espera por Gravedad */}
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant col-span-1">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 bg-tertiary-fixed rounded-lg flex items-center justify-center text-tertiary">
                  <span className="material-symbols-outlined">monitoring</span>
                </div>
                <h3 className="font-label-bold text-on-surface">Lista de Espera por Gravedad</h3>
              </div>
              <div className="space-y-4">
                <div>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-on-surface-variant font-label-bold">ALTA</span>
                    <span className="font-label-bold text-error">{metricas?.pacientesGravedadAlta ?? 0}</span>
                  </div>
                  <div className="h-2 w-full bg-surface-container rounded-full overflow-hidden">
                    <div className="h-full bg-error rounded-full transition-all duration-700" style={{ width: `${metricas?.totalPendientes ? (metricas.pacientesGravedadAlta / metricas.totalPendientes) * 100 : 0}%` }}></div>
                  </div>
                </div>
                <div>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-on-surface-variant font-label-bold">MEDIA</span>
                    <span className="font-label-bold text-primary">{metricas?.pacientesGravedadMedia ?? 0}</span>
                  </div>
                  <div className="h-2 w-full bg-surface-container rounded-full overflow-hidden">
                    <div className="h-full bg-primary rounded-full transition-all duration-700" style={{ width: `${metricas?.totalPendientes ? (metricas.pacientesGravedadMedia / metricas.totalPendientes) * 100 : 0}%` }}></div>
                  </div>
                </div>
                <div>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-on-surface-variant font-label-bold">BAJA</span>
                    <span className="font-label-bold text-tertiary">{metricas?.pacientesGravedadBaja ?? 0}</span>
                  </div>
                  <div className="h-2 w-full bg-surface-container rounded-full overflow-hidden">
                    <div className="h-full bg-tertiary rounded-full transition-all duration-700" style={{ width: `${metricas?.totalPendientes ? (metricas.pacientesGravedadBaja / metricas.totalPendientes) * 100 : 0}%` }}></div>
                  </div>
                </div>
              </div>
            </div>

            {/* Últimos Pacientes Registrados */}
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant col-span-1">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 bg-secondary-container rounded-lg flex items-center justify-center text-primary">
                  <span className="material-symbols-outlined">person_add</span>
                </div>
                <h3 className="font-label-bold text-on-surface">Últimos Pacientes Registrados</h3>
              </div>
              {ultimosPacientes.length === 0 ? (
                <p className="text-on-surface-variant text-body-md">No hay pacientes registrados.</p>
              ) : (
                <ul className="space-y-3">
                  {ultimosPacientes.map((p, i) => (
                    <li key={p.id ?? i} className="flex items-center gap-3 pb-3 border-b border-outline-variant/50 last:border-b-0 last:pb-0">
                      <div className="w-9 h-9 rounded-full bg-primary-fixed flex items-center justify-center text-primary font-label-bold text-sm">
                        {p.nombre?.[0]}{p.apellido?.[0]}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="font-label-bold text-on-surface truncate">{p.nombre} {p.apellido}</p>
                        <p className="text-on-surface-variant text-body-sm">{p.dni ?? 'Sin RUT'}</p>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            {/* Notificaciones Recientes */}
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant col-span-1">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 bg-error-container rounded-lg flex items-center justify-center text-error">
                  <span className="material-symbols-outlined">notification_important</span>
                </div>
                <h3 className="font-label-bold text-on-surface">Notificaciones Recientes</h3>
              </div>
              {notificaciones.length === 0 ? (
                <p className="text-on-surface-variant text-body-md">No hay notificaciones pendientes.</p>
              ) : (
                <ul className="space-y-3">
                  {notificaciones.map((n, i) => (
                    <li key={n.id ?? i} className="flex items-start gap-3 pb-3 border-b border-outline-variant/50 last:border-b-0 last:pb-0">
                      <div className="w-8 h-8 rounded-full bg-secondary-container flex items-center justify-center text-primary shrink-0 mt-0.5">
                        <span className="material-symbols-outlined text-sm">mail</span>
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="font-label-bold text-on-surface text-sm truncate">{n.tipo?.replace(/_/g, ' ')}</p>
                        <p className="text-on-surface-variant text-body-sm leading-tight line-clamp-2">{n.mensaje}</p>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>

          </section>
        )}

      </div>
    </main>
  )
}

export default Dashboard
