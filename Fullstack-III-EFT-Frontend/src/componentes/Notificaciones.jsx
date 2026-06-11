import { useEffect, useState } from 'react';
import {
  enviarNotificacion as enviarNotificacionApi,
  obtenerNotificacionesPendientes,
} from '../api/notificacionesApi';
import { Bell, RefreshCw, Send, Check, AlertTriangle, AlertCircle } from 'lucide-react';

function Notificaciones() {
  const [notificaciones, setNotificaciones] = useState([]);
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState('');
  const [error, setError] = useState('');

  const cargarNotificaciones = async () => {
    setCargando(true);
    setMensaje('');
    setError('');

    try {
      const datos = await obtenerNotificacionesPendientes();
      setNotificaciones(Array.isArray(datos) ? datos : []);
    } catch (errorCapturado) {
      setError(errorCapturado.message || 'No fue posible cargar las notificaciones');
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void cargarNotificaciones();
    }, 0);

    return () => window.clearTimeout(timeoutId);
  }, []);

  const manejarEnviarNotificacion = async (id) => {
    setCargando(true);
    setMensaje('');
    setError('');

    try {
      await enviarNotificacionApi(id);
      setNotificaciones((actuales) => actuales.filter((n) => n.id !== id));
      setMensaje(`Notificación ${id} enviada correctamente.`);
    } catch (errorCapturado) {
      setError(errorCapturado.message || 'No fue posible enviar la notificación');
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-blue-50 text-blue-600 rounded-xl">
            <Bell className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Notificaciones Pendientes</h2>
            <p className="text-sm text-slate-500 mt-1">
              Despacho manual y envío de alertas y notificaciones clínicas a los pacientes.
            </p>
          </div>
        </div>
        <button
          type="button"
          onClick={cargarNotificaciones}
          disabled={cargando}
          className="inline-flex items-center gap-2 px-4 py-2 border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-semibold rounded-lg text-sm shadow-xs transition-colors cursor-pointer"
        >
          <RefreshCw className={`w-4 h-4 ${cargando ? 'animate-spin' : ''}`} />
          <span>Actualizar</span>
        </button>
      </header>

      {/* Alert Feedbacks */}
      <div className="space-y-3">
        {mensaje && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 p-4 rounded-xl text-sm font-semibold flex items-center gap-2 shadow-2xs">
            <Check className="w-4 h-4 text-emerald-600" />
            <span>{mensaje}</span>
          </div>
        )}
        {error && (
          <div className="bg-rose-50 border border-rose-200 text-rose-800 p-4 rounded-xl text-sm font-semibold flex items-center gap-2 shadow-2xs">
            <AlertTriangle className="w-4 h-4 text-rose-600" />
            <span>{error}</span>
          </div>
        )}
      </div>

      {/* Main Container */}
      <div className="bg-white border border-slate-100 rounded-2xl shadow-xs p-6">
        {cargando ? (
          <div className="py-12 text-center text-slate-400">
            <RefreshCw className="w-8 h-8 animate-spin mx-auto text-blue-500 mb-3" />
            <p className="font-semibold text-sm">Cargando notificaciones…</p>
          </div>
        ) : notificaciones.length === 0 ? (
          <div className="py-12 text-center text-slate-450 border border-dashed border-slate-200 rounded-xl bg-slate-50/50">
            <AlertCircle className="w-10 h-10 text-slate-300 mx-auto mb-2" />
            <p className="font-semibold text-slate-550 text-slate-500 text-sm">No hay notificaciones pendientes</p>
          </div>
        ) : (
          <ul className="grid grid-cols-1 md:grid-cols-2 gap-4 list-none p-0 m-0">
            {notificaciones.map(n => (
              <li 
                key={n.id} 
                className="bg-white border border-slate-100 rounded-xl p-5 shadow-2xs hover:shadow-sm transition-all hover:border-blue-300 flex flex-col justify-between gap-4 text-left"
              >
                <div className="space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="font-mono text-xs font-bold text-slate-800 bg-slate-100 px-2 py-0.5 rounded border border-slate-200/50">
                      ID: {n.id}
                    </span>
                    <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-50 text-amber-700 border border-amber-100">
                      Estado: {n.estado ?? 'N/A'}
                    </span>
                  </div>

                  <div className="text-xs space-y-1 text-slate-600">
                    <p className="font-semibold text-slate-700">Paciente: {n.pacienteId ?? 'N/A'}</p>
                    <p className="font-semibold text-slate-700">Tipo: {n.tipo ?? 'N/A'}</p>
                    <div className="bg-slate-50/50 p-2.5 rounded-lg border border-slate-150 border-slate-200/50 mt-2">
                      <p className="text-slate-500 font-medium">Mensaje:</p>
                      <p className="text-slate-700 font-semibold italic mt-0.5">"{n.mensaje}"</p>
                    </div>
                  </div>
                </div>

                <div className="pt-3 border-t border-slate-50 flex justify-end">
                  <button
                    onClick={() => manejarEnviarNotificacion(n.id)}
                    disabled={cargando}
                    className="inline-flex items-center gap-1.5 py-1.5 px-4 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-bold transition-all shadow-xs shadow-blue-100 cursor-pointer"
                  >
                    <Send className="w-3 h-3" />
                    <span>Enviar</span>
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

export default Notificaciones;
