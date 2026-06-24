import { useState, useEffect } from 'react';
import { obtenerPacientes, obtenerListaEspera } from '../api/gestionPacientesApi';
import { obtenerNotificacionesPendientes } from '../api/notificacionesApi';
import {
  BarChart3, Users, ClipboardList, Bell, RefreshCw, TrendingUp,
  Activity, AlertTriangle, ArrowUpRight, ArrowDownRight
} from 'lucide-react';

function Reportes() {
  const [stats, setStats] = useState({
    totalPacientes: 0,
    listaEspera: [],
    notificaciones: 0,
  });
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState('');

  const cargarDatos = async () => {
    setCargando(true);
    setError('');

    try {
      const [pacientes, lista, notifs] = await Promise.allSettled([
        obtenerPacientes(),
        obtenerListaEspera(),
        obtenerNotificacionesPendientes(),
      ]);

      setStats({
        totalPacientes: pacientes.status === 'fulfilled' ? (Array.isArray(pacientes.value) ? pacientes.value.length : 0) : 0,
        listaEspera: lista.status === 'fulfilled' ? (Array.isArray(lista.value) ? lista.value : []) : [],
        notificaciones: notifs.status === 'fulfilled' ? (Array.isArray(notifs.value) ? notifs.value.length : 0) : 0,
      });
    } catch (err) {
      setError('No fue posible cargar los datos de reportes. Verifica que los servicios backend estén activos.');
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, []);

  // Calcular estadísticas de lista de espera por gravedad
  const gravedadCount = stats.listaEspera.reduce(
    (acc, item) => {
      const g = item.gravedad || 'NORMAL';
      acc[g] = (acc[g] || 0) + 1;
      return acc;
    },
    {}
  );

  const estadoCount = stats.listaEspera.reduce(
    (acc, item) => {
      const e = item.estado || 'PENDIENTE';
      acc[e] = (acc[e] || 0) + 1;
      return acc;
    },
    {}
  );

  const totalListaEspera = stats.listaEspera.length;

  // SVG donut chart helper
  const DonutSegment = ({ percentage, color, startAngle = 0 }) => {
    const radius = 40;
    const circumference = 2 * Math.PI * radius;
    const dashLength = (percentage / 100) * circumference;
    const dashGap = circumference - dashLength;
    const rotation = startAngle - 90;

    return (
      <circle
        cx="50"
        cy="50"
        r={radius}
        fill="none"
        stroke={color}
        strokeWidth="12"
        strokeDasharray={`${dashLength} ${dashGap}`}
        transform={`rotate(${rotation} 50 50)`}
        strokeLinecap="round"
        className="transition-all duration-1000 ease-out"
      />
    );
  };

  // Calculate donut segments
  const gravedadColors = { ALTA: '#ef4444', MEDIA: '#f59e0b', BAJA: '#10b981', NORMAL: '#3b82f6' };
  const gravedadEntries = Object.entries(gravedadCount);
  let cumulativeAngle = 0;
  const donutSegments = gravedadEntries.map(([key, count]) => {
    const percentage = totalListaEspera > 0 ? (count / totalListaEspera) * 100 : 0;
    const segment = { key, count, percentage, color: gravedadColors[key] || '#94a3b8', startAngle: cumulativeAngle };
    cumulativeAngle += (percentage / 100) * 360;
    return segment;
  });

  // Bar chart data
  const estadoColors = { PENDIENTE: '#ef4444', ATENDIDO: '#10b981', CANCELADO: '#94a3b8' };
  const estadoEntries = Object.entries(estadoCount);
  const maxEstado = Math.max(...Object.values(estadoCount), 1);

  return (
    <div className="space-y-6">
      {/* Header */}
      <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-primary-container text-primary rounded-xl">
            <BarChart3 className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-2xl font-bold text-on-surface tracking-tight">Reportes</h2>
            <p className="text-sm text-on-surface-variant mt-1">
              Métricas operativas y estadísticas del sistema de salud en tiempo real.
            </p>
          </div>
        </div>
        <button
          onClick={cargarDatos}
          disabled={cargando}
          className="inline-flex items-center gap-2 px-4 py-2 border border-outline-variant bg-surface hover:bg-surface-container-low text-on-surface-variant font-semibold rounded-lg text-sm shadow-xs transition-colors cursor-pointer"
        >
          <RefreshCw className={`w-4 h-4 ${cargando ? 'animate-spin' : ''}`} />
          <span>Actualizar</span>
        </button>
      </header>

      {error && (
        <div className="bg-error-container border border-error/10 text-on-error-container p-4 rounded-xl text-sm font-semibold flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Pacientes */}
        <div className="bg-surface-container-lowest border border-outline-variant rounded-xl p-5 hover:shadow-md transition-shadow">
          <div className="flex items-start justify-between mb-3">
            <div className="p-2 bg-blue-50 dark:bg-blue-950/30 rounded-lg">
              <Users className="w-5 h-5 text-blue-600 dark:text-blue-400" />
            </div>
            <span className="text-xs font-bold text-emerald-600 flex items-center gap-0.5">
              <ArrowUpRight className="w-3 h-3" /> Activo
            </span>
          </div>
          <p className="text-3xl font-extrabold text-on-surface">{cargando ? '...' : stats.totalPacientes}</p>
          <p className="text-xs text-on-surface-variant font-medium mt-1">Pacientes registrados</p>
        </div>

        {/* Lista de Espera */}
        <div className="bg-surface-container-lowest border border-outline-variant rounded-xl p-5 hover:shadow-md transition-shadow">
          <div className="flex items-start justify-between mb-3">
            <div className="p-2 bg-amber-50 dark:bg-amber-950/30 rounded-lg">
              <ClipboardList className="w-5 h-5 text-amber-600 dark:text-amber-400" />
            </div>
            {totalListaEspera > 0 && (
              <span className="text-xs font-bold text-amber-600 flex items-center gap-0.5">
                <Activity className="w-3 h-3" /> En proceso
              </span>
            )}
          </div>
          <p className="text-3xl font-extrabold text-on-surface">{cargando ? '...' : totalListaEspera}</p>
          <p className="text-xs text-on-surface-variant font-medium mt-1">En lista de espera</p>
        </div>

        {/* Notificaciones */}
        <div className="bg-surface-container-lowest border border-outline-variant rounded-xl p-5 hover:shadow-md transition-shadow">
          <div className="flex items-start justify-between mb-3">
            <div className="p-2 bg-rose-50 dark:bg-rose-950/30 rounded-lg">
              <Bell className="w-5 h-5 text-rose-600 dark:text-rose-400" />
            </div>
            {stats.notificaciones > 0 && (
              <span className="text-xs font-bold text-rose-600 flex items-center gap-0.5">
                <AlertTriangle className="w-3 h-3" /> Pendientes
              </span>
            )}
          </div>
          <p className="text-3xl font-extrabold text-on-surface">{cargando ? '...' : stats.notificaciones}</p>
          <p className="text-xs text-on-surface-variant font-medium mt-1">Notificaciones pendientes</p>
        </div>

        {/* Tasa de atención */}
        <div className="bg-surface-container-lowest border border-outline-variant rounded-xl p-5 hover:shadow-md transition-shadow">
          <div className="flex items-start justify-between mb-3">
            <div className="p-2 bg-emerald-50 dark:bg-emerald-950/30 rounded-lg">
              <TrendingUp className="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
            </div>
          </div>
          <p className="text-3xl font-extrabold text-on-surface">
            {cargando
              ? '...'
              : totalListaEspera > 0
              ? `${Math.round(((estadoCount.ATENDIDO || 0) / totalListaEspera) * 100)}%`
              : '0%'}
          </p>
          <p className="text-xs text-on-surface-variant font-medium mt-1">Tasa de atención</p>
        </div>
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Donut: Distribución por gravedad */}
        <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl p-6 shadow-xs">
          <h3 className="font-bold text-on-surface text-sm mb-4 flex items-center gap-2">
            <Activity className="w-4 h-4 text-primary" />
            Distribución por Gravedad
          </h3>

          <div className="flex items-center gap-8">
            <div className="relative w-32 h-32 shrink-0">
              <svg viewBox="0 0 100 100" className="w-full h-full -rotate-90">
                {/* Background circle */}
                <circle cx="50" cy="50" r="40" fill="none" stroke="currentColor" strokeWidth="12" className="text-surface-container" />
                {donutSegments.map((seg) => (
                  <DonutSegment key={seg.key} percentage={seg.percentage} color={seg.color} startAngle={seg.startAngle} />
                ))}
              </svg>
              <div className="absolute inset-0 flex items-center justify-center">
                <div className="text-center">
                  <p className="text-lg font-extrabold text-on-surface">{totalListaEspera}</p>
                  <p className="text-[9px] text-on-surface-variant font-bold uppercase tracking-wider">Total</p>
                </div>
              </div>
            </div>

            <div className="flex-1 space-y-2.5">
              {[
                { key: 'ALTA', label: 'Alta', color: 'bg-rose-500' },
                { key: 'MEDIA', label: 'Media', color: 'bg-amber-500' },
                { key: 'BAJA', label: 'Baja', color: 'bg-emerald-500' },
                { key: 'NORMAL', label: 'Normal', color: 'bg-blue-500' },
              ].map(({ key, label, color }) => (
                <div key={key} className="flex items-center justify-between text-xs">
                  <div className="flex items-center gap-2">
                    <div className={`w-2.5 h-2.5 rounded-full ${color}`} />
                    <span className="font-semibold text-on-surface">{label}</span>
                  </div>
                  <span className="font-bold text-on-surface-variant">{gravedadCount[key] || 0}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Bar Chart: Estado de pacientes */}
        <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl p-6 shadow-xs">
          <h3 className="font-bold text-on-surface text-sm mb-4 flex items-center gap-2">
            <BarChart3 className="w-4 h-4 text-primary" />
            Estado de Pacientes en Espera
          </h3>

          <div className="space-y-4">
            {[
              { key: 'PENDIENTE', label: 'Pendientes', color: 'bg-rose-500', textColor: 'text-rose-600' },
              { key: 'ATENDIDO', label: 'Atendidos', color: 'bg-emerald-500', textColor: 'text-emerald-600' },
              { key: 'CANCELADO', label: 'Cancelados', color: 'bg-slate-400', textColor: 'text-slate-500' },
            ].map(({ key, label, color, textColor }) => {
              const count = estadoCount[key] || 0;
              const percentage = totalListaEspera > 0 ? (count / maxEstado) * 100 : 0;
              return (
                <div key={key} className="space-y-1.5">
                  <div className="flex justify-between text-xs">
                    <span className="font-semibold text-on-surface">{label}</span>
                    <span className={`font-extrabold ${textColor}`}>{count}</span>
                  </div>
                  <div className="h-3 bg-surface-container rounded-full overflow-hidden">
                    <div
                      className={`h-full ${color} rounded-full transition-all duration-1000 ease-out`}
                      style={{ width: `${percentage}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>

          {totalListaEspera === 0 && !cargando && (
            <div className="mt-4 text-center py-6 text-on-surface-variant/60">
              <ClipboardList className="w-8 h-8 mx-auto mb-2 opacity-40" />
              <p className="text-xs font-medium">No hay datos de lista de espera disponibles</p>
            </div>
          )}
        </div>
      </div>

      {/* Summary table */}
      <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs overflow-hidden">
        <div className="p-5 border-b border-outline-variant bg-surface-container-low flex justify-between items-center">
          <h3 className="font-bold text-on-surface text-sm">Resumen General del Sistema</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead>
              <tr className="bg-surface-container-low text-on-surface-variant text-xs uppercase tracking-wider border-b border-outline-variant">
                <th className="py-3 px-6 font-bold">Métrica</th>
                <th className="py-3 px-6 font-bold">Valor</th>
                <th className="py-3 px-6 font-bold">Estado</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-outline-variant text-sm">
              <tr className="hover:bg-surface-container-low transition-colors">
                <td className="py-3 px-6 font-semibold text-on-surface">Pacientes registrados</td>
                <td className="py-3 px-6 font-bold text-on-surface">{stats.totalPacientes}</td>
                <td className="py-3 px-6">
                  <span className="text-[10px] font-extrabold bg-emerald-50 text-emerald-700 px-2 py-0.5 rounded-md border border-emerald-100">
                    ACTIVO
                  </span>
                </td>
              </tr>
              <tr className="hover:bg-surface-container-low transition-colors">
                <td className="py-3 px-6 font-semibold text-on-surface">Pacientes en espera</td>
                <td className="py-3 px-6 font-bold text-on-surface">{totalListaEspera}</td>
                <td className="py-3 px-6">
                  <span className={`text-[10px] font-extrabold px-2 py-0.5 rounded-md border ${
                    totalListaEspera > 0
                      ? 'bg-amber-50 text-amber-700 border-amber-100'
                      : 'bg-emerald-50 text-emerald-700 border-emerald-100'
                  }`}>
                    {totalListaEspera > 0 ? 'EN PROCESO' : 'VACÍA'}
                  </span>
                </td>
              </tr>
              <tr className="hover:bg-surface-container-low transition-colors">
                <td className="py-3 px-6 font-semibold text-on-surface">Notificaciones pendientes</td>
                <td className="py-3 px-6 font-bold text-on-surface">{stats.notificaciones}</td>
                <td className="py-3 px-6">
                  <span className={`text-[10px] font-extrabold px-2 py-0.5 rounded-md border ${
                    stats.notificaciones > 0
                      ? 'bg-rose-50 text-rose-700 border-rose-100'
                      : 'bg-emerald-50 text-emerald-700 border-emerald-100'
                  }`}>
                    {stats.notificaciones > 0 ? 'PENDIENTE' : 'AL DÍA'}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default Reportes;
