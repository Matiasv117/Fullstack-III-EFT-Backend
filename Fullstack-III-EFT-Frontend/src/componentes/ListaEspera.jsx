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
      ALTA: 'bg-rose-50 text-rose-700 border-rose-100',
      MEDIA: 'bg-amber-50 text-amber-700 border-amber-100',
      BAJA: 'bg-emerald-50 text-emerald-700 border-emerald-100',
      NORMAL: 'bg-blue-50 text-blue-700 border-blue-100',
    };
    return mapa[gravedad] || 'bg-slate-50 text-slate-700 border-slate-100';
  };

  const estadoStyles = (estado) => {
    const mapa = {
      PENDIENTE: 'bg-rose-600 text-white shadow-xs shadow-rose-100',
      ATENDIDO: 'bg-emerald-600 text-white shadow-xs shadow-emerald-100',
      CANCELADO: 'bg-slate-400 text-white shadow-xs shadow-slate-100',
    };
    return mapa[estado] || 'bg-blue-600 text-white shadow-xs shadow-blue-100';
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Lista de Espera</h2>
          <p className="text-sm text-slate-500 mt-1">
            Monitorea y gestiona los pacientes que esperan atención médica en tiempo real.
          </p>
        </div>
        <button
          type="button"
          onClick={recargarListaEspera}
          disabled={cargando}
          className="inline-flex items-center gap-2 px-4 py-2 border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-semibold rounded-lg text-sm shadow-xs transition-colors cursor-pointer"
        >
          <RefreshCw className={`w-4 h-4 ${cargando ? 'animate-spin' : ''}`} />
          <span>{cargando ? 'Actualizando…' : 'Actualizar'}</span>
        </button>
      </header>

      {/* Alert Feedbacks */}
      <div className="space-y-3">
        {mensaje && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 p-4 rounded-xl text-sm font-semibold flex items-center gap-2">
            <span>{mensaje}</span>
          </div>
        )}
        {error && (
          <div className="bg-rose-50 border border-rose-200 text-rose-800 p-4 rounded-xl text-sm font-semibold flex items-center gap-2">
            <span>{error}</span>
          </div>
        )}
      </div>

      <div className="bg-white border border-slate-100 rounded-2xl shadow-xs p-6 space-y-6">
        
        {/* Toolbar & Filters */}
        <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-4 pb-5 border-b border-slate-100">
          <div className="flex items-center gap-2">
            <ClipboardList className="w-5 h-5 text-blue-600 shrink-0" />
            <h3 className="font-bold text-slate-800">Pacientes en lista de espera</h3>
          </div>
          
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 w-full lg:w-auto">
            {/* Filter by Gravity */}
            <div className="relative flex-1 sm:flex-initial">
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Gravedad</label>
              <select
                value={filtroGravedad}
                onChange={(e) => setFiltroGravedad(e.target.value)}
                className="appearance-none w-full sm:w-40 bg-slate-50 text-slate-700 border border-slate-200 rounded-lg pl-3 pr-8 py-2 text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer"
              >
                <option value="TODOS">Todos</option>
                <option value="ALTA">Alta</option>
                <option value="MEDIA">Media</option>
                <option value="BAJA">Baja</option>
                <option value="NORMAL">Normal</option>
              </select>
              <Filter className="absolute right-3 bottom-2.5 text-slate-400 w-3.5 h-3.5 pointer-events-none" />
            </div>

            {/* Filter by Status */}
            <div className="relative flex-1 sm:flex-initial">
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Estado</label>
              <select
                value={filtroEstado}
                onChange={(e) => setFiltroEstado(e.target.value)}
                className="appearance-none w-full sm:w-40 bg-slate-50 text-slate-700 border border-slate-200 rounded-lg pl-3 pr-8 py-2 text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 cursor-pointer"
              >
                <option value="TODOS">Todos</option>
                <option value="PENDIENTE">Pendiente</option>
                <option value="ATENDIDO">Atendido</option>
                <option value="CANCELADO">Cancelado</option>
              </select>
              <Filter className="absolute right-3 bottom-2.5 text-slate-400 w-3.5 h-3.5 pointer-events-none" />
            </div>

            <div className="flex items-end pt-5 sm:pt-0">
              <span className="text-xs text-slate-500 font-medium">
                Mostrando: <strong className="text-slate-800">{listaFiltrada.length}</strong> de{' '}
                <strong className="text-slate-800">{listaEspera.length}</strong>
              </span>
            </div>
          </div>
        </div>

        {/* List of Waiting Entries */}
        {listaFiltrada.length === 0 ? (
          <div className="py-12 text-center text-slate-450 border border-dashed border-slate-200 rounded-xl bg-slate-50/50">
            <p className="text-slate-400 font-semibold text-sm">No hay pacientes en la lista de espera con esos filtros.</p>
          </div>
        ) : (
          <ul className="grid grid-cols-1 md:grid-cols-2 gap-4 list-none p-0 m-0">
            {listaFiltrada.map((item) => (
              <li 
                key={item.id} 
                className="bg-white border border-slate-100 rounded-xl p-5 shadow-2xs hover:shadow-sm transition-all hover:border-blue-300 flex flex-col justify-between gap-4"
              >
                <div className="space-y-3">
                  <div className="flex flex-wrap items-center gap-2">
                    <strong className="text-sm text-slate-800 font-bold">Paciente ID: {item.pacienteId ?? 'N/A'}</strong>
                    
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-extrabold border ${gravedadStyles(item.gravedad)}`}>
                      {item.gravedad || 'NORMAL'}
                    </span>
                    <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-extrabold border ${estadoStyles(item.estado)}`}>
                      {item.estado || 'PENDIENTE'}
                    </span>
                  </div>

                  <div className="text-xs text-slate-550 text-slate-500 space-y-1">
                    <p className="font-semibold text-slate-700">Interconsulta: <span className="text-blue-600 font-bold">{item.interconsulta || 'Sin especificar'}</span></p>
                    <p className="font-mono text-[10px] text-slate-400">ID Registro: {item.id}</p>
                  </div>
                </div>

                <div className="flex items-center gap-2 pt-3 border-t border-slate-50">
                  <select
                    className="flex-1 bg-slate-50 text-slate-700 border border-slate-200 rounded-lg p-2 text-xs font-semibold focus:outline-none focus:ring-1 focus:ring-blue-500 cursor-pointer"
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
                    className="p-2 bg-rose-50 hover:bg-rose-100 border border-rose-200 text-rose-600 rounded-lg transition-colors cursor-pointer shrink-0"
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
