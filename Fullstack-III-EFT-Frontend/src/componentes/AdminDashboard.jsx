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
      <div className="bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg p-6">
        <h2 className="text-lg font-bold text-slate-900 dark:text-white mb-4">Información de Administrador</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <p className="text-sm text-slate-600 dark:text-slate-400">Usuario</p>
            <p className="text-lg font-semibold text-slate-900 dark:text-white mt-1">{user?.username}</p>
          </div>
          <div>
            <p className="text-sm text-slate-600 dark:text-slate-400">Rol</p>
            <p className="text-lg font-semibold text-slate-900 dark:text-white mt-1">Administrador del Sistema</p>
          </div>
        </div>
      </div>

      {/* Aviso Importante */}
      <div className="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-6">
        <h3 className="font-bold text-yellow-900 dark:text-yellow-300 mb-2">⚠️ Acceso Restringido</h3>
        <p className="text-sm text-yellow-800 dark:text-yellow-400">
          Este panel solo es accesible para administradores del sistema. Todos los cambios realizados aquí serán auditados y registrados.
          Ten cuidado al crear, modificar o eliminar usuarios.
        </p>
      </div>
    </div>
  );
}

export default AdminDashboard;

