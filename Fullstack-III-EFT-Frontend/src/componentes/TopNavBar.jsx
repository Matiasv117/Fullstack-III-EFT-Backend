import { useEffect, useRef, useState } from 'react'
import { LogOut, User } from 'lucide-react'
import { buscarGlobal } from '../api/searchApi'
import { obtenerNotificacionesPendientes } from '../api/notificacionesApi'
import GlobalSearch from './GlobalSearch'

const roleLabels = {
  ROLE_ADMIN: 'Administrador',
  ROLE_FUNCIONARIO: 'Funcionario',
  ROLE_PACIENTE: 'Paciente',
}

const TopNavBar = ({ user, searchQuery, onSearchChange, onSectionChange, onLogout }) => {
  const roleLabel = roleLabels[user?.role] || user?.role?.replace('ROLE_', '') || 'USER'
  const [results, setResults] = useState({ pacientes: [], citas: [], listaEspera: [], funcionarios: [] });
  const [showSearch, setShowSearch] = useState(false);
  const [notifs, setNotifs] = useState([])
  const [showNotifs, setShowNotifs] = useState(false)
  const notifRef = useRef(null)
  const debounceRef = useRef(null);

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    if (!searchQuery || searchQuery.trim().length < 2) {
      setResults({ pacientes: [], citas: [], listaEspera: [], funcionarios: [] });
      return;
    }
    debounceRef.current = setTimeout(async () => {
      const data = await buscarGlobal(searchQuery, user?.role);
      setResults(data);
      setShowSearch(true);
    }, 300);
    return () => { if (debounceRef.current) clearTimeout(debounceRef.current); };
  }, [searchQuery, user?.role]);

  useEffect(() => {
    obtenerNotificacionesPendientes()
      .then(data => setNotifs(Array.isArray(data) ? data : []))
      .catch(() => setNotifs([]))
  }, [])

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (notifRef.current && !notifRef.current.contains(e.target)) {
        setShowNotifs(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  return (
    <header className="fixed top-0 right-0 h-16 px-gutter ml-[260px] w-[calc(100%-260px)] flex justify-between items-center bg-surface-bright dark:bg-surface-container border-b border-outline-variant z-40">
      <div className="flex-1 max-w-xl relative">
        <div className="relative group">
          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant transition-colors duration-300 group-focus-within:text-primary">
            search
          </span>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => {
              onSearchChange(e.target.value);
              setShowSearch(true);
            }}
            onFocus={() => { if (searchQuery?.trim().length >= 2) setShowSearch(true); }}
            placeholder="Buscar pacientes, citas o historiales..."
            className="w-full pl-11 pr-4 py-2 bg-surface-container-low border border-outline-variant rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all duration-[var(--animation-gentle)] placeholder:text-on-surface-variant/50 focus:bg-white dark:focus:bg-surface-container-lowest"
          />
        </div>
        {showSearch && (
          <GlobalSearch
            results={results}
            query={searchQuery}
            onNavigate={onSectionChange}
            onClose={() => setShowSearch(false)}
          />
        )}
      </div>

      <div className="flex items-center gap-4 ml-6" ref={notifRef}>
        <button
          onClick={() => setShowNotifs(v => !v)}
          className="p-2 text-on-surface-variant hover:text-primary relative transition-all duration-200 active:scale-95"
        >
          <span className="material-symbols-outlined">notifications</span>
          {notifs.length > 0 && (
            <span className="absolute -top-0.5 -right-0.5 w-4.5 h-4.5 flex items-center justify-center bg-error text-white text-[10px] font-bold rounded-full">
              {notifs.length > 9 ? '9+' : notifs.length}
            </span>
          )}
        </button>
        {showNotifs && (
          <div className="absolute top-14 right-36 w-80 max-h-96 overflow-y-auto bg-surface-container-lowest border border-outline-variant rounded-xl shadow-lg z-50 p-2">
            {notifs.length === 0 ? (
              <p className="text-on-surface-variant text-sm p-3">No hay notificaciones pendientes.</p>
            ) : (
              <ul className="space-y-1">
                {notifs.map((n, i) => (
                  <li key={n.id || i} className="p-3 rounded-lg hover:bg-surface-container-low transition-colors cursor-pointer">
                    <p className="font-medium text-sm text-on-surface">{n.tipo?.replace(/_/g, ' ') || 'Notificación'}</p>
                    <p className="text-xs text-on-surface-variant mt-0.5 line-clamp-2">{n.mensaje}</p>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
        <div className="h-8 w-[1px] bg-outline-variant mx-2"></div>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-surface-container-high dark:bg-surface-container-low">
            <User className="w-5 h-5 text-on-surface-variant" />
            <span className="font-medium text-sm text-on-surface">
              {user?.username || 'Usuario'}
            </span>
            <span className="text-xs text-on-surface-variant bg-primary/10 px-2 py-0.5 rounded-full">
              {roleLabel}
            </span>
          </div>
          <button
            onClick={onLogout}
            className="flex items-center gap-2 px-3 py-1.5 rounded-full hover:bg-error/10 text-error transition-all duration-200"
            title="Cerrar sesión"
          >
            <LogOut className="w-5 h-5" />
          </button>
        </div>
      </div>
    </header>
  )
}

export default TopNavBar
