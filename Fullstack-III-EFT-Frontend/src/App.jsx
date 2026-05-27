import { useState, useEffect } from 'react'
import './App.css'
import GestionPacientes from './componentes/GestionPacientes'
import ListaEspera from './componentes/ListaEspera'
import Notificaciones from './componentes/Notificaciones'
import Optimizacion from './componentes/Optimizacion'
import logo from './assets/logo.png'
import { obtenerResumenPortal } from './api/portalApi'

function App() {
  const [seccionActiva, setSeccionActiva] = useState('pacientes')
  const [resumenPortal, setResumenPortal] = useState(null)
  const [busqueda, setBusqueda] = useState('')

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

  const menuItems = [
    { id: 'pacientes', label: 'Gestión de Pacientes', icon: '👥' },
    { id: 'listaespera', label: 'Lista de Espera', icon: '📋' },
    { id: 'notificaciones', label: 'Notificaciones', icon: '🔔' },
    { id: 'optimizacion', label: 'Optimización', icon: '⚡' },
  ]

  return (
    <div className="container">
      {/* Sidebar Lateral */}
      <aside className="sidebar">
        <div>
          <h2 className="sidebarTitle">RedNorte</h2>
          <p className="sidebarSubtitle">Sistema de salud pública</p>
        </div>

        {/* Barra de Búsqueda */}
        <div className="searchBox">
          <span className="searchIcon">🔍</span>
          <input
            type="text"
            className="searchInput"
            placeholder="Buscar..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />
        </div>

        {/* Menú de Navegación */}
        <nav className="navMenu">
          {menuItems.map((item) => (
            <button
              key={item.id}
              className={`navItem ${seccionActiva === item.id ? 'navItemActive' : ''}`}
              onClick={() => setSeccionActiva(item.id)}
            >
              <span className="navItemIcon">{item.icon}</span>
              <span>{item.label}</span>
            </button>
          ))}
        </nav>

        {/* Métricas en Sidebar */}
        <div style={{ marginTop: 'auto', paddingTop: '20px', borderTop: '2px solid #e2e8f0' }}>
          <p style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '12px', fontWeight: 600 }}>RESUMEN</p>
          {metricas.map((metrica) => (
            <div key={metrica.label} style={{ marginBottom: '12px' }}>
              <p style={{ fontSize: '0.8rem', color: '#94a3b8', marginBottom: '4px' }}>{metrica.label}</p>
              <p style={{ fontSize: '1.5rem', fontWeight: 800, color: '#0ea5e9', lineHeight: 1 }}>{metrica.valor}</p>
            </div>
          ))}
        </div>
      </aside>

      {/* Contenido Principal */}
      <div className="mainContent">
        <header className="header">
          <div className="hero-copy">
            <span className="hero-badge">RedNorte · Sistema de salud pública</span>
            <h1>Portal RedNorte</h1>
            <p>
              Gestión de pacientes, lista de espera, notificaciones y optimización de citas para
              atención primaria y derivación asistida.
            </p>
            <div className="hero-tags">
              <span>Atención primaria</span>
              <span>Derivación asistida</span>
              <span>Portal unificado</span>
            </div>
          </div>

          <div className="hero-visual">
            <img src={logo} alt="Equipo del proyecto" />
            <div className="hero-note">
              <strong>RedNorte en operación</strong>
              <p>Lectura rápida del estado del portal para apoyar la gestión clínica diaria.</p>
            </div>
          </div>

          <div className="portal-resumen">
            {metricas.map((metrica) => (
              <article key={metrica.label} className="summary-card">
                <span>{metrica.label}</span>
                <strong>{metrica.valor}</strong>
                <p>{metrica.detalle}</p>
              </article>
            ))}
          </div>
        </header>

        {/* Contenido */}
        <main className="content">
          {seccionActiva === 'pacientes' && <GestionPacientes />}
          {seccionActiva === 'listaespera' && <ListaEspera />}
          {seccionActiva === 'notificaciones' && <Notificaciones />}
          {seccionActiva === 'optimizacion' && <Optimizacion />}
        </main>

        {/* Footer */}
        <footer className="footer">
          <p>&copy; 2026 RedNorte - Sistema de salud pública</p>
        </footer>
      </div>
    </div>
  )
}

export default App
