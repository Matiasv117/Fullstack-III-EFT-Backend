import { useState, useEffect } from 'react'
import {
  Users, Calendar, ClipboardList, Bell, Activity,
  Settings, Search, RefreshCw, AlertTriangle, Check, Building2, BarChart3
} from 'lucide-react'
import './App.css'
import GestionPacientes from './componentes/GestionPacientes'
import ListaEspera from './componentes/ListaEspera'
import Notificaciones from './componentes/Notificaciones'
import Optimizacion from './componentes/Optimizacion'
import logo from './assets/rednorte.png'
import logosolo from './assets/logosolo.png'
import logo_usuario from './assets/userlogo.png'
import { obtenerResumenPortal } from './api/portalApi'

function App() {
  const [seccionActiva, setSeccionActiva] = useState('pacientes')
  const [resumenPortal, setResumenPortal] = useState(null)
  const [busqueda, setBusqueda] = useState('')
  const [isSyncing, setIsSyncing] = useState(false)
  const [syncErrorResolved, setSyncErrorResolved] = useState(false)

  const metricas = [
    {
      label: 'Pacientes registrados',
      valor: resumenPortal?.totalPacientes ?? 0,
      detalle: 'Pacientes visibles desde el BFF',
    },
    {
      label: 'Notificaciones pendientes',
      valor: resumenPortal?.totalNotificacionesPendientes ?? 0,
      detalle: 'Avisos que aún esperan despacho',
    },
  ]

  useEffect(() => {
    const cargarResumen = async () => {
      try {
        const datos = await obtenerResumenPortal()
        setResumenPortal(datos?.resumen ?? null)
      } catch {
        setResumenPortal(null)
      }
    }
    cargarResumen()
  }, [])

  const handleSyncRetry = () => {
    setIsSyncing(true)
    setTimeout(() => {
      setIsSyncing(false)
      setSyncErrorResolved(true)
    }, 1500)
  }

  const menuItems = [
    { id: 'pacientes', label: 'Gestión de Pacientes', icon: <Users className="w-5 h-5 shrink-0" /> },
    { id: 'listaespera', label: 'Lista de Espera', icon: <ClipboardList className="w-5 h-5 shrink-0" /> },
    { id: 'notificaciones', label: 'Notificaciones', icon: <Bell className="w-5 h-5 shrink-0" /> },
    { id: 'optimizacion', label: 'Optimización', icon: <Activity className="w-5 h-5 shrink-0" /> },
  ]

  return (
    <div className="flex flex-col min-h-screen text-slate-800 bg-[#f8fafc]">
      <div className="flex flex-col lg:flex-row min-h-screen">

        {/* Sidebar Lateral */}
        <aside className="w-full lg:w-64 bg-white border-r border-slate-100/80 px-4 py-6 flex flex-col justify-between shrink-0">
          <div>
            {/* Logo */}
            <div className="flex items-center gap-3.5 mb-8 px-3">
              <img
                src={logosolo}
                alt="Logo RedNorte"
                className="w-15 h-15  object-cover"
              />
              <div>
                <h2 className="font-extrabold text-blue-900 text-lg leading-none tracking-tight">RedNorte</h2>
                <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block mt-0.5">Sistemas de Salud</span>
              </div>
            </div>

            {/* Menú de Navegación */}
            <nav className="space-y-1" aria-label="Main Navigation">
              {menuItems.map((item) => (
                <button
                  key={item.id}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-semibold transition-all cursor-pointer ${seccionActiva === item.id
                    ? 'navItemActive bg-blue-50/50 text-blue-600 font-bold'
                    : 'text-slate-500 hover:text-slate-900 hover:bg-slate-55 hover:bg-slate-50'
                    }`}
                  onClick={() => setSeccionActiva(item.id)}
                >
                  <span className="navItemIcon">{item.icon}</span>
                  <span>{item.label}</span>
                </button>
              ))}

            </nav>
          </div>
        </aside>

        {/* Contenido Principal */}
        <main className="flex-1 flex flex-col min-h-screen overflow-x-hidden p-6 md:p-8">

          {/* Header Bar */}
          <header className="flex flex-col sm:flex-row justify-between items-center gap-4 mb-8 bg-white p-4 rounded-xl shadow-xs border border-slate-100">
            <div className="relative w-full sm:w-96">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5" />
              <input
                type="text"
                placeholder="Buscar pacientes, clínicas..."
                value={busqueda}
                onChange={(e) => setBusqueda(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-slate-200 rounded-lg text-slate-700 bg-slate-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all text-sm"
              />
            </div>

            <div className="flex items-center gap-4 w-full sm:w-auto justify-end">
              <button className="relative p-2 text-slate-550 text-slate-500 hover:text-blue-500 hover:bg-slate-100 rounded-full transition-colors cursor-pointer">
                <Bell className="w-5 h-5" />
                <span className="absolute top-1.5 right-1.5 w-2.5 h-2.5 bg-red-500 rounded-full border-2 border-white"></span>
              </button>

              <div className="flex items-center gap-3 border-l border-slate-200 pl-4">
                <img
                  src={logo_usuario}
                  alt="Imagen del Usuario"
                  className="w-10 h-10 rounded-full border-2 border-black-100 object-cover"
                />
                <div className="text-left">
                  <p className="text-sm font-semibold text-slate-800">Dr. Benjamím Ibañes</p>
                </div>
              </div>
            </div>
          </header>

          {/* Hero Banner Header */}
          <div className="bg-gradient-to-r from-blue-900 via-indigo-955 via-indigo-900 to-slate-900 p-8 rounded-2xl text-white shadow-md mb-8 relative overflow-hidden">
            <div className="relative z-10 flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
              <div className="space-y-3">
                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white/10 text-white text-xs font-bold tracking-wider uppercase border border-white/10">
                  Sistema de salud pública
                </span>
                <h1 className="text-3xl md:text-4xl font-extrabold tracking-tight">Portal RedNorte</h1>
                <p className="text-slate-200 max-w-2xl text-sm leading-relaxed">
                  Gestión de pacientes, lista de espera, notificaciones y optimización de citas para
                  atención primaria y derivación asistida.
                </p>
              </div>
            </div>
          </div>

          {/* Sync Alert Banner */}
          {!syncErrorResolved && (
            <div className="bg-rose-50 border border-rose-100 rounded-xl p-4 mb-8 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 shadow-sm">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-rose-100 text-rose-600 rounded-lg">
                  {isSyncing ? (
                    <RefreshCw className="w-5 h-5 animate-spin" />
                  ) : (
                    <AlertTriangle className="w-5 h-5" />
                  )}
                </div>
                <div>
                  <p className="text-sm font-semibold text-rose-800">
                    {isSyncing ? 'Sincronizando archivos...' : 'Atención: No se pudieron sincronizar los resultados de laboratorio recientes para 3 pacientes.'}
                  </p>
                  <p className="text-xs text-rose-600 mt-0.5">
                    {isSyncing ? 'Conectando con el sistema central de laboratorios...' : 'Este es un problema de latencia temporal del servidor. Por favor, intente de nuevo.'}
                  </p>
                </div>
              </div>
              <button
                onClick={handleSyncRetry}
                disabled={isSyncing}
                className="px-4 py-2 bg-rose-600 hover:bg-rose-700 disabled:bg-rose-400 text-white text-xs font-bold rounded-lg shadow-xs transition-all flex items-center gap-1.5 cursor-pointer"
              >
                {isSyncing ? 'Sincronizando...' : 'Reintentar sincronización'}
              </button>
            </div>
          )}

          {syncErrorResolved && (
            <div className="bg-emerald-50 border border-emerald-100 rounded-xl p-4 mb-8 flex items-center shadow-xs">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-emerald-100 text-emerald-600 rounded-lg">
                  <Check className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-sm font-semibold text-emerald-800">¡Archivos médicos sincronizados con éxito!</p>
                  <p className="text-xs text-emerald-600 mt-0.5">Los reportes de laboratorio clínicos están al día.</p>
                </div>
              </div>
            </div>
          )}

          {/* Resumen Métricas */}
          <section className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
            {metricas.map((metrica) => (
              <article key={metrica.label} className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm transition-all hover:shadow-md">
                <p className="text-sm font-medium text-slate-500 mb-2">{metrica.label}</p>
                <div className="flex items-baseline gap-3">
                  <strong className="text-4xl font-bold text-slate-900 tracking-tight">{metrica.valor}</strong>
                  <span className="text-xs text-slate-400 font-medium">{metrica.detalle}</span>
                </div>
              </article>
            ))}
          </section>

          {/* Contenido Dinámico */}
          <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-6 md:p-8 min-h-[500px]">
            {seccionActiva === 'pacientes' && <GestionPacientes />}
            {seccionActiva === 'listaespera' && <ListaEspera />}
            {seccionActiva === 'notificaciones' && <Notificaciones />}
            {seccionActiva === 'optimizacion' && <Optimizacion />}
          </div>

          {/* Footer */}
          <footer className="mt-8 py-6 text-center text-xs text-slate-400 font-medium border-t border-slate-150 border-slate-200/50">
            <p>&copy; 2026 RedNorte - Sistema de salud pública. Todos los derechos reservados.</p>
          </footer>
        </main>
      </div>
    </div>
  )
}

export default App
