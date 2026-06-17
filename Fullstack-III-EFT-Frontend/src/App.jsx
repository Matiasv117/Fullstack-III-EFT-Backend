import { useState, useEffect } from 'react'
import Sidebar from './componentes/Sidebar'
import TopNavBar from './componentes/TopNavBar'
import Dashboard from './componentes/Dashboard'
import GestionPacientes from './componentes/GestionPacientes'
import ListaEspera from './componentes/ListaEspera'
import Notificaciones from './componentes/Notificaciones'
import Optimizacion from './componentes/Optimizacion'

function App() {
  const [activeSection, setActiveSection] = useState('dashboard')
  const [isDarkMode, setIsDarkMode] = useState(false)

  useEffect(() => {
    if (isDarkMode) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }, [isDarkMode])

  const renderContent = () => {
    switch (activeSection) {
      case 'dashboard':
        return <Dashboard />
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

  return (
    <div className="bg-surface text-on-surface font-body-md overflow-x-hidden">
      <Sidebar 
        activeSection={activeSection} 
        onSectionChange={setActiveSection}
        isDarkMode={isDarkMode}
        onToggleDarkMode={() => setIsDarkMode(!isDarkMode)}
      />
      <TopNavBar />
      {renderContent()}
    </div>
  )
}

export default App
