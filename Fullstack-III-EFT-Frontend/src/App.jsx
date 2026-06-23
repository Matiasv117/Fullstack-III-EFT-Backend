import { useState, useEffect } from 'react'
import Sidebar from './componentes/Sidebar'
import TopNavBar from './componentes/TopNavBar'
import Dashboard from './componentes/Dashboard'
import GestionPacientes from './componentes/GestionPacientes'
import ListaEspera from './componentes/ListaEspera'
import Notificaciones from './componentes/Notificaciones'
import Optimizacion from './componentes/Optimizacion'
import Login from './componentes/Login'

function App() {
  const [activeSection, setActiveSection] = useState('dashboard')
  const [isDarkMode, setIsDarkMode] = useState(false)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [user, setUser] = useState(null)

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
    switch (activeSection) {
      case 'dashboard':
        return <Dashboard user={user} />
      case 'pacientes':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><GestionPacientes /></div>
      case 'clinicas':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><h1 className="text-2xl font-bold">Clínicas</h1></div>
      case 'reportes':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><h1 className="text-2xl font-bold">Reportes</h1></div>
      case 'ajustes':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><h1 className="text-2xl font-bold">Ajustes</h1></div>
      case 'listaespera':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><ListaEspera /></div>
      case 'notificaciones':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><Notificaciones /></div>
      case 'optimizacion':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><Optimizacion /></div>
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
      />
      <TopNavBar user={user} onLogout={handleLogout} />
      {renderContent()}
    </div>
  )
}

export default App
