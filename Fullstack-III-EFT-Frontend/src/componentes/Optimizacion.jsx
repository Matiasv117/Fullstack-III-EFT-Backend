import { useEffect, useState } from 'react';
import { obtenerListaEsperaOptimizada, cancelarCitaConEstrategia } from '../api/optimizacionApi';
import { 
  Zap, Play, Filter, AlertTriangle, RefreshCw, Info, ChevronDown, ListOrdered, Layers, ClipboardList
} from 'lucide-react';

const ESTRATEGIAS = [
  { id: 'fifo', label: 'FIFO', desc: 'Primera en llegar — paciente con más tiempo en espera' },
  { id: 'lifo', label: 'LIFO', desc: 'Última en llegar — paciente más reciente en la lista' },
  { id: 'gravedad', label: 'Por Gravedad', desc: 'Mayor gravedad primero — criterio clínico' },
];

const PESO_GRAVEDAD = { ALTA: 3, MEDIA: 2, BAJA: 1, NORMAL: 1 };

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
  const [vistaEstrategia, setVistaEstrategia] = useState('lista');

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

  const ordenarLista = (lista, estrategiaId) => {
    const copia = [...lista];
    switch (estrategiaId) {
      case 'fifo':
        return copia.sort((a, b) => (a.id || 0) - (b.id || 0));
      case 'lifo':
        return copia.sort((a, b) => (b.id || 0) - (a.id || 0));
      case 'gravedad':
        return copia.sort((a, b) => {
          const gA = PESO_GRAVEDAD[a.gravedad] || 1;
          const gB = PESO_GRAVEDAD[b.gravedad] || 1;
          if (gB !== gA) return gB - gA;
          return (a.id || 0) - (b.id || 0);
        });
      default:
        return copia;
    }
  };

  useEffect(() => {
    let filtrada = [...listaEspera];

    if (filtroGravedad !== 'TODOS') {
      filtrada = filtrada.filter((item) => (item.gravedad || 'NORMAL') === filtroGravedad);
    }

    if (filtroEstado !== 'TODOS') {
      filtrada = filtrada.filter((item) => (item.estado || 'PENDIENTE') === filtroEstado);
    }

    filtrada = ordenarLista(filtrada, estrategia);
    setListaFiltrada(filtrada);
  }, [listaEspera, filtroGravedad, filtroEstado, estrategia]);

  const manejarCancelacion = async () => {
    if (!citaAnclarId) {
      setError('Selecciona una cita para cancelar');
      return;
    }

    setSimulandoCancelacion(true);
    setError('');

    try {
      await cancelarCitaConEstrategia(citaAnclarId, estrategia);
      const datos = await obtenerListaEsperaOptimizada();
      setListaEspera(Array.isArray(datos) ? datos : []);
      setCitaAnclarId(null);
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
      ALTA: 'bg-rose-50 dark:bg-rose-950/30 text-rose-700 dark:text-rose-300 border-rose-100 dark:border-rose-900/50',
      MEDIA: 'bg-amber-50 dark:bg-amber-950/30 text-amber-700 dark:text-amber-300 border-amber-100 dark:border-amber-900/50',
      BAJA: 'bg-emerald-50 dark:bg-emerald-950/30 text-emerald-700 dark:text-emerald-300 border-emerald-100 dark:border-emerald-900/50',
      NORMAL: 'bg-blue-50 dark:bg-blue-950/30 text-blue-700 dark:text-blue-300 border-blue-100 dark:border-blue-900/50',
    };
    return mapa[gravedad] || 'bg-surface-container-low text-on-surface-variant border-outline-variant';
  };

  const estadoColor = (estado) => {
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
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-primary-container text-primary rounded-xl">
            <Zap className="w-6 h-6 animate-pulse" />
          </div>
          <div>
            <h2 className="text-2xl font-bold text-on-surface tracking-tight">Optimización de Lista de Espera</h2>
            <p className="text-sm text-on-surface-variant mt-1">
              Monitorea y optimiza la asignación de citas médicas mediante estrategias inteligentes.
            </p>
          </div>
        </div>
      </header>

      {/* Error Feedbacks */}
      {error && (
        <div className="bg-error-container border border-error/10 text-on-error-container p-4 rounded-xl text-sm font-semibold flex items-center gap-2 shadow-2xs text-left">
          <AlertTriangle className="w-4 h-4 text-rose-600 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        
        {/* Simular Cancelación Form */}
        <div className="xl:col-span-1 bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs p-6 h-fit space-y-6">
          <div className="flex items-center gap-2 pb-4 border-b border-outline-variant">
            <Play className="w-5 h-5 text-primary shrink-0" />
            <h3 className="font-bold text-on-surface">Simular Cancelación de Cita</h3>
          </div>

          <form className="space-y-4 text-left" onSubmit={(e) => e.preventDefault()}>
            <div>
              <label className="block text-[10px] font-bold text-on-surface-variant/70 uppercase tracking-wider mb-1.5">ID de Cita a Cancelar</label>
              <input
                type="number"
                value={citaAnclarId || ''}
                onChange={(e) => setCitaAnclarId(e.target.value ? parseInt(e.target.value) : null)}
                placeholder="Ingresa el ID de la cita"
                disabled={simulandoCancelacion}
                className="w-full border border-outline-variant rounded-lg p-2.5 text-on-surface bg-surface-container-low focus:bg-surface-container-lowest focus:outline-none focus:ring-2 focus:ring-primary/10 focus:border-primary text-sm transition-all"
              />
            </div>

            <div>
              <label className="block text-[10px] font-bold text-on-surface-variant/70 uppercase tracking-wider mb-1.5">Estrategia de Reasignación</label>
              <div className="relative">
                <select
                  value={estrategia}
                  onChange={(e) => setEstrategia(e.target.value)}
                  disabled={simulandoCancelacion}
                  className="appearance-none w-full border border-outline-variant rounded-lg p-2.5 pr-8 text-on-surface bg-surface-container-low focus:bg-surface-container-lowest focus:outline-none focus:ring-2 focus:ring-primary/10 focus:border-primary text-sm font-semibold transition-all cursor-pointer"
                >
                  {ESTRATEGIAS.map((e) => (
                    <option key={e.id} value={e.id}>{e.label}</option>
                  ))}
                </select>
                <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 text-on-surface-variant w-4 h-4 pointer-events-none" />
              </div>
            </div>

            <button
              type="button"
              onClick={manejarCancelacion}
              disabled={simulandoCancelacion || !citaAnclarId}
              className={`w-full py-2.5 px-4 bg-primary hover:bg-primary/95 text-white rounded-lg text-sm font-bold shadow-lg shadow-primary/15 transition-all cursor-pointer ${
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
          <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs p-6 space-y-6">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 pb-5 border-b border-outline-variant">
              <div className="flex items-center gap-2">
                <ClipboardList className="w-5 h-5 text-primary shrink-0" />
                <h3 className="font-bold text-on-surface">Lista de Espera</h3>
              </div>

              <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 w-full sm:w-auto">
                <div className="relative">
                  <select
                    value={filtroGravedad}
                    onChange={(e) => setFiltroGravedad(e.target.value)}
                    className="appearance-none bg-surface-container-low border border-outline-variant text-on-surface font-semibold px-4 pr-9 py-1.5 rounded-lg text-xs cursor-pointer focus:outline-none"
                  >
                    <option value="TODOS">Todos (Gravedad)</option>
                    <option value="ALTA">Alta</option>
                    <option value="MEDIA">Media</option>
                    <option value="BAJA">Baja</option>
                    <option value="NORMAL">Normal</option>
                  </select>
                  <Filter className="w-3.5 h-3.5 text-on-surface-variant absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>

                <div className="relative">
                  <select
                    value={filtroEstado}
                    onChange={(e) => setFiltroEstado(e.target.value)}
                    className="appearance-none bg-surface-container-low border border-outline-variant text-on-surface font-semibold px-4 pr-9 py-1.5 rounded-lg text-xs cursor-pointer focus:outline-none"
                  >
                    <option value="TODOS">Todos (Estado)</option>
                    <option value="PENDIENTE">Pendiente</option>
                    <option value="ATENDIDO">Atendido</option>
                    <option value="CANCELADO">Cancelado</option>
                  </select>
                  <Filter className="w-3.5 h-3.5 text-on-surface-variant absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>

                <span className="text-xs text-on-surface-variant font-medium whitespace-nowrap self-center">
                  Total: <strong className="text-on-surface">{listaFiltrada.length}</strong> pacientes
                </span>
              </div>
            </div>

            {/* Strategy Tabs */}
            <div className="flex gap-1 bg-surface-container-low rounded-lg p-1">
              <button
                onClick={() => { setVistaEstrategia('lista'); setEstrategia('fifo'); }}
                className={`flex-1 py-1.5 px-3 rounded-md text-xs font-bold transition-all cursor-pointer ${
                  vistaEstrategia === 'lista' && estrategia === 'fifo'
                    ? 'bg-white dark:bg-surface-container-lowest text-primary shadow-xs'
                    : 'text-on-surface-variant hover:text-on-surface'
                }`}
              >
                <div className="flex items-center justify-center gap-1.5">
                  <ListOrdered className="w-3.5 h-3.5" />
                  <span>FIFO</span>
                </div>
              </button>
              <button
                onClick={() => { setVistaEstrategia('lista'); setEstrategia('lifo'); }}
                className={`flex-1 py-1.5 px-3 rounded-md text-xs font-bold transition-all cursor-pointer ${
                  vistaEstrategia === 'lista' && estrategia === 'lifo'
                    ? 'bg-white dark:bg-surface-container-lowest text-primary shadow-xs'
                    : 'text-on-surface-variant hover:text-on-surface'
                }`}
              >
                <div className="flex items-center justify-center gap-1.5">
                  <ListOrdered className="w-3.5 h-3.5 rotate-180" />
                  <span>LIFO</span>
                </div>
              </button>
              <button
                onClick={() => { setVistaEstrategia('lista'); setEstrategia('gravedad'); }}
                className={`flex-1 py-1.5 px-3 rounded-md text-xs font-bold transition-all cursor-pointer ${
                  vistaEstrategia === 'lista' && estrategia === 'gravedad'
                    ? 'bg-white dark:bg-surface-container-lowest text-primary shadow-xs'
                    : 'text-on-surface-variant hover:text-on-surface'
                }`}
              >
                <div className="flex items-center justify-center gap-1.5">
                  <Layers className="w-3.5 h-3.5" />
                  <span>Gravedad</span>
                </div>
              </button>
            </div>

            {/* Active strategy description */}
            <p className="text-[10px] text-on-surface-variant/70 italic -mt-4">
              Ordenando por: {ESTRATEGIAS.find((e) => e.id === estrategia)?.desc}
            </p>

            {/* List */}
            {cargando ? (
              <div className="py-12 text-center text-on-surface-variant/70">
                <RefreshCw className="w-8 h-8 animate-spin mx-auto text-primary mb-3" />
                <p className="font-semibold text-sm">Cargando lista de espera…</p>
              </div>
            ) : listaFiltrada.length === 0 ? (
              <div className="py-12 text-center text-on-surface-variant border border-dashed border-outline-variant rounded-xl bg-surface-container-low">
                <p className="text-on-surface-variant/70 font-semibold text-sm">No hay pacientes en la lista de espera</p>
              </div>
            ) : (
              <ul className="grid grid-cols-1 sm:grid-cols-2 gap-4 list-none p-0 m-0">
                {listaFiltrada.map((item, idx) => (
                  <li 
                    key={item.id} 
                    className="bg-surface-container-lowest border border-outline-variant rounded-xl p-4 shadow-sm hover:shadow-md transition-all hover:border-primary/50 flex flex-col justify-between gap-3 text-left"
                  >
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-[9px] font-mono text-on-surface-variant/50 min-w-[1.5rem]">#{idx + 1}</span>
                      <strong className="text-xs text-on-surface font-bold">ID: {item.id}</strong>
                      <span className={`px-2 py-0.5 rounded-full text-[9px] font-extrabold border ${gravedadColor(item.gravedad)}`}>
                        {item.gravedad || 'NORMAL'}
                      </span>
                      <span className={`px-2 py-0.5 rounded-full text-[9px] font-extrabold border ${estadoColor(item.estado)}`}>
                        {item.estado || 'PENDIENTE'}
                      </span>
                    </div>

                    <div className="text-xs text-on-surface-variant space-y-0.5">
                      <p className="font-semibold text-on-surface">Interconsulta: <span className="text-primary font-bold">{item.interconsulta || 'Sin especificar'}</span></p>
                    </div>
                  </li>
                ))}
              </ul>
            )}

          </div>

        </div>

      </div>

      {/* Strategies Information Section */}
      <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs p-6 text-left">
        <h3 className="font-bold text-on-surface text-sm flex items-center gap-2 pb-4 border-b border-outline-variant">
          <Info className="w-5 h-5 text-primary shrink-0" />
          Estrategias de Optimización
        </h3>
        
        <ul className="grid grid-cols-1 md:grid-cols-3 gap-6 list-none p-0 pt-4 m-0 text-xs">
          <li className="bg-surface-container-low p-4 rounded-xl border border-outline-variant space-y-1">
            <strong className="text-primary font-bold block text-sm mb-1">FIFO (Primera En Llegar)</strong>
            <p className="text-on-surface-variant leading-relaxed">Reasigna la cita cancelada al paciente que lleva más tiempo esperando en la lista de espera.</p>
          </li>
          <li className="bg-surface-container-low p-4 rounded-xl border border-outline-variant space-y-1">
            <strong className="text-primary font-bold block text-sm mb-1">LIFO (Última En Llegar)</strong>
            <p className="text-on-surface-variant leading-relaxed">Reasigna la cita al paciente más reciente registrado en la lista de espera.</p>
          </li>
          <li className="bg-surface-container-low p-4 rounded-xl border border-outline-variant space-y-1">
            <strong className="text-primary font-bold block text-sm mb-1">Por Gravedad</strong>
            <p className="text-on-surface-variant leading-relaxed">Reasigna la cita al paciente que presenta la mayor gravedad en su estado de salud.</p>
          </li>
        </ul>
      </div>

    </div>
  );
}

export default Optimizacion;