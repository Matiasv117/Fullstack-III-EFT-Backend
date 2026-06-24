import { useState, useEffect } from 'react'
import Sidebar from './componentes/Sidebar'
import TopNavBar from './componentes/TopNavBar'
import Dashboard from './componentes/Dashboard'
import GestionPacientes from './componentes/GestionPacientes'
import ListaEspera from './componentes/ListaEspera'
import Notificaciones from './componentes/Notificaciones'
import Optimizacion from './componentes/Optimizacion'
import Login from './componentes/Login'
import ClinicalOptions from './componentes/ClinicalOptions'
import Ayuda from './componentes/Ayuda'
import Reportes from './componentes/Reportes'
import Ajustes from './componentes/Ajustes'

function App() {
  const [activeSection, setActiveSection] = useState('dashboard')
  const [isDarkMode, setIsDarkMode] = useState(false)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [user, setUser] = useState(null)
  const [searchTerm, setSearchTerm] = useState('')

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

  const handleShowAyuda = () => {
    setActiveSection('ayuda')
  }

  const renderContent = () => {
    switch (activeSection) {
      case 'dashboard':
        return <Dashboard user={user} />
      case 'pacientes':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><GestionPacientes searchTerm={searchTerm} /></div>
      case 'clinicas':
        return <ClinicalOptions />
      case 'reportes':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><Reportes /></div>
      case 'ajustes':
        return (
          <div className="ml-[260px] pt-24 p-gutter min-h-screen">
            <Ajustes 
              user={user} 
              isDarkMode={isDarkMode} 
              onToggleDarkMode={() => setIsDarkMode(!isDarkMode)} 
              onLogout={handleLogout} 
            />
          </div>
        )
      case 'listaespera':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><ListaEspera /></div>
      case 'notificaciones':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><Notificaciones /></div>
      case 'optimizacion':
        return <div className="ml-[260px] pt-24 p-gutter min-h-screen"><Optimizacion /></div>
      case 'ayuda':
        return <Ayuda />
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
        onShowAyuda={handleShowAyuda}
      />
      <TopNavBar 
        user={user} 
        onLogout={handleLogout} 
        searchValue={searchTerm} 
        setSearchValue={setSearchTerm} 
        onSearchFocus={() => setActiveSection('pacientes')} 
      />
      {renderContent()}
    </div>
  )
}

export default App
