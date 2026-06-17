import { useState } from 'react'
import logo2Logo from '../assets/logo2.png'

const Sidebar = ({ activeSection, onSectionChange, isDarkMode, onToggleDarkMode }) => {
  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: 'dashboard' },
    { id: 'pacientes', label: 'Pacientes', icon: 'group' },
    { id: 'clinicas', label: 'Clínicas', icon: 'medical_services' },
    { id: 'reportes', label: 'Reportes', icon: 'analytics' },
    { id: 'ajustes', label: 'Ajustes', icon: 'settings' },
  ]

  return (
    <aside className="fixed left-0 top-0 h-full flex flex-col p-gutter bg-surface dark:bg-surface-dim w-sidebar-width z-50">
      <div className="flex items-center gap-3 mb-10 px-2">
        <img src={logo2Logo} alt="RedNorte Logo" className="w-20 h-20 rounded-lg object-cover" />
        <div>
          <h1 className="font-headline-md text-headline-md font-bold text-primary dark:text-primary-dark">RedNorte</h1>
          <p className="font-label-sm text-label-sm text-on-surface-variant">Administración Médica</p>
        </div>
      </div>

      <nav className="flex-1 flex flex-col gap-3">
        {menuItems.map((item) => (
          <button
            key={item.id}
            onClick={() => onSectionChange(item.id)}
            className={`flex items-center gap-4 p-3 rounded-lg transition-all duration-200 ${
              activeSection === item.id
                ? 'sidebar-item-active'
                : 'text-on-surface-variant hover:bg-surface-container-high'
            }`}
          >
            <span
              className="material-symbols-outlined"
              style={{ fontVariationSettings: activeSection === item.id ? "'FILL' 1" : "'FILL' 0" }}
            >
              {item.icon}
            </span>
            <span className="font-body-md text-body-md">{item.label}</span>
          </button>
        ))}
      </nav>

      <div className="mt-auto border-t border-outline-variant pt-6 flex flex-col gap-3">
        <button 
          onClick={onToggleDarkMode}
          className="flex items-center gap-4 p-3 text-on-surface-variant hover:bg-surface-container-high transition-colors duration-200 rounded-lg"
        >
          <span className="material-symbols-outlined">
            {isDarkMode ? 'light_mode' : 'dark_mode'}
          </span>
          <span className="font-body-md text-body-md">
            {isDarkMode ? 'Modo Claro' : 'Modo Oscuro'}
          </span>
        </button>
        <button className="flex items-center gap-4 p-3 text-on-surface-variant hover:bg-surface-container-high transition-colors duration-200 rounded-lg">
          <span className="material-symbols-outlined">help</span>
          <span className="font-body-md text-body-md">Ayuda</span>
        </button>
        <button className="flex items-center gap-4 p-3 text-on-surface-variant hover:bg-surface-container-high transition-colors duration-200 rounded-lg">
          <span className="material-symbols-outlined text-error">logout</span>
          <span className="font-body-md text-body-md">Cerrar Sesión</span>
        </button>
      </div>
    </aside>
  )
}

export default Sidebar
