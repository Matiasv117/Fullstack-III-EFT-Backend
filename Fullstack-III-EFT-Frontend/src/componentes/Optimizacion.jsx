import { useEffect, useState } from 'react';
import { obtenerListaEsperaOptimizada, cancelarCitaConEstrategia } from '../api/optimizacionApi';
import { 
  Zap, Play, Filter, AlertTriangle, RefreshCw, ClipboardList, Info, ChevronDown
} from 'lucide-react';

function Optimizacion() {
  const [listaEspera, setListaEspera] = useState([]);
  const [listaFiltrada, setListaFiltrada] = useState([]);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState('');
  const [filtroGravedad, setFiltroGravedad] = useState('TODOS');
  const [filtroEstado, setFiltroEstado] = useState('TODOS');
  const [simulandoCancelacion, setSimulandoCancelacion] = useState(false);
  const [citaAnclarId, setCitaAnclarId] = useState(null);
  const [estrategia, setEstrategia] = useState('fifo');

  useEffect(() => {
    const fetchLista = async () => {
      setCargando(true);
      setError('');

      try {
        const datos = await obtenerListaEsperaOptimizada();
        setListaEspera(Array.isArray(datos) ? datos : []);
      } catch (errorCapturado) {
        setError(errorCapturado.message || 'No fue posible obtener la lista de espera');
      } finally {
        setCargando(false);
      }
    };

    void fetchLista();
  }, []);

  useEffect(() => {
    let filtrada = [...listaEspera];

    if (filtroGravedad !== 'TODOS') {
      filtrada = filtrada.filter((item) => (item.gravedad || 'NORMAL') === filtroGravedad);
    }

    if (filtroEstado !== 'TODOS') {
      filtrada = filtrada.filter((item) => (item.estado || 'PENDIENTE') === filtroEstado);
    }

    setListaFiltrada(filtrada);
  }, [listaEspera, filtroGravedad, filtroEstado]);

  const manejarCancelacion = async () => {
    if (!citaAnclarId) {
      setError('Selecciona una cita para cancelar');
      return;
    }

    setSimulandoCancelacion(true);
    setError('');

    try {
      await cancelarCitaConEstrategia(citaAnclarId, estrategia);
      // Recargar la lista después de la cancelación
      const datos = await obtenerListaEsperaOptimizada();
      setListaEspera(Array.isArray(datos) ? datos : []);
      setCitaAnclarId(null);
      // Mostrar mensaje de éxito
      alert(`Cita ${citaAnclarId} cancelada y reasignada con estrategia ${estrategia}`);
    } catch (errorCapturado) {
      setError(
        errorCapturado.message ||
        'No fue posible procesar la cancelación de la cita'
      );
    } finally {
      setSimulandoCancelacion(false);
    }
  };

  const gravedadColor = (gravedad) => {
    const mapa = {
      ALTA: 'bg-rose-50 text-rose-700 border-rose-100',
      MEDIA: 'bg-amber-50 text-amber-700 border-amber-100',
      BAJA: 'bg-emerald-50 text-emerald-700 border-emerald-100',
      NORMAL: 'bg-blue-50 text-blue-700 border-blue-100',
    };
    return mapa[gravedad] || 'bg-slate-50 text-slate-700 border-slate-100';
  };

  const estadoColor = (estado) => {
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
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-blue-50 text-blue-600 rounded-xl">
            <Zap className="w-6 h-6 animate-pulse" />
          </div>
          <div>
            <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Optimización de Lista de Espera</h2>
            <p className="text-sm text-slate-500 mt-1">
              Monitorea y optimiza la asignación de citas médicas mediante estrategias inteligentes.
            </p>
          </div>
        </div>
      </header>

      {/* Error Feedbacks */}
      {error && (
        <div className="bg-rose-55 bg-rose-50 border border-rose-200 text-rose-800 p-4 rounded-xl text-sm font-semibold flex items-center gap-2 shadow-2xs text-left">
          <AlertTriangle className="w-4 h-4 text-rose-600 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        
        {/* Simular Cancelación Form */}
        <div className="xl:col-span-1 bg-white border border-slate-100 rounded-2xl shadow-xs p-6 h-fit space-y-6">
          <div className="flex items-center gap-2 pb-4 border-b border-slate-100">
            <Play className="w-5 h-5 text-blue-600 shrink-0" />
            <h3 className="font-bold text-slate-900">Simular Cancelación de Cita</h3>
          </div>

          <form className="space-y-4 text-left" onSubmit={(e) => e.preventDefault()}>
            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">ID de Cita a Cancelar</label>
              <input
                type="number"
                value={citaAnclarId || ''}
                onChange={(e) => setCitaAnclarId(e.target.value ? parseInt(e.target.value) : null)}
                placeholder="Ingresa el ID de la cita"
                disabled={simulandoCancelacion}
                className="w-full border border-slate-200 rounded-lg p-2.5 text-slate-700 bg-slate-50/50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/10 focus:border-blue-500 text-sm transition-all"
              />
            </div>

            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">Estrategia de Reasignación</label>
              <div className="relative">
                <select
                  value={estrategia}
                  onChange={(e) => setEstrategia(e.target.value)}
                  disabled={simulandoCancelacion}
                  className="appearance-none w-full border border-slate-200 rounded-lg p-2.5 pr-8 text-slate-700 bg-slate-50/50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/10 focus:border-blue-500 text-sm font-semibold transition-all cursor-pointer"
                >
                  <option value="fifo">FIFO (Primera En Llegar)</option>
                  <option value="lifo">LIFO (Última En Llegar)</option>
                  <option value="gravedad">Por Gravedad</option>
                </select>
                <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 w-4 h-4 pointer-events-none" />
              </div>
            </div>

            <button
              type="button"
              onClick={manejarCancelacion}
              disabled={simulandoCancelacion || !citaAnclarId}
              className={`w-full py-2.5 px-4 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-bold shadow-xs shadow-blue-100 transition-all cursor-pointer ${
                (simulandoCancelacion || !citaAnclarId) ? 'opacity-50 cursor-not-allowed transform-none' : 'hover:-translate-y-0.5'
              }`}
            >
              {simulandoCancelacion ? 'Procesando...' : 'Procesar Cancelación'}
            </button>
          </form>
        </div>

        {/* Filters and Current Waiting List */}
        <div className="xl:col-span-2 space-y-6">
          
          {/* Waiting List Panel */}
          <div className="bg-white border border-slate-100 rounded-2xl shadow-xs p-6 space-y-6">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 pb-5 border-b border-slate-100">
              <div className="flex items-center gap-2">
                <ClipboardList className="w-5 h-5 text-blue-600 shrink-0" />
                <h3 className="font-bold text-slate-800">Lista de Espera Actual</h3>
              </div>

              <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 w-full sm:w-auto">
                <div className="relative">
                  <select
                    value={filtroGravedad}
                    onChange={(e) => setFiltroGravedad(e.target.value)}
                    className="appearance-none bg-slate-50 border border-slate-200 text-slate-700 font-semibold px-4 pr-9 py-1.5 rounded-lg text-xs cursor-pointer focus:outline-none"
                  >
                    <option value="TODOS">Todos (Gravedad)</option>
                    <option value="ALTA">Alta</option>
                    <option value="MEDIA">Media</option>
                    <option value="BAJA">Baja</option>
                    <option value="NORMAL">Normal</option>
                  </select>
                  <Filter className="w-3.5 h-3.5 text-slate-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>

                <div className="relative">
                  <select
                    value={filtroEstado}
                    onChange={(e) => setFiltroEstado(e.target.value)}
                    className="appearance-none bg-slate-50 border border-slate-200 text-slate-700 font-semibold px-4 pr-9 py-1.5 rounded-lg text-xs cursor-pointer focus:outline-none"
                  >
                    <option value="TODOS">Todos (Estado)</option>
                    <option value="PENDIENTE">Pendiente</option>
                    <option value="ATENDIDO">Atendido</option>
                    <option value="CANCELADO">Cancelado</option>
                  </select>
                  <Filter className="w-3.5 h-3.5 text-slate-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>

                <span className="text-xs text-slate-500 font-medium whitespace-nowrap self-center">
                  Total: <strong className="text-slate-800">{listaFiltrada.length}</strong> pacientes
                </span>
              </div>
            </div>

            {/* List */}
            {cargando ? (
              <div className="py-12 text-center text-slate-400">
                <RefreshCw className="w-8 h-8 animate-spin mx-auto text-blue-500 mb-3" />
                <p className="font-semibold text-sm">Cargando lista de espera…</p>
              </div>
            ) : listaFiltrada.length === 0 ? (
              <div className="py-12 text-center text-slate-450 border border-dashed border-slate-200 rounded-xl bg-slate-50/50">
                <p className="text-slate-400 font-semibold text-sm">No hay pacientes en la lista de espera</p>
              </div>
            ) : (
              <ul className="grid grid-cols-1 sm:grid-cols-2 gap-4 list-none p-0 m-0">
                {listaFiltrada.map((item) => (
                  <li 
                    key={item.id} 
                    className="bg-white border border-slate-100 rounded-xl p-4 shadow-2xs hover:shadow-sm transition-all hover:border-blue-300 flex flex-col justify-between gap-3 text-left"
                  >
                    <div className="flex flex-wrap items-center gap-2">
                      <strong className="text-xs text-slate-800 font-bold">Paciente ID: {item.pacienteId ?? 'N/A'}</strong>
                      <span className={`px-2 py-0.5 rounded-full text-[9px] font-extrabold border ${gravedadColor(item.gravedad)}`}>
                        {item.gravedad || 'NORMAL'}
                      </span>
                      <span className={`px-2 py-0.5 rounded-full text-[9px] font-extrabold border ${estadoColor(item.estado)}`}>
                        {item.estado || 'PENDIENTE'}
                      </span>
                    </div>

                    <div className="text-xs text-slate-500 space-y-0.5">
                      <p className="font-semibold text-slate-700">Interconsulta: <span className="text-blue-600 font-bold">{item.interconsulta || 'Sin especificar'}</span></p>
                      <p className="font-mono text-[9px]">ID Registro: {item.id}</p>
                    </div>
                  </li>
                ))}
              </ul>
            )}

          </div>

        </div>

      </div>

      {/* Strategies Information Section */}
      <div className="bg-white border border-slate-100 rounded-2xl shadow-xs p-6 text-left">
        <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2 pb-4 border-b border-slate-100">
          <Info className="w-5 h-5 text-blue-600 shrink-0" />
          <span>📋 Estrategias de Optimización</span>
        </h3>
        
        <ul className="grid grid-cols-1 md:grid-cols-3 gap-6 list-none p-0 pt-4 m-0 text-xs">
          <li className="bg-slate-50/50 p-4 rounded-xl border border-slate-150 border-slate-200/50 space-y-1">
            <strong className="text-blue-600 font-bold block text-sm mb-1">FIFO (Primera En Llegar)</strong>
            <p className="text-slate-600 leading-relaxed">Reasigna la cita cancelada al paciente que lleva más tiempo esperando en la lista de espera.</p>
          </li>
          <li className="bg-slate-50/50 p-4 rounded-xl border border-slate-150 border-slate-200/50 space-y-1">
            <strong className="text-blue-600 font-bold block text-sm mb-1">LIFO (Última En Llegar)</strong>
            <p className="text-slate-600 leading-relaxed">Reasigna la cita al paciente más reciente registrado en la lista de espera.</p>
          </li>
          <li className="bg-slate-50/50 p-4 rounded-xl border border-slate-150 border-slate-200/50 space-y-1">
            <strong className="text-blue-600 font-bold block text-sm mb-1">Por Gravedad</strong>
            <p className="text-slate-600 leading-relaxed">Reasigna la cita al paciente que presenta la mayor gravedad en su estado de salud.</p>
          </li>
        </ul>
      </div>

    </div>
  );
}

export default Optimizacion;
