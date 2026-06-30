import { useState, useEffect } from 'react'
import Sidebar from './componentes/Sidebar'
import TopNavBar from './componentes/TopNavBar'
import Dashboard from './componentes/Dashboard'
import AdminDashboard from './componentes/AdminDashboard'
import AdminGestionUsuarios from './componentes/AdminGestionUsuarios'
import ReportesView from './componentes/ReportesView'
import GestionPacientes from './componentes/GestionPacientes'
import GestionCitas from './componentes/GestionCitas'
import ListaEspera from './componentes/ListaEspera'
import Notificaciones from './componentes/Notificaciones'
import Optimizacion from './componentes/Optimizacion'
import AgendarCita from './componentes/AgendarCita'
import TriagePaciente from './componentes/TriagePaciente'
import Login from './componentes/Login'
import PacienteAjustes from './componentes/PacienteAjustes'

function App() {
  const [activeSection, setActiveSection] = useState('dashboard')
  const [isDarkMode, setIsDarkMode] = useState(false)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [user, setUser] = useState(null)
  const [searchQuery, setSearchQuery] = useState('')

  useEffect(() => {
    // Verificar si hay un token guardado al cargar la aplicación
    const token = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    
    if (token && savedUser) {
      setIsAuthenticated(true)
      setUser(JSON.parse(savedUser))
    }
  }, [])

  useEffect(() => {
    if (isDarkMode) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }, [isDarkMode])

  const handleLoginSuccess = (userData) => {
    setIsAuthenticated(true)
    setUser({
      username: userData.username,
      role: userData.role
    })
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setIsAuthenticated(false)
    setUser(null)
    setActiveSection('dashboard')
  }

  const renderContent = () => {
    const isAdmin = user?.role === 'ROLE_ADMIN';
    const isPaciente = user?.role === 'ROLE_PACIENTE';

    if (isAdmin) {
      switch (activeSection) {
        case 'dashboard':
          return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><AdminDashboard user={user} /></div>
        case 'usuarios':
          return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><AdminGestionUsuarios /></div>
        case 'reportes':
          return <ReportesView />
        case 'ajustes':
          return (
            <div className="ml-[260px] pt-24 p-gutter min-h-screen space-y-4">
              <header className="flex items-center gap-3">
                <div className="p-2.5 bg-primary-container text-primary rounded-xl">
                  <span className="text-xl">⚙️</span>
                </div>
                <div>
                  <h2 className="text-2xl font-bold text-on-surface tracking-tight">Configuración del Sistema</h2>
                  <p className="text-sm text-on-surface-variant mt-1">Panel de administración del sistema</p>
                </div>
              </header>
              <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl p-8 text-center text-on-surface-variant">
                <p className="font-semibold">Próximamente — Configuración global del sistema, logs y parámetros avanzados.</p>
              </div>
            </div>
          )
        default:
          return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><AdminDashboard user={user} /></div>
      }
    }

    if (isPaciente) {
      switch (activeSection) {
        case 'dashboard':
          return <Dashboard user={user} />
        case 'citas':
          return <PacienteAjustes user={user} />
        case 'perfil':
          return <PacienteAjustes user={user} section="perfil" />
        default:
          return <Dashboard user={user} />
      }
    }

    switch (activeSection) {
      case 'dashboard':
        return <Dashboard user={user} onSectionChange={setActiveSection} />
      case 'citas':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><GestionCitas /></div>
      case 'pacientes':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><GestionPacientes onSectionChange={setActiveSection} /></div>
      case 'reportes':
        return <ReportesView />
      case 'ajustes':
        return (
          <div className="ml-[260px] pt-24 p-gutter min-h-screen space-y-4">
            <header className="flex items-center gap-3">
              <div className="p-2.5 bg-primary-container text-primary rounded-xl">
                <span className="text-xl">⚙️</span>
              </div>
              <div>
                <h2 className="text-2xl font-bold text-on-surface tracking-tight">Ajustes</h2>
                <p className="text-sm text-on-surface-variant mt-1">Panel de configuración del sistema</p>
              </div>
            </header>
            <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl p-8 text-center text-on-surface-variant">
              <p className="font-semibold">Próximamente — Personalización de perfil, preferencias y configuración avanzada.</p>
            </div>
          </div>
        )
      case 'listaespera':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><ListaEspera onSectionChange={setActiveSection} /></div>
      case 'notificaciones':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><Notificaciones /></div>
      case 'optimizacion':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><Optimizacion /></div>
      case 'agendarcita':
        return <AgendarCita onSectionChange={setActiveSection} />
      case 'triaje':
        return <TriagePaciente onSectionChange={setActiveSection} />
      default:
        return <Dashboard />
    }
  }

  // Mostrar Login si no está autenticado
  if (!isAuthenticated) {
    return <Login onLoginSuccess={handleLoginSuccess} />
  }

  return (
    <div className="bg-surface text-on-surface font-body-md overflow-x-hidden">
      <Sidebar 
        user={user}
        activeSection={activeSection} 
        onSectionChange={setActiveSection}
        isDarkMode={isDarkMode}
        onToggleDarkMode={() => setIsDarkMode(!isDarkMode)}
        onLogout={handleLogout}
      />
      <TopNavBar
        user={user}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        onSectionChange={setActiveSection}
        onLogout={handleLogout}
      />
      {renderContent()}
      <footer className="ml-[260px] text-center text-on-surface-variant text-sm py-4 border-t border-outline-variant">
        © 2026 RedNorte. Todos los derechos reservados.
      </footer>
    </div>
  )
}

export default App
