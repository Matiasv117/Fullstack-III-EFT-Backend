import { useState } from 'react';
import logo2Logo from '../assets/logo2.png'
import AyudaModal from './AyudaModal';

const Sidebar = ({ user, activeSection, onSectionChange, isDarkMode, onToggleDarkMode, onLogout }) => {
  const [ayudaOpen, setAyudaOpen] = useState(false);
  const isPaciente = user?.role === 'ROLE_PACIENTE';
  const isAdmin = user?.role === 'ROLE_ADMIN';

  const menuItems = isPaciente
    ? [
      { id: 'dashboard', label: 'Mi Portal', icon: 'dashboard' },
    ]
    : isAdmin
    ? [
      { id: 'dashboard', label: 'Panel Admin', icon: 'admin_panel_settings' },
      { id: 'usuarios', label: 'Gestión de Usuarios', icon: 'people' },
      { id: 'reportes', label: 'Reportes y Auditoría', icon: 'analytics' },
      { id: 'ajustes', label: 'Configuración', icon: 'settings' },
    ]
    : [
      { id: 'dashboard', label: 'Dashboard', icon: 'dashboard' },
      { id: 'pacientes', label: 'Pacientes', icon: 'group' },
      { id: 'listaespera', label: 'Lista de Espera', icon: 'list_alt' },
      { id: 'notificaciones', label: 'Notificaciones', icon: 'notifications' },
      { id: 'optimizacion', label: 'Optimización', icon: 'query_stats' },
      { id: 'clinicas', label: 'Clínicas', icon: 'medical_services' },
      { id: 'reportes', label: 'Reportes', icon: 'analytics' },
      { id: 'ajustes', label: 'Ajustes', icon: 'settings' },
    ];

  return (
    <aside className="fixed left-0 top-0 h-full flex flex-col p-gutter bg-surface dark:bg-surface-dim w-sidebar-width z-50">
      <div className="flex items-center gap-3 mb-10 px-2">
        <img src={logo2Logo} alt="RedNorte Logo" className="w-24 h-20 rounded-lg object-cover" />
        <div>
          <h1 className="font-headline-md text-headline-md font-bold text-primary dark:text-primary-dark">RedNorte</h1>
          <p className="font-label-sm text-label-sm text-on-surface-variant">
            {isPaciente ? 'Portal del Paciente' : isAdmin ? 'Administración del Sistema' : 'Administración Médica'}
          </p>
        </div>
      </div>

      <nav className="flex-1 flex flex-col gap-3">
        {menuItems.map((item) => (
          <button
            key={item.id}
            onClick={() => onSectionChange(item.id)}
            className={`flex items-center gap-4 p-3 rounded-lg transition-all duration-200 ${activeSection === item.id
              ? 'sidebar-item-active'
              : 'text-on-surface-variant hover:bg-surface-container-high'
              }`}
          >
            <span
              className="material-symbols-outlined"
              aria-hidden="true"
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
          <span className="material-symbols-outlined" aria-hidden="true">
            {isDarkMode ? 'light_mode' : 'dark_mode'}
          </span>
          <span className="font-body-md text-body-md">
            {isDarkMode ? 'Modo Claro' : 'Modo Oscuro'}
          </span>
        </button>
        <button onClick={() => setAyudaOpen(true)} className="flex items-center gap-4 p-3 text-on-surface-variant hover:bg-surface-container-high transition-colors duration-200 rounded-lg">
          <span className="material-symbols-outlined" aria-hidden="true">help</span>
          <span className="font-body-md text-body-md">Ayuda</span>
        </button>
        <AyudaModal isOpen={ayudaOpen} onClose={() => setAyudaOpen(false)} />
        <button onClick={onLogout} className="flex items-center gap-4 p-3 text-on-surface-variant hover:bg-surface-container-high transition-colors duration-200 rounded-lg">
          <span className="material-symbols-outlined text-error" aria-hidden="true">logout</span>
          <span className="font-body-md text-body-md">Cerrar Sesión</span>
        </button>
      </div>
    </aside>
  )
}

export default Sidebar
