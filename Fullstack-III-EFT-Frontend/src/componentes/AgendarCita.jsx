import { useEffect, useState } from 'react';
import { obtenerPacientes } from '../api/gestionPacientesApi';
import { obtenerMedicos } from '../api/pacienteApi';
import { crearCita } from '../api/citasApi';
import {
  CalendarPlus, User, Stethoscope, Clock, CheckCircle, AlertCircle, RefreshCw
} from 'lucide-react';

function AgendarCita({ onSectionChange }) {
  const [pacientes, setPacientes] = useState([]);
  const [medicos, setMedicos] = useState([]);
  const [pacienteId, setPacienteId] = useState('');
  const [medicoId, setMedicoId] = useState('');
  const [fechaHora, setFechaHora] = useState('');
  const [cargando, setCargando] = useState(false);
  const [cargandoDatos, setCargandoDatos] = useState(true);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');

  useEffect(() => {
    const cargarDatos = async () => {
      setCargandoDatos(true);
      setError('');
      try {
        const [pacientesData, medicosData] = await Promise.all([
          obtenerPacientes(),
          obtenerMedicos(),
        ]);
        setPacientes(Array.isArray(pacientesData) ? pacientesData : []);
        setMedicos(Array.isArray(medicosData) ? medicosData : []);
      } catch (err) {
        setError(err.message || 'Error al cargar datos');
      } finally {
        setCargandoDatos(false);
      }
    };
    void cargarDatos();
  }, []);

  const manejarSubmit = async (e) => {
    e.preventDefault();
    if (!pacienteId || !medicoId || !fechaHora) {
      setError('Completa todos los campos');
      return;
    }

    setCargando(true);
    setError('');
    setExito('');

    try {
      const medico = medicos.find((m) => m.id === Number(medicoId));
      await crearCita({
        pacienteId: Number(pacienteId),
        medico: medico ? { id: medico.id } : { id: Number(medicoId) },
        fechaHora: new Date(fechaHora).toISOString(),
      });
      setExito('Cita agendada exitosamente');
      setPacienteId('');
      setMedicoId('');
      setFechaHora('');
    } catch (err) {
      setError(err.message || 'Error al agendar la cita');
    } finally {
      setCargando(false);
    }
  };

  const minDatetime = () => {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    return now.toISOString().slice(0, 16);
  };

  return (
    <div className="ml-[260px] pt-24 p-gutter min-h-screen space-y-6">
      <header className="flex items-center gap-3">
        <div className="p-2.5 bg-primary-container text-primary rounded-xl">
          <CalendarPlus className="w-6 h-6" />
        </div>
        <div>
          <h2 className="text-2xl font-bold text-on-surface tracking-tight">Agendar Cita</h2>
          <p className="text-sm text-on-surface-variant mt-1">
            Programa una cita médica para un paciente existente
          </p>
        </div>
      </header>

      {error && (
        <div className="bg-error-container border border-error/10 text-on-error-container p-4 rounded-xl text-sm font-semibold flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {exito && (
        <div className="bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-100 dark:border-emerald-900/50 text-emerald-800 dark:text-emerald-300 p-4 rounded-xl text-sm font-semibold space-y-3">
          <div className="flex items-center gap-2">
            <CheckCircle className="w-4 h-4 shrink-0" />
            <span>{exito}</span>
          </div>
          <button
            type="button"
            onClick={() => onSectionChange?.('citas')}
            className="w-full py-2 px-4 bg-primary hover:bg-primary/95 text-white rounded-lg text-sm font-bold shadow-lg shadow-primary/15 transition-all cursor-pointer hover:-translate-y-0.5"
          >
            Ir a Gestión de Citas →
          </button>
        </div>
      )}

      {cargandoDatos ? (
        <div className="py-12 text-center text-on-surface-variant/70">
          <RefreshCw className="w-8 h-8 animate-spin mx-auto text-primary mb-3" />
          <p className="font-semibold text-sm">Cargando datos…</p>
        </div>
      ) : (
        <form onSubmit={manejarSubmit} className="bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs p-6 space-y-5 max-w-xl">
          <div>
            <label className="block text-[10px] font-bold text-on-surface-variant/70 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
              <User className="w-3.5 h-3.5" /> Paciente
            </label>
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
          </div>

          <div>
            <label className="block text-[10px] font-bold text-on-surface-variant/70 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
              <Stethoscope className="w-3.5 h-3.5" /> Médico
            </label>
            <select
              value={medicoId}
              onChange={(e) => setMedicoId(e.target.value)}
              disabled={cargando}
              className="appearance-none w-full border border-outline-variant rounded-lg p-2.5 pr-8 text-on-surface bg-surface-container-low focus:bg-surface-container-lowest focus:outline-none focus:ring-2 focus:ring-primary/10 focus:border-primary text-sm transition-all cursor-pointer"
            >
              <option value="">— Selecciona un médico —</option>
              {medicos.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.nombre} — {m.especialidad || 'Sin especificar'}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-[10px] font-bold text-on-surface-variant/70 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5" /> Fecha y Hora
            </label>
            <input
              type="datetime-local"
              value={fechaHora}
              onChange={(e) => setFechaHora(e.target.value)}
              min={minDatetime()}
              disabled={cargando}
              className="w-full border border-outline-variant rounded-lg p-2.5 text-on-surface bg-surface-container-low focus:bg-surface-container-lowest focus:outline-none focus:ring-2 focus:ring-primary/10 focus:border-primary text-sm transition-all"
            />
          </div>

          <button
            type="submit"
            disabled={cargando || !pacienteId || !medicoId || !fechaHora}
            className={`w-full py-2.5 px-4 bg-primary hover:bg-primary/95 text-white rounded-lg text-sm font-bold shadow-lg shadow-primary/15 transition-all cursor-pointer ${
              (cargando || !pacienteId || !medicoId || !fechaHora) ? 'opacity-50 cursor-not-allowed transform-none' : 'hover:-translate-y-0.5'
            }`}
          >
            {cargando ? 'Agendando…' : 'Agendar Cita'}
          </button>
        </form>
      )}
    </div>
  );
}

export default AgendarCita;
