import { useState } from 'react';
import { Settings, Moon, Sun, User, Shield, Server, LogOut, Info, Monitor } from 'lucide-react';
import ConnectionStatus from './ConnectionStatus';

function Ajustes({ user, isDarkMode, onToggleDarkMode, onLogout }) {
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false);

  const systemInfo = [
    { label: 'Versión del Frontend', value: 'v1.0.0' },
    { label: 'Framework', value: 'React 19 + Vite 8' },
    { label: 'Estilos', value: 'TailwindCSS v4' },
    { label: 'BFF Endpoint', value: 'http://localhost:8080' },
    { label: 'Auth Endpoint', value: 'http://localhost:8097' },
    { label: 'Base de Datos', value: 'PostgreSQL 16 (Docker)' },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <header className="flex items-center gap-3">
        <div className="p-2.5 bg-primary-container text-primary rounded-xl">
          <Settings className="w-6 h-6" />
        </div>
        <div>
          <h2 className="text-2xl font-bold text-on-surface tracking-tight">Ajustes</h2>
          <p className="text-sm text-on-surface-variant mt-1">
            Configuración del sistema, perfil de usuario y estado de servicios.
          </p>
        </div>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left column: Settings */}
        <div className="lg:col-span-2 space-y-6">
          {/* User Profile Card */}
          <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs overflow-hidden">
            <div className="p-5 border-b border-outline-variant bg-surface-container-low">
              <h3 className="font-bold text-on-surface text-sm flex items-center gap-2">
                <User className="w-4 h-4 text-primary" />
                Perfil de Usuario
              </h3>
            </div>
            <div className="p-5 space-y-4">
              <div className="flex items-center gap-4">
                <div className="w-16 h-16 bg-primary-container rounded-xl flex items-center justify-center">
                  <User className="w-8 h-8 text-primary" />
                </div>
                <div>
                  <h4 className="text-lg font-bold text-on-surface">{user?.username || 'Usuario'}</h4>
                  <div className="flex items-center gap-2 mt-1">
                    <Shield className="w-3.5 h-3.5 text-on-surface-variant" />
                    <span className="text-xs font-bold text-on-surface-variant bg-primary/10 px-2 py-0.5 rounded-full">
                      {user?.role?.replace('ROLE_', '') || 'USER'}
                    </span>
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-3 border-t border-outline-variant">
                <div className="bg-surface-container-low p-3 rounded-lg">
                  <span className="block text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-0.5">Nombre de usuario</span>
                  <span className="text-sm font-semibold text-on-surface">{user?.username || 'N/A'}</span>
                </div>
                <div className="bg-surface-container-low p-3 rounded-lg">
                  <span className="block text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-0.5">Rol asignado</span>
                  <span className="text-sm font-semibold text-on-surface">{user?.role || 'N/A'}</span>
                </div>
                <div className="bg-surface-container-low p-3 rounded-lg">
                  <span className="block text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-0.5">Token JWT</span>
                  <span className="text-xs font-mono text-on-surface-variant truncate block">
                    {localStorage.getItem('token')?.substring(0, 30)}...
                  </span>
                </div>
                <div className="bg-surface-container-low p-3 rounded-lg">
                  <span className="block text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-0.5">Estado de sesión</span>
                  <span className="text-xs font-extrabold text-emerald-600 bg-emerald-50 dark:bg-emerald-950/30 px-2 py-0.5 rounded-md">
                    ACTIVA
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Appearance */}
          <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs overflow-hidden">
            <div className="p-5 border-b border-outline-variant bg-surface-container-low">
              <h3 className="font-bold text-on-surface text-sm flex items-center gap-2">
                <Monitor className="w-4 h-4 text-primary" />
                Apariencia
              </h3>
            </div>
            <div className="p-5">
              <div className="flex items-center justify-between p-4 bg-surface-container-low rounded-xl border border-outline-variant">
                <div className="flex items-center gap-3">
                  {isDarkMode ? (
                    <Moon className="w-5 h-5 text-indigo-500" />
                  ) : (
                    <Sun className="w-5 h-5 text-amber-500" />
                  )}
                  <div>
                    <p className="text-sm font-bold text-on-surface">Modo {isDarkMode ? 'Oscuro' : 'Claro'}</p>
                    <p className="text-xs text-on-surface-variant">
                      {isDarkMode ? 'Interfaz con fondo oscuro para menos fatiga visual' : 'Interfaz estándar con fondo claro'}
                    </p>
                  </div>
                </div>
                <button
                  onClick={onToggleDarkMode}
                  className={`relative w-14 h-7 rounded-full transition-colors duration-300 cursor-pointer ${
                    isDarkMode ? 'bg-indigo-600' : 'bg-slate-300'
                  }`}
                >
                  <div
                    className={`absolute top-0.5 w-6 h-6 bg-white rounded-full shadow-md transition-transform duration-300 ${
                      isDarkMode ? 'translate-x-7.5' : 'translate-x-0.5'
                    }`}
                  />
                </button>
              </div>
            </div>
          </div>

          {/* System Info */}
          <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs overflow-hidden">
            <div className="p-5 border-b border-outline-variant bg-surface-container-low">
              <h3 className="font-bold text-on-surface text-sm flex items-center gap-2">
                <Info className="w-4 h-4 text-primary" />
                Información del Sistema
              </h3>
            </div>
            <div className="p-5">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {systemInfo.map((item, index) => (
                  <div key={index} className="bg-surface-container-low p-3 rounded-lg border border-outline-variant/50">
                    <span className="block text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-0.5">
                      {item.label}
                    </span>
                    <span className="text-xs font-semibold text-on-surface font-mono">{item.value}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Logout */}
          <div className="bg-surface-container-lowest border border-error/20 rounded-2xl shadow-xs overflow-hidden">
            <div className="p-5">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <LogOut className="w-5 h-5 text-error" />
                  <div>
                    <p className="text-sm font-bold text-on-surface">Cerrar Sesión</p>
                    <p className="text-xs text-on-surface-variant">Tu sesión se cerrará y serás redirigido al login.</p>
                  </div>
                </div>
                {showLogoutConfirm ? (
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => setShowLogoutConfirm(false)}
                      className="px-3 py-1.5 text-xs font-bold text-on-surface-variant bg-surface-container-low border border-outline-variant rounded-lg cursor-pointer hover:bg-surface-container-high transition-colors"
                    >
                      Cancelar
                    </button>
                    <button
                      onClick={onLogout}
                      className="px-3 py-1.5 text-xs font-bold text-white bg-error rounded-lg cursor-pointer hover:bg-error/90 transition-colors shadow-xs"
                    >
                      Sí, cerrar sesión
                    </button>
                  </div>
                ) : (
                  <button
                    onClick={() => setShowLogoutConfirm(true)}
                    className="px-4 py-2 text-xs font-bold text-error bg-error-container/30 border border-error/20 rounded-lg cursor-pointer hover:bg-error-container/60 transition-colors"
                  >
                    Cerrar Sesión
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Right column: Connection Status */}
        <div className="space-y-6">
          <ConnectionStatus />
        </div>
      </div>
    </div>
  );
}

export default Ajustes;
