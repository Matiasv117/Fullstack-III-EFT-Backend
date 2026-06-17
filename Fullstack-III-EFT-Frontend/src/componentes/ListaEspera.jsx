import { useState } from 'react';
import { useListaEspera } from '../hooks/useListaEspera';
import { 
  ClipboardList, RefreshCw, Filter, Trash2, Edit3, 
  AlertCircle, Activity, ShieldAlert
} from 'lucide-react';

function ListaEsperaView({
  listaEspera,
  cargando,
  mensaje,
  error,
  eliminarDeListaEspera,
  actualizarEstado,
  recargarListaEspera,
}) {
  const [filtroGravedad, setFiltroGravedad] = useState('TODOS');
  const [filtroEstado, setFiltroEstado] = useState('TODOS');

  const listaFiltrada = listaEspera.filter((item) => {
    if (filtroGravedad !== 'TODOS' && (item.gravedad || 'NORMAL') !== filtroGravedad) {
      return false;
    }
    if (filtroEstado !== 'TODOS' && (item.estado || 'PENDIENTE') !== filtroEstado) {
      return false;
    }
    return true;
  });

  const gravedadStyles = (gravedad) => {
    const mapa = {
      ALTA: 'bg-rose-50 dark:bg-rose-950/30 text-rose-700 dark:text-rose-300 border border-rose-100 dark:border-rose-900/50',
      MEDIA: 'bg-amber-50 dark:bg-amber-950/30 text-amber-700 dark:text-amber-300 border border-amber-100 dark:border-amber-900/50',
      BAJA: 'bg-emerald-50 dark:bg-emerald-950/30 text-emerald-700 dark:text-emerald-300 border border-emerald-100 dark:border-emerald-900/50',
      NORMAL: 'bg-blue-50 dark:bg-blue-950/30 text-blue-700 dark:text-blue-300 border border-blue-100 dark:border-blue-900/50',
    };
    return mapa[gravedad] || 'bg-surface-container-low text-on-surface-variant border-outline-variant';
  };

  const estadoStyles = (estado) => {
    const mapa = {
      PENDIENTE: 'bg-rose-600 text-white shadow-xs',
      ATENDIDO: 'bg-emerald-600 text-white shadow-xs',
      CANCELADO: 'bg-outline text-white shadow-xs',
    };
    return mapa[estado] || 'bg-primary text-white shadow-xs';
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-2xl font-bold text-on-surface tracking-tight">Lista de Espera</h2>
          <p className="text-sm text-on-surface-variant mt-1">
            Monitorea y gestiona los pacientes que esperan atención médica en tiempo real.
          </p>
        </div>
        <button
          type="button"
          onClick={recargarListaEspera}
          disabled={cargando}
          className="inline-flex items-center gap-2 px-4 py-2 border border-outline-variant bg-surface hover:bg-surface-container-low text-on-surface-variant font-semibold rounded-lg text-sm shadow-xs transition-colors cursor-pointer"
        >
          <RefreshCw className={`w-4 h-4 ${cargando ? 'animate-spin' : ''}`} />
          <span>{cargando ? 'Actualizando…' : 'Actualizar'}</span>
        </button>
      </header>

      {/* Alert Feedbacks */}
      <div className="space-y-3">
        {mensaje && (
          <div className="bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-100 dark:border-emerald-900/50 text-emerald-800 dark:text-emerald-300 p-4 rounded-xl text-sm font-semibold flex items-center gap-2">
            <span>{mensaje}</span>
          </div>
        )}
        {error && (
          <div className="bg-error-container border border-error/10 text-on-error-container p-4 rounded-xl text-sm font-semibold flex items-center gap-2">
            <span>{error}</span>
          </div>
        )}
      </div>

      <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs p-6 space-y-6">
        
        {/* Toolbar & Filters */}
        <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-4 pb-5 border-b border-outline-variant">
          <div className="flex items-center gap-2">
            <ClipboardList className="w-5 h-5 text-primary shrink-0" />
            <h3 className="font-bold text-on-surface">Pacientes en lista de espera</h3>
          </div>
          
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 w-full lg:w-auto">
            {/* Filter by Gravity */}
            <div className="relative flex-1 sm:flex-initial">
              <label className="block text-[10px] font-bold text-on-surface-variant/70 uppercase tracking-wider mb-1">Gravedad</label>
              <select
                value={filtroGravedad}
                onChange={(e) => setFiltroGravedad(e.target.value)}
                className="appearance-none w-full sm:w-40 bg-surface-container-low text-on-surface border border-outline-variant rounded-lg pl-3 pr-8 py-2 text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary cursor-pointer"
              >
                <option value="TODOS">Todos</option>
                <option value="ALTA">Alta</option>
                <option value="MEDIA">Media</option>
                <option value="BAJA">Baja</option>
                <option value="NORMAL">Normal</option>
              </select>
              <Filter className="absolute right-3 bottom-2.5 text-on-surface-variant w-3.5 h-3.5 pointer-events-none" />
            </div>

            {/* Filter by Status */}
            <div className="relative flex-1 sm:flex-initial">
              <label className="block text-[10px] font-bold text-on-surface-variant/70 uppercase tracking-wider mb-1">Estado</label>
              <select
                value={filtroEstado}
                onChange={(e) => setFiltroEstado(e.target.value)}
                className="appearance-none w-full sm:w-40 bg-surface-container-low text-on-surface border border-outline-variant rounded-lg pl-3 pr-8 py-2 text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary cursor-pointer"
              >
                <option value="TODOS">Todos</option>
                <option value="PENDIENTE">Pendiente</option>
                <option value="ATENDIDO">Atendido</option>
                <option value="CANCELADO">Cancelado</option>
              </select>
              <Filter className="absolute right-3 bottom-2.5 text-on-surface-variant w-3.5 h-3.5 pointer-events-none" />
            </div>

            <div className="flex items-end pt-5 sm:pt-0">
              <span className="text-xs text-on-surface-variant font-medium">
                Mostrando: <strong className="text-on-surface">{listaFiltrada.length}</strong> de{' '}
                <strong className="text-on-surface">{listaEspera.length}</strong>
              </span>
            </div>
          </div>
        </div>

        {/* List of Waiting Entries */}
        {listaFiltrada.length === 0 ? (
          <div className="py-12 text-center text-on-surface-variant border border-dashed border-outline-variant rounded-xl bg-surface-container-low">
            <p className="text-on-surface-variant/70 font-semibold text-sm">No hay pacientes en la lista de espera con esos filtros.</p>
          </div>
        ) : (
          <ul className="grid grid-cols-1 md:grid-cols-2 gap-4 list-none p-0 m-0">
            {listaFiltrada.map((item) => (
              <li 
                key={item.id} 
                className="bg-surface-container-lowest border border-outline-variant rounded-xl p-5 shadow-sm hover:shadow-md transition-all hover:border-primary/50 flex flex-col justify-between gap-4"
              >
                <div className="space-y-3">
                  <div className="flex flex-wrap items-center gap-2">
                    <strong className="text-sm text-on-surface font-bold">Paciente ID: {item.pacienteId ?? 'N/A'}</strong>
                    
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-extrabold border ${gravedadStyles(item.gravedad)}`}>
                      {item.gravedad || 'NORMAL'}
                    </span>
                    <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-extrabold border ${estadoStyles(item.estado)}`}>
                      {item.estado || 'PENDIENTE'}
                    </span>
                  </div>

                  <div className="text-xs text-on-surface-variant space-y-1">
                    <p className="font-semibold text-on-surface">Interconsulta: <span className="text-primary font-bold">{item.interconsulta || 'Sin especificar'}</span></p>
                    <p className="font-mono text-[10px] text-on-surface-variant/70">ID Registro: {item.id}</p>
                  </div>
                </div>

                <div className="flex items-center gap-2 pt-3 border-t border-outline-variant/30">
                  <select
                    className="flex-1 bg-surface-container-low text-on-surface border border-outline-variant rounded-lg p-2 text-xs font-semibold focus:outline-none focus:ring-1 focus:ring-primary cursor-pointer"
                    onChange={(e) => {
                      if (e.target.value) {
                        actualizarEstado(item.id, e.target.value);
                        e.target.value = '';
                      }
                    }}
                    disabled={cargando}
                  >
                    <option value="">Cambiar estado</option>
                    <option value="PENDIENTE">Pendiente</option>
                    <option value="ATENDIDO">Atendido</option>
                    <option value="CANCELADO">Cancelado</option>
                  </select>
                  
                  <button
                    type="button"
                    onClick={() => {
                      if (window.confirm('¿Estás seguro de que deseas eliminar este registro?')) {
                        eliminarDeListaEspera(item.id);
                      }
                    }}
                    disabled={cargando}
                    className="p-2 bg-error-container/20 hover:bg-error-container/40 border border-error/20 text-error rounded-lg transition-colors cursor-pointer shrink-0"
                    title="Eliminar registro"
                  >
                    <Trash2 className="w-4 h-4" />
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

function ListaEspera() {
  const {
    listaEspera,
    cargando,
    mensaje,
    error,
    cargarListaEspera,
    eliminarDeListaEspera,
    actualizarEstado,
  } = useListaEspera();

  return (
    <ListaEsperaView
      listaEspera={listaEspera}
      cargando={cargando}
      mensaje={mensaje}
      error={error}
      eliminarDeListaEspera={eliminarDeListaEspera}
      actualizarEstado={actualizarEstado}
      recargarListaEspera={cargarListaEspera}
    />
  );
}

export default ListaEspera;
