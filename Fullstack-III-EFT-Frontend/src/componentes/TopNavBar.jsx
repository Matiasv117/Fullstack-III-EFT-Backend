import { useState } from 'react'
import { LogOut, User } from 'lucide-react'

const TopNavBar = ({ user, onLogout }) => {
  const [searchValue, setSearchValue] = useState('')

  return (
    <header className="fixed top-0 right-0 h-16 px-gutter ml-[260px] w-[calc(100%-260px)] flex justify-between items-center bg-surface-bright dark:bg-surface-container border-b border-outline-variant z-40">
      <div className="flex-1 max-w-xl">
        <div className="relative group">
          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant transition-colors duration-300 group-focus-within:text-primary">
            search
          </span>
          <input
            type="text"
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            placeholder="Buscar pacientes, citas o historiales..."
            className="w-full pl-11 pr-4 py-2 bg-surface-container-low border border-outline-variant rounded-lg focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all duration-[var(--animation-gentle)] placeholder:text-on-surface-variant/50 focus:bg-white dark:focus:bg-surface-container-lowest"
          />
        </div>
      </div>

      <div className="flex items-center gap-4 ml-6">
        <button className="p-2 text-on-surface-variant hover:text-primary transition-all duration-200 active:scale-95">
          <span className="material-symbols-outlined">sync</span>
        </button>
        <button className="p-2 text-on-surface-variant hover:text-primary relative transition-all duration-200 active:scale-95">
          <span className="material-symbols-outlined">notifications</span>
          <span className="absolute top-2.5 right-2.5 w-1.5 h-1.5 bg-error rounded-full pulsate"></span>
          <span className="absolute top-2 right-2 w-2 h-2 bg-error rounded-full border-2 border-surface-bright dark:border-surface-container"></span>
        </button>
        <div className="h-8 w-[1px] bg-outline-variant mx-2"></div>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-surface-container-high dark:bg-surface-container-low">
            <User className="w-5 h-5 text-on-surface-variant" />
            <span className="font-medium text-sm text-on-surface">
              {user?.username || 'Usuario'}
            </span>
            <span className="text-xs text-on-surface-variant bg-primary/10 px-2 py-0.5 rounded-full">
              {user?.role?.replace('ROLE_', '') || 'USER'}
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
