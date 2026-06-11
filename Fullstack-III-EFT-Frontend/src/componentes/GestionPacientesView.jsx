import { useState } from 'react'
import { 
  Plus, RefreshCw, Trash2, Eye, UserPlus, FileText, 
  Calendar, User, CreditCard, Phone, Mail, MapPin, 
  Pill, X, ChevronDown, Check
} from 'lucide-react'

// Mock data generator for extra clinical profile details based on patient DNI/ID
const getMockDetails = (paciente) => {
  const code = paciente.id || paciente.dni || '0';
  const lastDigit = parseInt(code.toString().slice(-1)) || 0;
  
  const priorities = ['Standard', 'Urgent', 'Emergency'];
  const priority = priorities[lastDigit % 3];

  const depts = ['General', 'Cardiology', 'Pediatrics', 'Emergency Care', 'Traumatology'];
  const dept = depts[lastDigit % 5];

  const doctors = ['Dr. Sofia Lopez', 'Dr. Alejandro Diaz', 'Dr. Elena Gomez', 'Dr. Elena Cruz'];
  const doctor = doctors[lastDigit % 4];

  const statusList = ['Waiting', 'In Consultation', 'Completed', 'Pending'];
  const status = statusList[lastDigit % 4];

  const genders = ['Female', 'Male', 'Female', 'Male'];
  const gender = genders[lastDigit % 4];

  const bloodTypes = ['O+', 'A+', 'B+', 'O-', 'AB+'];
  const bloodType = bloodTypes[lastDigit % 5];

  const photoUrls = [
    'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&q=80&w=200&h=200',
    'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200&h=200',
    'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200&h=200',
    'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=200&h=200'
  ];
  const photoUrl = photoUrls[lastDigit % 4];

  return { priority, dept, doctor, status, gender, bloodType, photoUrl };
}

function GestionPacientesView({
  pacientes,
  nuevoPaciente,
  cargando,
  mensaje,
  error,
  formValido,
  actualizarCampo,
  registrar,
  agregarALista,
  borrarPaciente,
  recargarPacientes,
}) {
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [clinicalNotes, setClinicalNotes] = useState({});
  const [newNoteText, setNewNoteText] = useState('');
  const [refillStatus, setRefillStatus] = useState({});

  const handleOpenProfile = (paciente) => {
    setSelectedPatient(paciente);
  };

  const handleAddNote = (e, patientId) => {
    e.preventDefault();
    if (!newNoteText.trim()) return;
    const author = "Dr. Elena Cruz";
    const date = new Date().toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' });
    const newNote = {
      id: Date.now(),
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

  const handleTriggerRefill = (medId) => {
    setRefillStatus(prev => ({ ...prev, [medId]: 'Requested' }));
    setTimeout(() => {
      setRefillStatus(prev => {
        const copy = { ...prev };
        delete copy[medId];
        return copy;
      });
    }, 2000);
  };

  return (
    <div className="space-y-8">
      {/* Header */}
      <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Gestión de Pacientes</h2>
          <p className="text-sm text-slate-500 mt-1">
            Registro de pacientes y derivación a la lista de espera desde un único flujo de trabajo.
          </p>
        </div>
        <button
          type="button"
          onClick={recargarPacientes}
          disabled={cargando}
          className="inline-flex items-center gap-2 px-4 py-2 border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-semibold rounded-lg text-sm shadow-xs transition-colors cursor-pointer"
        >
          <RefreshCw className={`w-4 h-4 ${cargando ? 'animate-spin' : ''}`} />
          <span>Actualizar lista</span>
        </button>
      </header>

      {/* Alert Feedbacks */}
      <div className="space-y-3">
        {mensaje && (
          <div className="bg-emerald-50 border border-emerald-150 border-emerald-250/20 text-emerald-800 p-4 rounded-xl text-sm font-semibold flex items-center gap-2">
            <Check className="w-4 h-4 shrink-0" />
            <span>{mensaje}</span>
          </div>
        )}
        {error && (
          <div className="bg-rose-55 bg-rose-50 border border-rose-150 border-rose-250/20 text-rose-800 p-4 rounded-xl text-sm font-semibold flex items-center gap-2">
            <X className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-4 gap-8">
        {/* Pacientes Registrados Table */}
        <div className="xl:col-span-3 bg-white border border-slate-100 rounded-2xl shadow-xs overflow-hidden">
          <div className="p-5 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
            <h3 className="font-bold text-slate-800">Pacientes registrados</h3>
            <span className="bg-blue-50 text-blue-700 text-xs px-2.5 py-1 rounded-full font-bold">
              {pacientes.length} Registros
            </span>
          </div>

          <div className="overflow-x-auto w-full">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/75 text-slate-500 font-semibold text-xs uppercase tracking-wider border-b border-slate-100">
                  <th className="py-4 px-6">Paciente</th>
                  <th className="py-4 px-6">Identificación</th>
                  <th className="py-4 px-6">Asignación</th>
                  <th className="py-4 px-6 text-center">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-sm">
                {pacientes.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="py-12 px-6 text-center text-slate-400">
                      No hay pacientes registrados todavía.
                    </td>
                  </tr>
                ) : (
                  pacientes.map((paciente) => {
                    const extra = getMockDetails(paciente);
                    return (
                      <tr key={paciente.id} className="hover:bg-slate-50/50 transition-colors">
                        <td className="py-4 px-6">
                          <div className="flex items-center gap-3">
                            <img
                              src={extra.photoUrl}
                              alt={paciente.nombre}
                              className="w-10 h-10 rounded-full border border-slate-200 object-cover cursor-pointer hover:opacity-80"
                              onClick={() => handleOpenProfile(paciente)}
                            />
                            <div>
                              <p 
                                onClick={() => handleOpenProfile(paciente)}
                                className="font-semibold text-slate-800 hover:text-blue-600 cursor-pointer transition-colors"
                              >
                                {paciente.nombre} {paciente.apellido}
                              </p>
                              <span className="text-xs text-slate-400 font-medium">{paciente.email || 'Sin email'}</span>
                            </div>
                          </div>
                        </td>
                        <td className="py-4 px-6">
                          <div className="space-y-0.5">
                            <p className="font-mono text-xs text-slate-700">DNI: {paciente.dni}</p>
                            <p className="text-xs text-slate-400">Contacto: {paciente.telefono || 'Sin teléfono'}</p>
                          </div>
                        </td>
                        <td className="py-4 px-6">
                          <div className="space-y-1">
                            <span className={`inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-bold ${
                              extra.priority === 'Emergency' ? 'bg-rose-50 text-rose-700 border border-rose-100' :
                              extra.priority === 'Urgent' ? 'bg-amber-50 text-amber-700 border border-amber-100' :
                              'bg-emerald-50 text-emerald-700 border border-emerald-100'
                            }`}>
                              {extra.priority}
                            </span>
                            <p className="text-xs font-semibold text-slate-650 text-slate-600">{extra.doctor}</p>
                          </div>
                        </td>
                        <td className="py-4 px-6">
                          <div className="flex items-center gap-2 justify-center">
                            <button
                              type="button"
                              onClick={() => agregarALista(paciente.id)}
                              disabled={cargando}
                              className="inline-flex items-center justify-center py-1.5 px-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-bold transition-all shadow-xs shadow-blue-100 cursor-pointer"
                            >
                              Agregar a lista
                            </button>
                            <button
                              type="button"
                              onClick={() => handleOpenProfile(paciente)}
                              className="inline-flex items-center justify-center p-1.5 bg-slate-50 hover:bg-slate-100 border border-slate-200 text-slate-600 rounded-lg transition-colors cursor-pointer"
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
                              className="inline-flex items-center justify-center p-1.5 bg-rose-50 hover:bg-rose-100 border border-rose-200 text-rose-600 rounded-lg transition-colors cursor-pointer"
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
        <div className="xl:col-span-1 bg-white border border-slate-100 rounded-2xl shadow-xs p-6 h-fit space-y-6">
          <div className="flex items-center gap-2 pb-4 border-b border-slate-100">
            <UserPlus className="w-5 h-5 text-blue-600 shrink-0" />
            <h3 className="font-bold text-slate-900">Registrar nuevo paciente</h3>
          </div>

          <form className="space-y-4 text-left">
            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">Nombre *</label>
              <input
                value={nuevoPaciente.nombre}
                onChange={(event) => actualizarCampo('nombre', event.target.value)}
                placeholder="Nombre *"
                autoComplete="given-name"
                className="w-full border border-slate-200 rounded-lg p-2.5 text-slate-700 bg-slate-50/50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/10 focus:border-blue-500 text-sm transition-all"
              />
            </div>

            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">Apellido *</label>
              <input
                value={nuevoPaciente.apellido}
                onChange={(event) => actualizarCampo('apellido', event.target.value)}
                placeholder="Apellido *"
                autoComplete="family-name"
                className="w-full border border-slate-200 rounded-lg p-2.5 text-slate-700 bg-slate-50/50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/10 focus:border-blue-500 text-sm transition-all"
              />
            </div>

            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">DNI *</label>
              <input
                value={nuevoPaciente.dni}
                onChange={(event) => actualizarCampo('dni', event.target.value)}
                placeholder="DNI *"
                autoComplete="off"
                className="w-full border border-slate-200 rounded-lg p-2.5 text-slate-700 bg-slate-50/50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/10 focus:border-blue-500 text-sm font-mono transition-all"
              />
            </div>

            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">Teléfono (opcional)</label>
              <input
                value={nuevoPaciente.telefono}
                onChange={(event) => actualizarCampo('telefono', event.target.value)}
                placeholder="Teléfono (opcional)"
                autoComplete="tel"
                className="w-full border border-slate-200 rounded-lg p-2.5 text-slate-700 bg-slate-50/50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/10 focus:border-blue-500 text-sm transition-all"
              />
            </div>

            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">Correo electrónico (opcional)</label>
              <input
                type="email"
                value={nuevoPaciente.email}
                onChange={(event) => actualizarCampo('email', event.target.value)}
                placeholder="Correo electrónico (opcional)"
                autoComplete="email"
                className="w-full border border-slate-200 rounded-lg p-2.5 text-slate-700 bg-slate-50/50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/10 focus:border-blue-500 text-sm transition-all"
              />
            </div>

            <button
              type="button"
              onClick={registrar}
              disabled={!formValido || cargando}
              className={`w-full py-2.5 px-4 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-bold shadow-xs shadow-blue-100 transition-all cursor-pointer ${
                (!formValido || cargando) ? 'opacity-50 cursor-not-allowed transform-none' : 'hover:-translate-y-0.5'
              }`}
            >
              {cargando ? 'Procesando…' : 'Registrar paciente'}
            </button>
          </form>
        </div>
      </div>

      {/* Patient Detailed Profile Modal */}
      {selectedPatient && (() => {
        const extra = getMockDetails(selectedPatient);
        const pId = selectedPatient.id || `RN-2026-${selectedPatient.dni}`;
        const patientNotes = clinicalNotes[selectedPatient.id] || [
          { id: 1, doctor: "Dr. Elena Gomez", date: "15 de May, 2026", content: "Paciente con signos vitales normales. Se mantiene control de presión arterial." },
          { id: 2, doctor: "Dr. Sofia Lopez", date: "10 de Abr, 2026", content: "Monitoreo mensual exitoso. Sugerido plan nutricional bajo en grasas." }
        ];

        return (
          <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs z-50 flex justify-center items-center p-4">
            <div className="bg-white rounded-2xl border border-slate-100 w-full max-w-5xl shadow-2xl relative overflow-hidden flex flex-col max-h-[90vh]">
              
              {/* Modal Header */}
              <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50">
                <div>
                  <h3 className="text-lg font-bold text-slate-900">Historial Clínico del Paciente</h3>
                  <p className="text-xs text-slate-550 text-slate-500 mt-1">Detalle clínico unificado de atención primaria y derivación</p>
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
                <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
                  
                  {/* Left demographics card */}
                  <div className="lg:col-span-1 bg-slate-50/50 p-5 rounded-xl border border-slate-150 border-slate-200/50 flex flex-col items-center text-center text-xs space-y-4">
                    <img src={extra.photoUrl} alt={selectedPatient.nombre} className="w-28 h-28 rounded-xl border border-slate-200 object-cover shadow-xs" />
                    
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
                        <span className="block font-bold text-slate-400 uppercase text-[9px] tracking-wider mb-0.5">Género / Tipo de sangre</span>
                        <span className="font-semibold text-slate-800">{extra.gender} (</span>
                        <span className="font-bold text-rose-600 bg-rose-50 px-1.5 py-0.5 rounded text-[10px]">{extra.bloodType}</span>
                        <span className="font-semibold text-slate-800">)</span>
                      </div>
                      <div>
                        <span className="block font-bold text-slate-400 uppercase text-[9px] tracking-wider mb-0.5">Contacto</span>
                        <span className="font-semibold text-slate-800">{selectedPatient.telefono || 'Sin teléfono'}</span>
                      </div>
                      <div>
                        <span className="block font-bold text-slate-400 uppercase text-[9px] tracking-wider mb-0.5">Email</span>
                        <span className="font-semibold text-slate-800 select-all break-all">{selectedPatient.email || 'Sin correo electrónico'}</span>
                      </div>
                      <div>
                        <span className="block font-bold text-slate-400 uppercase text-[9px] tracking-wider mb-0.5">Especialidad</span>
                        <span className="font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded text-[10px]">{extra.dept}</span>
                      </div>
                    </div>
                  </div>

                  {/* Right clinical data section */}
                  <div className="lg:col-span-3 space-y-6">
                    
                    {/* Clinical History Timeline */}
                    <div className="border border-slate-100 p-5 rounded-xl bg-white space-y-4 shadow-2xs">
                      <h4 className="font-bold text-slate-800 text-sm">Línea de Tiempo Médica</h4>
                      
                      <div className="relative pl-6 border-l-2 border-slate-100 space-y-4 ml-2 py-1 text-xs">
                        <div className="relative">
                          <div className="absolute -left-[30px] top-1 w-3.5 h-3.5 rounded-full border-2 border-blue-600 bg-white" />
                          <p className="font-bold text-slate-800">18 de May, 2026 - Consulta General de Control</p>
                          <p className="text-slate-500 mt-0.5">Paciente asistió a control general. Presión arterial dentro de rango normal (120/80).</p>
                        </div>
                        <div className="relative">
                          <div className="absolute -left-[30px] top-1 w-3.5 h-3.5 rounded-full border-2 border-amber-500 bg-white" />
                          <p className="font-bold text-slate-800">04 de May, 2026 - Actualización de Medicación</p>
                          <p className="text-slate-500 mt-0.5">Se renovó receta de Atorvastatina. Dosificación: 20mg diario.</p>
                        </div>
                        <div className="relative">
                          <div className="absolute -left-[30px] top-1 w-3.5 h-3.5 rounded-full border-2 border-rose-500 bg-white" />
                          <p className="font-bold text-slate-800">12 de Abr, 2026 - Resultados de Laboratorios Recibidos</p>
                          <p className="text-slate-500 mt-0.5">Lipid Panel completado. Se observa leve mejora en el colesterol total.</p>
                        </div>
                      </div>
                    </div>

                    {/* Bottom widgets grid */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      
                      {/* Medications list widget */}
                      <div className="border border-slate-100 p-4 rounded-xl space-y-3 text-xs bg-white">
                        <h4 className="font-bold text-slate-800 flex items-center gap-1.5">
                          <Pill className="w-4 h-4 text-blue-600" />
                          <span>Medicamentos Activos</span>
                        </h4>
                        
                        <div className="space-y-2.5">
                          <div className="flex justify-between items-center bg-slate-50 p-2.5 rounded-lg border border-slate-100">
                            <div>
                              <p className="font-bold text-slate-800">Atorvastatina (20mg, diario)</p>
                              <p className="text-[10px] text-slate-400">Control de Colesterol</p>
                            </div>
                            <button
                              onClick={() => handleTriggerRefill('med1')}
                              className="text-[10px] uppercase tracking-wide font-extrabold py-1 px-2.5 rounded-md border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 transition-all cursor-pointer"
                            >
                              {refillStatus['med1'] ? 'Solicitado' : 'Recetar'}
                            </button>
                          </div>
                          
                          <div className="flex justify-between items-center bg-slate-50 p-2.5 rounded-lg border border-slate-100">
                            <div>
                              <p className="font-bold text-slate-800">Lisinopril (10mg, diario)</p>
                              <p className="text-[10px] text-slate-400">Control de Hipertensión</p>
                            </div>
                            <button
                              onClick={() => handleTriggerRefill('med2')}
                              className="text-[10px] uppercase tracking-wide font-extrabold py-1 px-2.5 rounded-md border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 transition-all cursor-pointer"
                            >
                              {refillStatus['med2'] ? 'Solicitado' : 'Recetar'}
                            </button>
                          </div>
                        </div>
                      </div>

                      {/* Laboratory SVG Sparklines widget */}
                      <div className="border border-slate-100 p-4 rounded-xl space-y-3 text-xs bg-white">
                        <h4 className="font-bold text-slate-800 flex items-center gap-1.5">
                          <FileText className="w-4 h-4 text-blue-600" />
                          <span>Laboratorios Recientes</span>
                        </h4>

                        <div className="space-y-3">
                          <div className="space-y-1.5">
                            <div className="flex justify-between items-baseline">
                              <span className="font-semibold text-slate-700">Colesterol Total</span>
                              <strong className="text-slate-900 font-extrabold">215 mg/dL</strong>
                            </div>
                            
                            <div className="flex justify-between items-center bg-slate-50 py-1.5 px-3 rounded-lg border border-slate-100">
                              <span className="text-[9px] text-slate-400 font-mono font-bold uppercase tracking-wider">Hist. Trend</span>
                              <svg width="100" height="24">
                                <polyline
                                  fill="none"
                                  stroke="#3b82f6"
                                  strokeWidth="2"
                                  strokeLinecap="round"
                                  points="0,20 25,12 50,22 75,5 100,10"
                                />
                                <circle cx="100" cy="10" r="3.5" className="fill-blue-600 stroke-white stroke-2" />
                              </svg>
                            </div>
                          </div>

                          <div className="space-y-1.5">
                            <div className="flex justify-between items-baseline">
                              <span className="font-semibold text-slate-700">Hemoglobina Glicada (HbA1c)</span>
                              <strong className="text-rose-600 font-extrabold">6.8%</strong>
                            </div>
                            
                            <div className="flex justify-between items-center bg-slate-50 py-1.5 px-3 rounded-lg border border-slate-100">
                              <span className="text-[9px] text-slate-400 font-mono font-bold uppercase tracking-wider">Hist. Trend</span>
                              <svg width="100" height="24">
                                <polyline
                                  fill="none"
                                  stroke="#3b82f6"
                                  strokeWidth="2"
                                  strokeLinecap="round"
                                  points="0,18 25,16 50,10 75,12 100,4"
                                />
                                <circle cx="100" cy="4" r="3.5" className="fill-rose-600 stroke-white stroke-2" />
                              </svg>
                            </div>
                          </div>
                        </div>
                      </div>

                    </div>

                    {/* Clinical Notes formulation */}
                    <div className="border border-slate-100 p-4 rounded-xl space-y-4 bg-white text-xs">
                      <h4 className="font-bold text-slate-800 flex items-center gap-1.5">
                        <FileText className="w-4 h-4 text-blue-600" />
                        <span>Notas Médicas del Portal</span>
                      </h4>

                      <div className="space-y-2.5 max-h-40 overflow-y-auto pr-1">
                        {patientNotes.map(n => (
                          <div key={n.id} className="bg-slate-50 p-2.5 rounded-lg border border-slate-100 space-y-1">
                            <div className="flex justify-between items-center font-bold text-slate-700">
                              <span>{n.doctor}</span>
                              <span className="text-[10px] text-slate-400 font-mono">{n.date}</span>
                            </div>
                            <p className="text-slate-655 text-slate-600 italic leading-relaxed">"{n.content}"</p>
                          </div>
                        ))}
                      </div>

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
