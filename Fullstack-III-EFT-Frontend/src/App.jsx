import { useState, useEffect } from 'react'
import './App.css'
import GestionPacientes from './componentes/GestionPacientes'
import ListaEspera from './componentes/ListaEspera'
import Notificaciones from './componentes/Notificaciones'
import Optimizacion from './componentes/Optimizacion'
import parguelas from './assets/parguelas.jpg'
import { obtenerResumenPortal } from './api/portalApi'

function App() {
  const [seccionActiva, setSeccionActiva] = useState('pacientes')
  const [resumenPortal, setResumenPortal] = useState(null)

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
        const data = await obtenerResumenPortal()
        setResumenPortal(data?.resumen ?? null)
      } catch {
        setResumenPortal(null)
      }
    }
    cargarResumen()
  }, [])

  return (
    <div className="container">
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
          <img src={parguelas} alt="Equipo del proyecto" />
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

      {/* Navegación */}
      <nav className="nav">
        <button
          className={seccionActiva === 'pacientes' ? 'active' : ''}
          onClick={() => setSeccionActiva('pacientes')}
        >
          Pacientes
        </button>
        <button
          className={seccionActiva === 'listaespera' ? 'active' : ''}
          onClick={() => setSeccionActiva('listaespera')}
        >
          Lista de Espera
        </button>
        <button
          className={seccionActiva === 'notificaciones' ? 'active' : ''}
          onClick={() => setSeccionActiva('notificaciones')}
        >
          Notificaciones
        </button>
        <button
          className={seccionActiva === 'optimizacion' ? 'active' : ''}
          onClick={() => setSeccionActiva('optimizacion')}
        >
          Optimización
        </button>
      </nav>

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
  )
}

export default App
