import { useState } from 'react'
import './App.css'
import GestionPacientes from './componentes/GestionPacientes'
import Notificaciones from './componentes/Notificaciones'
import Optimizacion from './componentes/Optimizacion'
import parguelas from './assets/parguelas.jpg'
function App() {
  const [seccionActiva, setSeccionActiva] = useState('pacientes')

  return (
    <div className="container">
      {/* Header */}
      <header className="header">
        <h1>Fullstack III - Sistema de Salud</h1>
        <p>Gestión de Pacientes y Optimización de Citas</p>
        <img src={parguelas} style={{height: '110px', width: 'auto'}}/>
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
        {seccionActiva === 'notificaciones' && <Notificaciones />}
        {seccionActiva === 'optimizacion' && <Optimizacion />}
      </main>

      {/* Footer */}
      <footer className="footer">
        <p>&copy; 2026 Salud Norte - Todos los derechos reservados</p>
      </footer>
    </div>
  )
}

export default App
