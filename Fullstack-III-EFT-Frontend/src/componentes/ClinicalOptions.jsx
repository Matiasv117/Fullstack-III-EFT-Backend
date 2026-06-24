import { useState } from 'react';
import { 
  Stethoscope, Calendar, FileText, Users, Activity, 
  Settings, ChevronRight, Clock, AlertCircle, CheckCircle
} from 'lucide-react';
import { useToast } from '../hooks/useToast';
import ToastContainer from './Toast';

function ClinicalOptions() {
  const [selectedOption, setSelectedOption] = useState(null);
  const { toasts, removeToast, success, error, info } = useToast();

  const clinicalOptions = [
    {
      id: 'consultas',
      icon: Stethoscope,
      title: 'Gestión de Consultas',
      description: 'Programar y administrar consultas médicas',
      color: 'blue',
      items: [
        { name: 'Agendar consulta', action: () => info('Módulo de Agenda: Cargando calendario...', 3000) },
        { name: 'Ver calendario', action: () => info('Abriendo vista de calendario médico...', 3000) },
        { name: 'Historial de consultas', action: () => info('Buscando historial de consultas...', 3000) },
      ]
    },
    {
      id: 'especialidades',
      icon: Users,
      title: 'Especialidades Médicas',
      description: 'Administrar especialidades and médicos',
      color: 'green',
      items: [
        { name: 'Lista de especialidades', action: () => info('Obteniendo lista de especialidades activas...', 3000) },
        { name: 'Médicos disponibles', action: () => info('Consultando staff de médicos de turno...', 3000) },
        { name: 'Asignar especialidad', action: () => info('Abriendo asignador de especialidades...', 3000) },
      ]
    },
    {
      id: 'examenes',
      icon: FileText,
      title: 'Exámenes y Laboratorios',
      description: 'Gestionar exámenes médicos y resultados',
      color: 'purple',
      items: [
        { name: 'Solicitar examen', action: () => info('Abriendo formulario de solicitud de examen...', 3000) },
        { name: 'Ver resultados', action: () => info('Cargando resultados de laboratorio...', 3000) },
        { name: 'Historial de exámenes', action: () => info('Recuperando historial de exámenes del paciente...', 3000) },
      ]
    },
    {
      id: 'urgencias',
      icon: AlertCircle,
      title: 'Urgencias y Emergencias',
      description: 'Manejo de casos urgentes',
      color: 'red',
      items: [
        { name: 'Triaje de urgencias', action: () => info('Iniciando protocolo de clasificación de urgencias...', 3000) },
        { name: 'Casos activos', action: () => info('Listando pacientes ingresados en box de urgencia...', 3000) },
        { name: 'Protocolos de emergencia', action: () => info('Cargando manual de protocolos y procedimientos...', 3000) },
      ]
    },
    {
      id: 'farmacia',
      icon: Activity,
      title: 'Farmacia y Medicamentos',
      description: 'Control de medicamentos y recetas',
      color: 'orange',
      items: [
        { name: 'Inventario de medicamentos', action: () => info('Consultando stock actual de bodega...', 3000) },
        { name: 'Generar recetas', action: () => info('Abriendo generador de recetas médicas digitales...', 3000) },
        { name: 'Control de stock', action: () => info('Verificando alertas de bajo stock...', 3000) },
      ]
    },
    {
      id: 'configuracion',
      icon: Settings,
      title: 'Configuración Clínica',
      description: 'Ajustes del sistema clínico',
      color: 'gray',
      items: [
        { name: 'Horarios de atención', action: () => info('Configurando rango de atención por bloques...', 3000) },
        { name: 'Parámetros médicos', action: () => info('Abriendo edición de constantes y parámetros clínicos...', 3000) },
        { name: 'Integraciones', action: () => info('Verificando canales de integración con Fonasa...', 3000) },
      ]
    }
  ];

  const colorClasses = {
    blue: 'bg-blue-50 border-blue-200 text-blue-700 hover:bg-blue-100',
    green: 'bg-green-50 border-green-200 text-green-700 hover:bg-green-100',
    purple: 'bg-purple-50 border-purple-200 text-purple-700 hover:bg-purple-100',
    red: 'bg-red-50 border-red-200 text-red-700 hover:bg-red-100',
    orange: 'bg-orange-50 border-orange-200 text-orange-700 hover:bg-orange-100',
    gray: 'bg-gray-50 border-gray-200 text-gray-700 hover:bg-gray-100',
  };

  const iconColorClasses = {
    blue: 'text-blue-600',
    green: 'text-green-600',
    purple: 'text-purple-600',
    red: 'text-red-600',
    orange: 'text-orange-600',
    gray: 'text-gray-600',
  };

  return (
    <>
      <ToastContainer toasts={toasts} onRemove={removeToast} />
      <div className="ml-[260px] pt-24 p-gutter min-h-screen">
        <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-slate-900 mb-2">Opciones Clínicas</h1>
          <p className="text-slate-600">Gestión integral de servicios médicos y clínicos</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {clinicalOptions.map((option) => {
            const Icon = option.icon;
            return (
              <div
                key={option.id}
                className={`border rounded-xl p-6 cursor-pointer transition-all duration-200 ${colorClasses[option.color]}`}
                onClick={() => setSelectedOption(selectedOption?.id === option.id ? null : option)}
              >
                <div className="flex items-start gap-4">
                  <div className={`p-3 rounded-lg bg-white ${iconColorClasses[option.color]}`}>
                    <Icon className="w-6 h-6" />
                  </div>
                  <div className="flex-1">
                    <h3 className="font-bold text-lg mb-1">{option.title}</h3>
                    <p className="text-sm opacity-80 mb-3">{option.description}</p>
                    <ChevronRight className={`w-4 h-4 transition-transform ${selectedOption?.id === option.id ? 'rotate-90' : ''}`} />
                  </div>
                </div>

                {selectedOption?.id === option.id && (
                  <div className="mt-4 pt-4 border-t border-current/20">
                    <ul className="space-y-2">
                      {option.items.map((item, index) => (
                        <li key={index}>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              item.action();
                            }}
                            className="w-full text-left px-3 py-2 rounded-lg bg-white/50 hover:bg-white transition-colors text-sm font-medium"
                          >
                            {item.name}
                          </button>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        <div className="mt-8 p-6 bg-blue-50 border border-blue-200 rounded-xl">
          <div className="flex items-start gap-4">
            <Clock className="w-6 h-6 text-blue-600 mt-1" />
            <div>
              <h3 className="font-bold text-blue-900 mb-2">Estado del Sistema</h3>
              <div className="space-y-2 text-sm text-blue-800">
                <div className="flex items-center gap-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>Servicios de pacientes: Activo</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>Servicios de optimización: Activo</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>Servicios de notificaciones: Activo</span>
                </div>
                <div className="flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 text-amber-600" />
                  <span>Sincronización de base de datos: Pendiente de revisión</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    </>
  );
}

export default ClinicalOptions;
