import { useState } from 'react'
import { 
  RefreshCw, Trash2, Eye, UserPlus, FileText, 
  X, Check
} from 'lucide-react'
import { validarNombre, validarApellido, validarDNI, validarEmail, validarTelefono } from '../utils/validations'

const getInitials = (nombre, apellido) => {
  return ((nombre?.[0] || '') + (apellido?.[0] || '')).toUpperCase() || '?';
}

function GestionPacientesView({
  pacientes,
  nuevoPaciente,
  cargando,
  mensaje,
  error,
  actualizarCampo,
  registrar,
  borrarPaciente,
  recargarPacientes,
  onSectionChange,
}) {
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [clinicalNotes, setClinicalNotes] = useState({});
  const [newNoteText, setNewNoteText] = useState('');
  const [erroresCampo, setErroresCampo] = useState({});

  const handleOpenProfile = (paciente) => {
    setSelectedPatient(paciente);
  };

  const handleAddNote = (e, patientId) => {
    e.preventDefault();
    if (!newNoteText.trim()) return;
    const author = "Dr. Elena Cruz";
    const date = new Date().toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' });
    const newNote = {
      id: crypto.randomUUID(),
      doctor: author,
      date,
      content: newNoteText
    };
    setClinicalNotes(prev => ({
      ...prev,
      [patientId]: [newNote, ...(prev[patientId] || [])]
    }));
    setNewNoteText('');
  };

  const validarFormulario = () => {
    const errores = {};

    const validNombre = validarNombre(nuevoPaciente.nombre);
    if (!validNombre.valido) errores.nombre = validNombre.mensaje;

    const validApellido = validarApellido(nuevoPaciente.apellido);
    if (!validApellido.valido) errores.apellido = validApellido.mensaje;

    const validDNI = validarDNI(nuevoPaciente.dni);
    if (!validDNI.valido) errores.dni = validDNI.mensaje;

    if (nuevoPaciente.telefono) {
      const validTel = validarTelefono(nuevoPaciente.telefono);
      if (!validTel.valido) errores.telefono = validTel.mensaje;
    }

    if (nuevoPaciente.email) {
      const validMail = validarEmail(nuevoPaciente.email);
      if (!validMail.valido) errores.email = validMail.mensaje;
    }

    setErroresCampo(errores);
    return Object.keys(errores).length === 0;
  };

  const handleRegistrar = () => {
    if (validarFormulario()) {
      registrar();
    }
  };

  return (
    <div className="space-y-8">
      {/* Header */}
      <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Gestión de Pacientes</h2>
          <p className="text-sm text-slate-500 mt-1">
            Registro de pacientes, notas médicas y visualización de datos clínicos.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={recargarPacientes}
            disabled={cargando}
            className="inline-flex items-center gap-2 px-4 py-2 border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-semibold rounded-lg text-sm shadow-xs transition-colors cursor-pointer"
          >
            <RefreshCw className={`w-4 h-4 ${cargando ? 'animate-spin' : ''}`} />
            <span>Actualizar lista</span>
          </button>
          <button
            type="button"
            onClick={() => onSectionChange?.('triaje')}
            className="inline-flex items-center gap-2 px-4 py-2 bg-primary hover:bg-primary/95 text-white font-semibold rounded-lg text-sm shadow-xs shadow-primary/15 transition-all cursor-pointer hover:-translate-y-0.5"
          >
            Ir a Triage →
          </button>
        </div>
      </header>

      {/* Alert Feedbacks */}
      <div className="space-y-3">
        {mensaje && (
          <div className="bg-emerald-50 border border-emerald-200/20 text-emerald-800 p-4 rounded-xl text-sm font-semibold flex items-center gap-2">
            <Check className="w-4 h-4 shrink-0" />
            <span>{mensaje}</span>
          </div>
        )}
        {error && (
          <div className="bg-rose-50 border border-rose-200/20 text-rose-800 p-4 rounded-xl text-sm font-semibold flex items-center gap-2">
            <X className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-4 gap-8">
        {/* Pacientes Registrados Table */}
        <div className="xl:col-span-3 bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs overflow-hidden">
          <div className="p-5 border-b border-outline-variant flex justify-between items-center bg-surface-container-low">
            <h3 className="font-bold text-on-surface">Pacientes registrados</h3>
            <span className="bg-primary-container text-primary text-xs px-2.5 py-1 rounded-full font-bold">
              {pacientes.length} Registros
            </span>
          </div>

          <div className="overflow-x-auto w-full">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-surface-container-low text-on-surface-variant font-semibold text-xs uppercase tracking-wider border-b border-outline-variant">
                  <th className="py-4 px-6">Paciente</th>
                  <th className="py-4 px-6">Identificación</th>
                  <th className="py-4 px-6 text-center">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-outline-variant text-sm">
                {pacientes.length === 0 ? (
                  <tr>
                    <td colSpan={3} className="py-12 px-6 text-center text-on-surface-variant">
                      No hay pacientes registrados todavía.
                    </td>
                  </tr>
                ) : (
                  pacientes.map((paciente) => {
                    const initials = getInitials(paciente.nombre, paciente.apellido);
                    return (
                      <tr key={paciente.id} className="hover:bg-surface-container-low transition-colors">
                        <td className="py-4 px-6">
                          <div className="flex items-center gap-3">
                            <div
                              onClick={() => handleOpenProfile(paciente)}
                              className="w-10 h-10 rounded-full bg-primary/10 text-primary flex items-center justify-center font-bold text-sm cursor-pointer hover:opacity-80"
                            >
                              {initials}
                            </div>
                            <div>
                              <p 
                                onClick={() => handleOpenProfile(paciente)}
                                className="font-semibold text-on-surface hover:text-primary cursor-pointer transition-colors"
                              >
                                {paciente.nombre} {paciente.apellido}
                              </p>
                              <span className="text-xs text-on-surface-variant font-medium">{paciente.email || 'Sin email'}</span>
                            </div>
                          </div>
                        </td>
                        <td className="py-4 px-6">
                          <div className="space-y-0.5">
                            <p className="font-mono text-xs text-on-surface">DNI: {paciente.dni}</p>
                            <p className="text-xs text-on-surface-variant">Contacto: {paciente.telefono || 'Sin teléfono'}</p>
                          </div>
                        </td>
                        <td className="py-4 px-6">
                          <div className="flex items-center gap-2 justify-center">
                            <button
                              type="button"
                              onClick={() => handleOpenProfile(paciente)}
                              className="inline-flex items-center justify-center p-1.5 bg-surface-container-low hover:bg-surface-container border border-outline-variant text-on-surface-variant rounded-lg transition-colors cursor-pointer"
                              title="Ver Ficha Médica"
                            >
                              <Eye className="w-3.5 h-3.5" />
                            </button>
                            <button
                              type="button"
                              onClick={() => {
                                if (window.confirm('¿Estás seguro de que deseas eliminar este paciente?')) {
                                  borrarPaciente(paciente.id);
                                }
                              }}
                              disabled={cargando}
                              className="inline-flex items-center justify-center p-1.5 bg-error-container hover:bg-error-container/80 border border-error/20 text-error rounded-lg transition-colors cursor-pointer"
                              title="Eliminar"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Registrar Paciente Card Form */}
        <div className="xl:col-span-1 bg-surface-container-lowest border border-outline-variant rounded-2xl shadow-xs p-6 h-fit space-y-6">
          <div className="flex items-center gap-2 pb-4 border-b border-outline-variant">
            <UserPlus className="w-5 h-5 text-primary shrink-0" />
            <h3 className="font-bold text-on-surface">Registrar nuevo paciente</h3>
          </div>

           <form className="space-y-4 text-left">
             <div>
               <label className="block text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-1.5">Nombre *</label>
               <input
                 value={nuevoPaciente.nombre}
                 onChange={(event) => {
                   actualizarCampo('nombre', event.target.value);
                   if (erroresCampo.nombre) {
                     setErroresCampo(prev => ({ ...prev, nombre: '' }));
                   }
                 }}
                 placeholder="Nombre *"
                 autoComplete="given-name"
                  className={`w-full border rounded-lg p-2.5 text-on-surface bg-surface-container-low focus:bg-surface-container-lowest focus:outline-none focus:ring-2 text-sm transition-all ${
                    erroresCampo.nombre
                      ? 'border-red-500 focus:ring-red-300'
                      : 'border-outline-variant focus:ring-primary/10 focus:border-primary'
                 }`}
               />
               {erroresCampo.nombre && (
                 <p className="text-red-600 text-xs mt-1.5 flex items-center gap-1">
                   <span>⚠</span> {erroresCampo.nombre}
                 </p>
               )}
             </div>

             <div>
               <label className="block text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-1.5">Apellido *</label>
               <input
                 value={nuevoPaciente.apellido}
                 onChange={(event) => {
                   actualizarCampo('apellido', event.target.value);
                   if (erroresCampo.apellido) {
                     setErroresCampo(prev => ({ ...prev, apellido: '' }));
                   }
                 }}
                 placeholder="Apellido *"
                 autoComplete="family-name"
                  className={`w-full border rounded-lg p-2.5 text-on-surface bg-surface-container-low focus:bg-surface-container-lowest focus:outline-none focus:ring-2 text-sm transition-all ${
                    erroresCampo.apellido
                      ? 'border-red-500 focus:ring-red-300'
                      : 'border-outline-variant focus:ring-primary/10 focus:border-primary'
                 }`}
               />
               {erroresCampo.apellido && (
                 <p className="text-red-600 text-xs mt-1.5 flex items-center gap-1">
                   <span>⚠</span> {erroresCampo.apellido}
                 </p>
               )}
             </div>

             <div>
               <label className="block text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-1.5">DNI *</label>
               <input
                 value={nuevoPaciente.dni}
                 onChange={(event) => {
                   actualizarCampo('dni', event.target.value);
                   if (erroresCampo.dni) {
                     setErroresCampo(prev => ({ ...prev, dni: '' }));
                   }
                 }}
                 placeholder="DNI *"
                 autoComplete="off"
                  className={`w-full border rounded-lg p-2.5 text-on-surface bg-surface-container-low focus:bg-surface-container-lowest focus:outline-none focus:ring-2 text-sm font-mono transition-all ${
                    erroresCampo.dni
                      ? 'border-red-500 focus:ring-red-300'
                      : 'border-outline-variant focus:ring-primary/10 focus:border-primary'
                 }`}
               />
               {erroresCampo.dni && (
                 <p className="text-red-600 text-xs mt-1.5 flex items-center gap-1">
                   <span>⚠</span> {erroresCampo.dni}
                 </p>
               )}
             </div>

             <div>
               <label className="block text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-1.5">Teléfono (opcional)</label>
               <input
                 value={nuevoPaciente.telefono}
                 onChange={(event) => {
                   actualizarCampo('telefono', event.target.value);
                   if (erroresCampo.telefono) {
                     setErroresCampo(prev => ({ ...prev, telefono: '' }));
                   }
                 }}
                 placeholder="Teléfono (opcional)"
                 autoComplete="tel"
                  className={`w-full border rounded-lg p-2.5 text-on-surface bg-surface-container-low focus:bg-surface-container-lowest focus:outline-none focus:ring-2 text-sm transition-all ${
                    erroresCampo.telefono
                      ? 'border-red-500 focus:ring-red-300'
                      : 'border-outline-variant focus:ring-primary/10 focus:border-primary'
                 }`}
               />
               {erroresCampo.telefono && (
                 <p className="text-red-600 text-xs mt-1.5 flex items-center gap-1">
                   <span>⚠</span> {erroresCampo.telefono}
                 </p>
               )}
             </div>

             <div>
               <label className="block text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-1.5">Correo electrónico (opcional)</label>
               <input
                 type="email"
                 value={nuevoPaciente.email}
                 onChange={(event) => {
                   actualizarCampo('email', event.target.value);
                   if (erroresCampo.email) {
                     setErroresCampo(prev => ({ ...prev, email: '' }));
                   }
                 }}
                 placeholder="Correo electrónico (opcional)"
                 autoComplete="email"
                  className={`w-full border rounded-lg p-2.5 text-on-surface bg-surface-container-low focus:bg-surface-container-lowest focus:outline-none focus:ring-2 text-sm transition-all ${
                    erroresCampo.email
                      ? 'border-red-500 focus:ring-red-300'
                      : 'border-outline-variant focus:ring-primary/10 focus:border-primary'
                 }`}
               />
               {erroresCampo.email && (
                 <p className="text-red-600 text-xs mt-1.5 flex items-center gap-1">
                   <span>⚠</span> {erroresCampo.email}
                 </p>
               )}
             </div>

             <button
               type="button"
               onClick={handleRegistrar}
               disabled={cargando}
               className={`w-full py-2.5 px-4 bg-primary hover:bg-primary/95 text-white rounded-lg text-sm font-bold shadow-xs shadow-primary/15 transition-all cursor-pointer ${
                 cargando ? 'opacity-50 cursor-not-allowed transform-none' : 'hover:-translate-y-0.5'
               }`}
             >
               {cargando ? 'Procesando…' : 'Registrar paciente'}
             </button>
           </form>
        </div>
      </div>

      {/* Patient Detailed Profile Modal */}
      {selectedPatient && (() => {
        const initials = getInitials(selectedPatient.nombre, selectedPatient.apellido);
        const pId = selectedPatient.id || `RN-2026-${selectedPatient.dni}`;
        const patientNotes = clinicalNotes[selectedPatient.id] || [];

        return (
          <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs z-50 flex justify-center items-center p-4">
            <div className="bg-white rounded-2xl border border-slate-100 w-full max-w-3xl shadow-2xl relative overflow-hidden flex flex-col max-h-[90vh]">
              
              {/* Modal Header */}
              <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50">
                <div>
                  <h3 className="text-lg font-bold text-slate-900">Ficha del Paciente</h3>
                  <p className="text-xs text-slate-500 mt-1">Información general y notas clínicas</p>
                </div>
                <button
                  onClick={() => setSelectedPatient(null)}
                  className="p-1.5 text-slate-400 hover:text-slate-700 bg-white hover:bg-slate-100 border border-slate-200 rounded-full transition-all shadow-xs cursor-pointer"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              {/* Modal Content */}
              <div className="p-6 overflow-y-auto flex-1 space-y-6">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                  
                  {/* Left demographics card */}
                  <div className="lg:col-span-1 bg-slate-50/50 p-5 rounded-xl border border-slate-200/50 flex flex-col items-center text-center text-xs space-y-4">
                    <div className="w-28 h-28 rounded-xl bg-primary/10 text-primary flex items-center justify-center font-bold text-3xl">
                      {initials}
                    </div>
                    
                    <div>
                      <h4 className="text-base font-bold text-slate-900 leading-tight">{selectedPatient.nombre} {selectedPatient.apellido}</h4>
                      <p className="font-mono text-[10px] text-slate-400 font-bold mt-1 select-all">{pId}</p>
                    </div>

                    <div className="w-full text-left space-y-3 pt-3 border-t border-slate-200/50 text-slate-600">
                      <div>
                        <span className="block font-bold text-slate-400 uppercase text-[9px] tracking-wider mb-0.5">DNI</span>
                        <span className="font-semibold text-slate-800 font-mono">{selectedPatient.dni}</span>
                      </div>
                      <div>
                        <span className="block font-bold text-slate-400 uppercase text-[9px] tracking-wider mb-0.5">Contacto</span>
                        <span className="font-semibold text-slate-800">{selectedPatient.telefono || 'Sin teléfono'}</span>
                      </div>
                      <div>
                        <span className="block font-bold text-slate-400 uppercase text-[9px] tracking-wider mb-0.5">Email</span>
                        <span className="font-semibold text-slate-800 select-all break-all">{selectedPatient.email || 'Sin correo electrónico'}</span>
                      </div>
                    </div>
                  </div>

                  {/* Right clinical data section */}
                  <div className="lg:col-span-2 space-y-6">

                    {/* Clinical Notes */}
                    <div className="border border-slate-100 p-4 rounded-xl space-y-4 bg-white text-xs">
                      <h4 className="font-bold text-slate-800 flex items-center gap-1.5">
                        <FileText className="w-4 h-4 text-blue-600" />
                        <span>Notas Médicas</span>
                      </h4>

                      {patientNotes.length === 0 ? (
                        <p className="text-slate-400 italic">Sin notas registradas.</p>
                      ) : (
                        <div className="space-y-2.5 max-h-40 overflow-y-auto pr-1">
                          {patientNotes.map(n => (
                            <div key={n.id} className="bg-slate-50 p-2.5 rounded-lg border border-slate-100 space-y-1">
                              <div className="flex justify-between items-center font-bold text-slate-700">
                                <span>{n.doctor}</span>
                                <span className="text-[10px] text-slate-400 font-mono">{n.date}</span>
                              </div>
                              <p className="text-slate-600 italic leading-relaxed">"{n.content}"</p>
                            </div>
                          ))}
                        </div>
                      )}

                      <form onSubmit={(e) => handleAddNote(e, selectedPatient.id)} className="flex gap-2">
                        <input
                          type="text"
                          placeholder="Añadir observaciones médicas..."
                          value={newNoteText}
                          onChange={(e) => setNewNoteText(e.target.value)}
                          className="flex-1 border border-slate-200 rounded-lg p-2 text-xs text-slate-700 bg-slate-50 focus:bg-white focus:outline-none focus:ring-1 focus:ring-blue-500"
                        />
                        <button
                          type="submit"
                          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-bold transition-all shadow-xs cursor-pointer shrink-0"
                        >
                          Añadir nota
                        </button>
                      </form>
                    </div>

                  </div>

                </div>
              </div>

              {/* Modal Footer */}
              <div className="p-4 border-t border-slate-100 flex justify-end bg-slate-50">
                <button
                  onClick={() => setSelectedPatient(null)}
                  className="px-4 py-2 bg-slate-200 hover:bg-slate-300 text-slate-700 text-sm font-semibold rounded-lg cursor-pointer"
                >
                  Cerrar
                </button>
              </div>

            </div>
          </div>
        );
      })()}

    </div>
  );
}

export default GestionPacientesView;
