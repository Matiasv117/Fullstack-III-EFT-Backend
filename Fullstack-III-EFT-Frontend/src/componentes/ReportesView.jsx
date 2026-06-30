import { useEffect, useState } from 'react';
import { Users, Clock, AlertTriangle, Activity, ClipboardList } from 'lucide-react';
import reportesApi from '../api/reportesApi';

const ReportesView = () => {
  const [metricas, setMetricas] = useState(null);
  const [totalPacientes, setTotalPacientes] = useState(null);
  const [eventos, setEventos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [tiempoPromedio, setTiempoPromedio] = useState(null);

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    setCargando(true);
    setError('');
    try {
      const [metricasData, pacientesData, eventosData] = await Promise.all([
        reportesApi.obtenerMetricasListaEspera().catch(() => null),
        reportesApi.listarPacientes().catch(() => null),
        reportesApi.listarEventosAuditoria().catch(() => null),
      ]);

      setMetricas(metricasData);
      setTotalPacientes(Array.isArray(pacientesData) ? pacientesData.length : null);
      setEventos(Array.isArray(eventosData) ? eventosData : []);

      if (metricasData) {
        const total = (metricasData.pacientesGravedadAlta || 0) +
          (metricasData.pacientesGravedadMedia || 0) +
          (metricasData.pacientesGravedadBaja || 0);
        const estimado = total > 0 ? { total, conPrioridad: metricasData.pacientesGravedadAlta } : null;
        setTiempoPromedio(estimado);
      }
    } catch (err) {
      setError('Error al cargar datos de reportes');
    } finally {
      setCargando(false);
    }
  };

  if (cargando) {
    return (
      <div className="ml-[260px] pt-24 p-gutter min-h-screen">
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        </div>
      </div>
    );
  }

  const totalPendientes = metricas?.totalPendientes ?? 0;
  const gravedadAlta = metricas?.pacientesGravedadAlta ?? 0;
  const gravedadMedia = metricas?.pacientesGravedadMedia ?? 0;
  const gravedadBaja = metricas?.pacientesGravedadBaja ?? 0;

  return (
    <div className="ml-[260px] pt-24 p-gutter min-h-screen">
      <h1 className="text-2xl font-bold mb-6">Reportes y Auditoría</h1>

      {error && (
        <div className="bg-red-50 text-red-700 p-4 rounded-lg mb-4">{error}</div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <div className="bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-lg p-5 shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm opacity-90">Total Pacientes</p>
              <p className="text-3xl font-bold mt-1">{totalPacientes ?? '—'}</p>
            </div>
            <Users className="w-10 h-10 opacity-30" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-amber-500 to-amber-600 text-white rounded-lg p-5 shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm opacity-90">En Lista de Espera</p>
              <p className="text-3xl font-bold mt-1">{totalPendientes}</p>
            </div>
            <ClipboardList className="w-10 h-10 opacity-30" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-red-500 to-red-600 text-white rounded-lg p-5 shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm opacity-90">Prioridad Alta</p>
              <p className="text-3xl font-bold mt-1">{gravedadAlta}</p>
            </div>
            <AlertTriangle className="w-10 h-10 opacity-30" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-emerald-500 to-emerald-600 text-white rounded-lg p-5 shadow-lg">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm opacity-90">Total en Espera</p>
              <p className="text-3xl font-bold mt-1">
                {tiempoPromedio ? tiempoPromedio.total : '—'}
              </p>
            </div>
            <Clock className="w-10 h-10 opacity-30" />
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <div className="bg-white dark:bg-surface-dim rounded-xl shadow-sm border border-outline-variant p-5">
          <h2 className="text-lg font-bold mb-4 flex items-center gap-2">
            <Activity className="w-5 h-5 text-primary" />
            Distribución por Gravedad
          </h2>
          {totalPendientes > 0 ? (
            <div className="space-y-3">
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-red-600 font-medium">Alta</span>
                  <span>{gravedadAlta} pacientes ({Math.round(gravedadAlta / totalPendientes * 100)}%)</span>
                </div>
                <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2.5">
                  <div className="bg-red-500 h-2.5 rounded-full" style={{ width: `${gravedadAlta / totalPendientes * 100}%` }}></div>
                </div>
              </div>
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-amber-600 font-medium">Media</span>
                  <span>{gravedadMedia} pacientes ({Math.round(gravedadMedia / totalPendientes * 100)}%)</span>
                </div>
                <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2.5">
                  <div className="bg-amber-500 h-2.5 rounded-full" style={{ width: `${gravedadMedia / totalPendientes * 100}%` }}></div>
                </div>
              </div>
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-emerald-600 font-medium">Baja</span>
                  <span>{gravedadBaja} pacientes ({Math.round(gravedadBaja / totalPendientes * 100)}%)</span>
                </div>
                <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2.5">
                  <div className="bg-emerald-500 h-2.5 rounded-full" style={{ width: `${gravedadBaja / totalPendientes * 100}%` }}></div>
                </div>
              </div>
            </div>
          ) : (
            <p className="text-on-surface-variant text-sm">No hay pacientes en lista de espera</p>
          )}
        </div>

        <div className="bg-white dark:bg-surface-dim rounded-xl shadow-sm border border-outline-variant p-5">
          <h2 className="text-lg font-bold mb-4 flex items-center gap-2">
            <Users className="w-5 h-5 text-primary" />
            Resumen del Sistema
          </h2>
          <div className="space-y-3 text-sm">
            <div className="flex justify-between py-2 border-b border-outline-variant">
              <span className="text-on-surface-variant">Total pacientes registrados</span>
              <span className="font-semibold">{totalPacientes ?? '—'}</span>
            </div>
            <div className="flex justify-between py-2 border-b border-outline-variant">
              <span className="text-on-surface-variant">Pendientes en lista de espera</span>
              <span className="font-semibold">{totalPendientes}</span>
            </div>
            <div className="flex justify-between py-2 border-b border-outline-variant">
              <span className="text-on-surface-variant">Prioridad alta</span>
              <span className="font-semibold text-red-600">{gravedadAlta}</span>
            </div>
            <div className="flex justify-between py-2 border-b border-outline-variant">
              <span className="text-on-surface-variant">Prioridad media</span>
              <span className="font-semibold text-amber-600">{gravedadMedia}</span>
            </div>
            <div className="flex justify-between py-2">
              <span className="text-on-surface-variant">Prioridad baja</span>
              <span className="font-semibold text-emerald-600">{gravedadBaja}</span>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-white dark:bg-surface-dim rounded-xl shadow-sm border border-outline-variant p-5">
        <h2 className="text-lg font-bold mb-4 flex items-center gap-2">
          <Activity className="w-5 h-5 text-primary" />
          Auditoría Reciente
        </h2>
        {eventos.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-outline-variant text-left">
                  <th className="pb-2 font-semibold text-on-surface-variant">Fecha</th>
                  <th className="pb-2 font-semibold text-on-surface-variant">Usuario</th>
                  <th className="pb-2 font-semibold text-on-surface-variant">Acción</th>
                  <th className="pb-2 font-semibold text-on-surface-variant">Detalle</th>
                </tr>
              </thead>
              <tbody>
                {eventos.slice(0, 20).map((ev) => (
                  <tr key={ev.id} className="border-b border-outline-variant/50 hover:bg-surface-container-high transition-colors">
                    <td className="py-2 pr-4 text-on-surface-variant whitespace-nowrap">
                      {ev.timestamp ? new Date(ev.timestamp).toLocaleString('es-CL') : '—'}
                    </td>
                    <td className="py-2 pr-4 font-medium">{ev.username}</td>
                    <td className="py-2 pr-4">
                      <span className="px-2 py-0.5 bg-primary/10 text-primary rounded text-xs font-medium">
                        {ev.action}
                      </span>
                    </td>
                    <td className="py-2 text-on-surface-variant max-w-xs truncate">{ev.details}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {eventos.length > 20 && (
              <p className="text-xs text-on-surface-variant mt-2">Mostrando los últimos 20 eventos</p>
            )}
          </div>
        ) : (
          <p className="text-on-surface-variant text-sm">No hay eventos de auditoría registrados</p>
        )}
      </div>
    </div>
  );
};

export default ReportesView;
