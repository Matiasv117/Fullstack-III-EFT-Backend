import { useState, useEffect, useCallback } from 'react';
import { checkAllServices } from '../api/connectionApi';
import { RefreshCw, CheckCircle, XCircle, Wifi, WifiOff, Database } from 'lucide-react';

function ConnectionStatus({ compact = false }) {
  const [services, setServices] = useState([]);
  const [checking, setChecking] = useState(false);
  const [lastChecked, setLastChecked] = useState(null);

  const checkConnections = useCallback(async () => {
    setChecking(true);
    try {
      const results = await checkAllServices();
      setServices(results);
      setLastChecked(new Date());
    } catch {
      // Si falla todo, marcar todo como desconectado
      setServices([
        { status: 'disconnected', name: 'Gestión de Pacientes', port: '8080' },
        { status: 'disconnected', name: 'Notificaciones', port: '8080' },
        { status: 'disconnected', name: 'Optimización', port: '8080' },
        { status: 'disconnected', name: 'Autenticación', port: '8097' },
      ]);
      setLastChecked(new Date());
    } finally {
      setChecking(false);
    }
  }, []);

  useEffect(() => {
    checkConnections();
    const interval = setInterval(checkConnections, 30000);
    return () => clearInterval(interval);
  }, [checkConnections]);

  const connectedCount = services.filter((s) => s.status === 'connected').length;
  const totalCount = services.length;
  const allConnected = connectedCount === totalCount && totalCount > 0;
  const noneConnected = connectedCount === 0 && totalCount > 0;

  const overallStatusColor = allConnected
    ? 'text-emerald-600 dark:text-emerald-400'
    : noneConnected
    ? 'text-rose-600 dark:text-rose-400'
    : 'text-amber-600 dark:text-amber-400';

  const overallStatusBg = allConnected
    ? 'bg-emerald-50 dark:bg-emerald-950/30 border-emerald-200 dark:border-emerald-800'
    : noneConnected
    ? 'bg-rose-50 dark:bg-rose-950/30 border-rose-200 dark:border-rose-800'
    : 'bg-amber-50 dark:bg-amber-950/30 border-amber-200 dark:border-amber-800';

  if (compact) {
    return (
      <div className={`flex items-center gap-2 px-3 py-1.5 rounded-full border text-xs font-bold ${overallStatusBg} ${overallStatusColor}`}>
        {checking ? (
          <RefreshCw className="w-3.5 h-3.5 animate-spin" />
        ) : allConnected ? (
          <Wifi className="w-3.5 h-3.5" />
        ) : (
          <WifiOff className="w-3.5 h-3.5" />
        )}
        <span>{connectedCount}/{totalCount} servicios</span>
      </div>
    );
  }

  return (
    <div className="bg-surface-container-lowest dark:bg-surface-container border border-outline-variant rounded-2xl shadow-xs overflow-hidden">
      {/* Header */}
      <div className="p-5 border-b border-outline-variant flex justify-between items-center bg-surface-container-low dark:bg-surface-container">
        <div className="flex items-center gap-3">
          <div className={`p-2 rounded-lg ${overallStatusBg}`}>
            <Database className={`w-5 h-5 ${overallStatusColor}`} />
          </div>
          <div>
            <h3 className="font-bold text-on-surface text-sm">Estado de Conexión</h3>
            <p className="text-[10px] text-on-surface-variant font-medium">
              {lastChecked
                ? `Última verificación: ${lastChecked.toLocaleTimeString('es-CL')}`
                : 'Verificando...'}
            </p>
          </div>
        </div>
        <button
          onClick={checkConnections}
          disabled={checking}
          className="p-2 rounded-lg border border-outline-variant hover:bg-surface-container-high transition-colors cursor-pointer"
          title="Verificar conexiones"
        >
          <RefreshCw className={`w-4 h-4 text-on-surface-variant ${checking ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {/* Overall status banner */}
      <div className={`px-5 py-3 border-b border-outline-variant/50 ${overallStatusBg}`}>
        <div className="flex items-center gap-2">
          {allConnected ? (
            <CheckCircle className={`w-4 h-4 ${overallStatusColor}`} />
          ) : (
            <XCircle className={`w-4 h-4 ${overallStatusColor}`} />
          )}
          <span className={`text-xs font-bold ${overallStatusColor}`}>
            {checking
              ? 'Verificando conexiones...'
              : allConnected
              ? 'Todos los servicios conectados'
              : noneConnected
              ? 'Sin conexión al backend'
              : `${connectedCount} de ${totalCount} servicios conectados`}
          </span>
        </div>
      </div>

      {/* Services list */}
      <div className="p-4 space-y-2">
        {services.map((service, index) => {
          const isConnected = service.status === 'connected';
          return (
            <div
              key={index}
              className={`flex items-center justify-between p-3 rounded-xl border transition-all ${
                isConnected
                  ? 'bg-emerald-50/50 dark:bg-emerald-950/20 border-emerald-100 dark:border-emerald-900/30'
                  : 'bg-rose-50/50 dark:bg-rose-950/20 border-rose-100 dark:border-rose-900/30'
              }`}
            >
              <div className="flex items-center gap-3">
                <div
                  className={`w-2.5 h-2.5 rounded-full ${
                    checking
                      ? 'bg-amber-400 animate-pulse'
                      : isConnected
                      ? 'bg-emerald-500'
                      : 'bg-rose-500'
                  }`}
                />
                <div>
                  <p className="text-xs font-bold text-on-surface">{service.name}</p>
                  <p className="text-[10px] text-on-surface-variant font-mono">Puerto: {service.port}</p>
                </div>
              </div>
              <span
                className={`text-[10px] font-extrabold uppercase tracking-wider px-2 py-0.5 rounded-md ${
                  isConnected
                    ? 'text-emerald-700 dark:text-emerald-300 bg-emerald-100 dark:bg-emerald-900/40'
                    : 'text-rose-700 dark:text-rose-300 bg-rose-100 dark:bg-rose-900/40'
                }`}
              >
                {checking ? 'Verificando' : isConnected ? 'Conectado' : 'Desconectado'}
              </span>
            </div>
          );
        })}

        {services.length === 0 && (
          <div className="py-6 text-center">
            <RefreshCw className="w-6 h-6 animate-spin mx-auto text-primary mb-2" />
            <p className="text-xs text-on-surface-variant font-medium">Verificando servicios...</p>
          </div>
        )}
      </div>

      {/* Footer hint */}
      {noneConnected && (
        <div className="px-5 pb-4">
          <div className="bg-surface-container-low dark:bg-surface-container p-3 rounded-lg border border-outline-variant text-[10px] text-on-surface-variant leading-relaxed">
            <strong className="text-on-surface block mb-1">💡 ¿No se conectan los servicios?</strong>
            Asegúrate de tener los contenedores Docker corriendo con{' '}
            <code className="bg-surface-container-high px-1 py-0.5 rounded font-mono text-primary">
              docker-compose up -d
            </code>{' '}
            y los microservicios Java iniciados.
          </div>
        </div>
      )}
    </div>
  );
}

export default ConnectionStatus;
