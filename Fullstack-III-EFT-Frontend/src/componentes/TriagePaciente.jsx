import { useEffect, useState } from 'react';
import { obtenerPacientes } from '../api/gestionPacientesApi';
import { enviarAutotriage } from '../api/triageApi';
import {
  Activity, User, Stethoscope, FileText, AlertTriangle, CheckCircle, RefreshCw, ChevronDown
} from 'lucide-react';

const NIVELES_GRAVEDAD = [
  { valor: 1, label: '1 — Leve', desc: 'Síntomas menores, atención ambulatoria' },
  { valor: 2, label: '2 — Moderado', desc: 'Requiere evaluación médica' },
  { valor: 3, label: '3 — Grave', desc: 'Requiere atención prioritaria' },
  { valor: 4, label: '4 — Muy Grave', desc: 'Requiere atención urgente' },
  { valor: 5, label: '5 — Crítico', desc: 'Emergencia vital' },
];

function TriagePaciente({ onSectionChange }) {
  const [pacientes, setPacientes] = useState([]);
  const [pacienteId, setPacienteId] = useState('');
  const [gravedad, setGravedad] = useState(1);
  const [sintomas, setSintomas] = useState('');
  const [cargando, setCargando] = useState(false);
  const [cargandoDatos, setCargandoDatos] = useState(true);
  const [error, setError] = useState('');
  const [resultado, setResultado] = useState(null);

  useEffect(() => {
    const cargar = async () => {
      setCargandoDatos(true);
      try {
        const data = await obtenerPacientes();
        setPacientes(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(err.message || 'Error al cargar pacientes');
      } finally {
        setCargandoDatos(false);
      }
    };
    void cargar();
  }, []);

  const manejarSubmit = async (e) => {
    e.preventDefault();
    if (!pacienteId) {
      setError('Selecciona un paciente');
      return;
    }

    setCargando(true);
    setError('');
    setResultado(null);

    try {
      const data = await enviarAutotriage({
        pacienteId: Number(pacienteId),
        gravedad,
        sintomas,
      });
      setResultado(data);
    } catch (err) {
      setError(err.message || 'Error al procesar triage');
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="ml-[260px] pt-24 p-gutter min-h-screen space-y-6">
      <header className="flex items-center gap-3">
        <div className="p-2.5 bg-primary-container text-primary rounded-xl">
          <Activity className="w-6 h-6" />
        </div>
        <div>
          <h2 className="text-2xl font-bold text-on-surface tracking-tight">Triage de Pacientes</h2>
          <p className="text-sm text-on-surface-variant mt-1">
            Evalúa y clasifica pacientes según su nivel de urgencia para asignarlos a la lista de espera
          </p>
        </div>
      </header>

      {error && (
        <div className="bg-error-container border border-error/10 text-on-error-container p-4 rounded-xl text-sm font-semibold flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {resultado && (
        <div className="bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs p-6 space-y-3">
          <div className="flex items-center gap-2 text-emerald-600 dark:text-emerald-400 font-bold text-sm">
            <CheckCircle className="w-4 h-4" />
            Triage completado
          </div>
          <div className="text-xs text-on-surface-variant space-y-2">
            {resultado.prioridad && (
              <p>Prioridad calculada: <strong className="text-on-surface">{resultado.prioridad.nivel || '—'}</strong></p>
            )}
            {resultado.listaEspera && (
              <p>Registrado en lista de espera: <strong className="text-on-surface">#{resultado.listaEspera.id}</strong></p>
            )}
            {resultado.prioridadError && (
              <p className="text-error">Error prioridad: {resultado.prioridadError}</p>
            )}
            {resultado.listaError && (
              <p className="text-error">Error lista de espera: {resultado.listaError}</p>
            )}
          </div>
          {resultado.listaEspera && (
            <button
              type="button"
              onClick={() => onSectionChange?.('listaespera')}
              className="mt-2 w-full py-2.5 px-4 bg-primary hover:bg-primary/95 text-white rounded-lg text-sm font-bold shadow-lg shadow-primary/15 transition-all cursor-pointer hover:-translate-y-0.5"
            >
              Ver en Lista de Espera →
            </button>
          )}
        </div>
      )}

      {cargandoDatos ? (
        <div className="py-12 text-center text-on-surface-variant/70">
          <RefreshCw className="w-8 h-8 animate-spin mx-auto text-primary mb-3" />
          <p className="font-semibold text-sm">Cargando pacientes…</p>
        </div>
      ) : (
        <form onSubmit={manejarSubmit} className="bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs p-6 space-y-5 max-w-xl">
          <div>
            <label className="block text-[10px] font-bold text-on-surface-variant/70 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
              <User className="w-3.5 h-3.5" /> Paciente
            </label>
            <div className="relative">
              <select
                value={pacienteId}
                onChange={(e) => setPacienteId(e.target.value)}
                disabled={cargando}
                className="appearance-none w-full border border-outline-variant rounded-lg p-2.5 pr-8 text-on-surface bg-surface-container-low focus:bg-surface-container-lowest focus:outline-none focus:ring-2 focus:ring-primary/10 focus:border-primary text-sm transition-all cursor-pointer"
              >
                <option value="">— Selecciona un paciente —</option>
                {pacientes.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.nombre} {p.apellido} ({p.dni || 'ID: ' + p.id})
                  </option>
                ))}
              </select>
              <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 text-on-surface-variant w-4 h-4 pointer-events-none" />
            </div>
          </div>

          <div>
            <label className="block text-[10px] font-bold text-on-surface-variant/70 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
              <Stethoscope className="w-3.5 h-3.5" /> Nivel de Gravedad
            </label>
            <div className="space-y-2">
              {NIVELES_GRAVEDAD.map((n) => (
                <label
                  key={n.valor}
                  className={`flex items-center gap-3 p-3 rounded-lg border cursor-pointer transition-all ${
                    gravedad === n.valor
                      ? 'border-primary bg-primary-container/30 text-on-surface'
                      : 'border-outline-variant bg-surface-container-low hover:bg-surface-container-high text-on-surface-variant'
                  }`}
                >
                  <input
                    type="radio"
                    name="gravedad"
                    value={n.valor}
                    checked={gravedad === n.valor}
                    onChange={() => setGravedad(n.valor)}
                    className="accent-primary"
                  />
                  <div className="flex-1">
                    <span className="font-bold text-sm">{n.label}</span>
                    <p className="text-[10px] text-on-surface-variant/70">{n.desc}</p>
                  </div>
                </label>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-[10px] font-bold text-on-surface-variant/70 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
              <FileText className="w-3.5 h-3.5" /> Síntomas / Interconsulta
            </label>
            <textarea
              value={sintomas}
              onChange={(e) => setSintomas(e.target.value)}
              placeholder="Describe los síntomas del paciente o la especialidad de interconsulta..."
              rows={3}
              disabled={cargando}
              className="w-full border border-outline-variant rounded-lg p-2.5 text-on-surface bg-surface-container-low focus:bg-surface-container-lowest focus:outline-none focus:ring-2 focus:ring-primary/10 focus:border-primary text-sm transition-all resize-none"
            />
          </div>

          <button
            type="submit"
            disabled={cargando || !pacienteId}
            className={`w-full py-2.5 px-4 bg-primary hover:bg-primary/95 text-white rounded-lg text-sm font-bold shadow-lg shadow-primary/15 transition-all cursor-pointer ${
              (cargando || !pacienteId) ? 'opacity-50 cursor-not-allowed transform-none' : 'hover:-translate-y-0.5'
            }`}
          >
            {cargando ? 'Procesando…' : 'Realizar Triage'}
          </button>
        </form>
      )}
    </div>
  );
}

export default TriagePaciente;
