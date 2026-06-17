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
          <div className="p-2.5 bg-primary-container text-primary rounded-xl">
            <Zap className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-2xl font-bold text-on-surface tracking-tight">Notificaciones Pendientes</h2>
            <p className="text-sm text-on-surface-variant mt-1">
              Despacho manual y envío de alertas y notificaciones clínicas a los pacientes.
            </p>
          </div>
        </div>
        <button
          type="button"
          onClick={cargarNotificaciones}
          disabled={cargando}
          className="inline-flex items-center gap-2 px-4 py-2 border border-outline-variant bg-surface hover:bg-surface-container-low text-on-surface-variant font-semibold rounded-lg text-sm shadow-xs transition-colors cursor-pointer"
        >
          <RefreshCw className={`w-4 h-4 ${cargando ? 'animate-spin' : ''}`} />
          <span>Actualizar</span>
        </button>
      </header>

      {/* Alert Feedbacks */}
      <div className="space-y-3">
        {mensaje && (
          <div className="bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-100 dark:border-emerald-900/50 text-emerald-800 dark:text-emerald-300 p-4 rounded-xl text-sm font-semibold flex items-center gap-2 shadow-2xs">
            <Check className="w-4 h-4 text-emerald-600" />
            <span>{mensaje}</span>
          </div>
        )}
        {error && (
          <div className="bg-error-container border border-error/10 text-on-error-container p-4 rounded-xl text-sm font-semibold flex items-center gap-2 shadow-2xs">
            <AlertTriangle className="w-4 h-4 text-rose-600" />
            <span>{error}</span>
          </div>
        )}
      </div>

      {/* Main Container */}
      <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs p-6">
        {cargando ? (
          <div className="py-12 text-center text-on-surface-variant/70">
            <RefreshCw className="w-8 h-8 animate-spin mx-auto text-primary mb-3" />
            <p className="font-semibold text-sm">Cargando notificaciones…</p>
          </div>
        ) : notificaciones.length === 0 ? (
          <div className="py-12 text-center text-on-surface-variant border border-dashed border-outline-variant rounded-xl bg-surface-container-low">
            <Bell className="w-10 h-10 text-on-surface-variant/50 mx-auto mb-2" />
            <p className="font-semibold text-on-surface-variant text-sm">No hay notificaciones pendientes</p>
          </div>
        ) : (
          <ul className="grid grid-cols-1 md:grid-cols-2 gap-4 list-none p-0 m-0">
            {notificaciones.map(n => (
              <li 
                key={n.id} 
                className="bg-surface-container-lowest border border-outline-variant rounded-xl p-5 shadow-sm hover:shadow-md transition-all hover:border-primary/50 flex flex-col justify-between gap-4 text-left"
              >
                <div className="space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="font-mono text-xs font-bold text-on-surface-variant bg-surface-container-low px-2 py-0.5 rounded border border-outline-variant">
                      ID: {n.id}
                    </span>
                    <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-50 dark:bg-amber-950/30 text-amber-700 dark:text-amber-300 border border-amber-100 dark:border-amber-900/50">
                      Estado: {n.estado ?? 'N/A'}
                    </span>
                  </div>

                  <div className="text-xs space-y-1 text-on-surface-variant">
                    <p className="font-semibold text-on-surface">Paciente: {n.pacienteId ?? 'N/A'}</p>
                    <p className="font-semibold text-on-surface">Tipo: {n.tipo ?? 'N/A'}</p>
                    <div className="bg-surface-container-low p-2.5 rounded-lg border border-outline-variant mt-2">
                      <p className="text-on-surface-variant font-medium">Mensaje:</p>
                      <p className="text-on-surface font-semibold italic mt-0.5">"{n.mensaje}"</p>
                    </div>
                  </div>
                </div>

                <div className="pt-3 border-t border-outline-variant/30 flex justify-end">
                  <button
                    onClick={() => manejarEnviarNotificacion(n.id)}
                    disabled={cargando}
                    className="inline-flex items-center gap-1.5 py-1.5 px-4 bg-primary hover:bg-primary/95 text-white rounded-lg text-xs font-bold transition-all shadow-xs shadow-primary/10 cursor-pointer"
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
