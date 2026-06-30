import { Users, Settings, Lock, BarChart3 } from 'lucide-react';

function AdminDashboard({ user }) {
  return (
    <div className="space-y-8">
      <header>
        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Panel de Administración</h1>
        <p className="text-slate-600 dark:text-slate-400 mt-2">Bienvenido, {user?.username}</p>
      </header>

      {/* Tarjetas de Acceso Rápido */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-lg p-6 shadow-lg hover:shadow-xl transition-shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium opacity-90">Gestión de Funcionarios</p>
              <p className="text-3xl font-bold mt-2">Administrar</p>
            </div>
            <Users className="w-12 h-12 opacity-20" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-purple-500 to-purple-600 text-white rounded-lg p-6 shadow-lg hover:shadow-xl transition-shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium opacity-90">Control de Acceso</p>
              <p className="text-3xl font-bold mt-2">Seguridad</p>
            </div>
            <Lock className="w-12 h-12 opacity-20" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-orange-500 to-orange-600 text-white rounded-lg p-6 shadow-lg hover:shadow-xl transition-shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium opacity-90">Configuración del Sistema</p>
              <p className="text-3xl font-bold mt-2">Ajustes</p>
            </div>
            <Settings className="w-12 h-12 opacity-20" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-emerald-500 to-emerald-600 text-white rounded-lg p-6 shadow-lg hover:shadow-xl transition-shadow">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium opacity-90">Reportes y Auditoría</p>
              <p className="text-3xl font-bold mt-2">Reportes</p>
            </div>
            <BarChart3 className="w-12 h-12 opacity-20" />
          </div>
        </div>
      </div>

      {/* Información de Admin */}
      <div className="bg-white dark:bg-slate-800/90 border border-slate-200 dark:border-slate-700 rounded-lg p-6 shadow-sm">
        <div className="flex items-center gap-3 mb-4">
          <div className="w-1 h-6 bg-blue-500 rounded-full" />
          <h2 className="text-lg font-bold text-slate-900 dark:text-white">Información de Administrador</h2>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-slate-50 dark:bg-slate-700/50 rounded-lg p-4">
            <p className="text-sm font-medium text-slate-500 dark:text-slate-400 mb-1">Usuario</p>
            <p className="text-lg font-semibold text-slate-900 dark:text-white">{user?.username}</p>
          </div>
          <div className="bg-slate-50 dark:bg-slate-700/50 rounded-lg p-4">
            <p className="text-sm font-medium text-slate-500 dark:text-slate-400 mb-1">Rol</p>
            <p className="text-lg font-semibold text-slate-900 dark:text-white">Administrador del Sistema</p>
          </div>
        </div>
      </div>

      {/* Aviso Importante */}
      <div className="bg-white dark:bg-slate-800 border border-red-300 dark:border-red-700 rounded-lg p-6">
        <div className="flex items-start gap-4">
          <div className="w-1 h-14 bg-red-500 rounded-full flex-shrink-0 mt-0.5" />
          <div>
            <h3 className="font-bold text-red-800 dark:text-red-300 mb-1">⚠️ Acceso Restringido</h3>
            <p className="text-sm text-red-700 dark:text-red-400">
              Este panel solo es accesible para administradores del sistema. Todos los cambios realizados aquí serán auditados y registrados.
              Ten cuidado al crear, modificar o eliminar usuarios.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;

