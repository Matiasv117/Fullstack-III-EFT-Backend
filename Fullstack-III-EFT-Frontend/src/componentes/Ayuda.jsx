import { useState } from 'react';
import { 
  HelpCircle, Book, Video, MessageSquare, Phone, Mail, 
  Search, ChevronDown, ChevronUp, ExternalLink, CheckCircle
} from 'lucide-react';

function Ayuda() {
  const [expandedFaq, setExpandedFaq] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  const faqs = [
    {
      id: 1,
      question: '¿Cómo registro un nuevo paciente?',
      answer: 'Para registrar un paciente, ve a la sección "Pacientes", completa el formulario con los datos requeridos (nombre, apellido, DNI) y haz clic en "Registrar paciente". Los campos de teléfono y email son opcionales.'
    },
    {
      id: 2,
      question: '¿Cómo agrego un paciente a la lista de espera?',
      answer: 'En la sección "Pacientes", cada paciente registrado tiene un botón "Agregar a lista". Haz clic en este botón para agregar el paciente a la lista de espera. Podrás especificar la gravedad y la interconsulta.'
    },
    {
      id: 3,
      question: '¿Qué significa el error de sincronización de base de datos?',
      answer: 'Este error indica que hay un problema de conexión entre la aplicación y la base de datos. Verifica que los servicios backend estén corriendo correctamente. Si el problema persiste, contacta al administrador del sistema.'
    },
    {
      id: 4,
      question: '¿Cómo cambio el estado de un paciente en la lista de espera?',
      answer: 'En la sección "Lista de Espera", cada paciente tiene un selector de estado. Puedes cambiar entre PENDIENTE, ASIGNADA y FINALIZADA según el estado actual del paciente.'
    },
    {
      id: 5,
      question: '¿Cómo funcionan las opciones clínicas?',
      answer: 'La sección "Clínicas" (Clinical Options) proporciona acceso a diversas funciones médicas como gestión de consultas, especialidades, exámenes, urgencias, farmacia y configuración del sistema.'
    },
    {
      id: 6,
      question: '¿Cómo veo el historial de un paciente?',
      answer: 'En la sección "Pacientes", haz clic en el icono de ojo (👁️) junto al paciente para ver su ficha médica completa, incluyendo historial, medicamentos activos y notas médicas.'
    }
  ];

  const helpResources = [
    {
      title: 'Documentación del Sistema',
      description: 'Guía completa de uso del sistema',
      icon: Book,
      action: () => window.open('#', '_blank'),
      color: 'blue'
    },
    {
      title: 'Tutoriales en Video',
      description: 'Videos explicativos paso a paso',
      icon: Video,
      action: () => window.open('#', '_blank'),
      color: 'purple'
    },
    {
      title: 'Foro de Usuarios',
      description: 'Comunidad de usuarios y soporte',
      icon: MessageSquare,
      action: () => window.open('#', '_blank'),
      color: 'green'
    }
  ];

  const contactMethods = [
    {
      icon: Phone,
      title: 'Teléfono',
      value: '+56 2 2345 6789',
      description: 'Lunes a viernes, 9:00 - 18:00'
    },
    {
      icon: Mail,
      title: 'Email',
      value: 'soporte@rednorte.cl',
      description: 'Respuesta en 24 horas'
    }
  ];

  const filteredFaqs = faqs.filter(faq =>
    faq.question.toLowerCase().includes(searchQuery.toLowerCase()) ||
    faq.answer.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="ml-[260px] pt-24 p-gutter min-h-screen">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-slate-900 mb-2">Centro de Ayuda</h1>
          <p className="text-slate-600">Encuentra respuestas a tus preguntas y obtén soporte técnico</p>
        </div>

        {/* Search Bar */}
        <div className="mb-8">
          <div className="relative max-w-2xl">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5" />
            <input
              type="text"
              placeholder="Buscar en la ayuda..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-12 pr-4 py-3 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
        </div>

        {/* Quick Help Resources */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          {helpResources.map((resource, index) => {
            const Icon = resource.icon;
            const colorClasses = {
              blue: 'bg-blue-50 border-blue-200 text-blue-700 hover:bg-blue-100',
              purple: 'bg-purple-50 border-purple-200 text-purple-700 hover:bg-purple-100',
              green: 'bg-green-50 border-green-200 text-green-700 hover:bg-green-100',
            };
            return (
              <button
                key={index}
                onClick={resource.action}
                className={`border rounded-xl p-6 text-left transition-all duration-200 ${colorClasses[resource.color]}`}
              >
                <Icon className="w-8 h-8 mb-3" />
                <h3 className="font-bold text-lg mb-1">{resource.title}</h3>
                <p className="text-sm opacity-80">{resource.description}</p>
                <ExternalLink className="w-4 h-4 mt-3 opacity-60" />
              </button>
            );
          })}
        </div>

        {/* FAQ Section */}
        <div className="mb-8">
          <h2 className="text-2xl font-bold text-slate-900 mb-4">Preguntas Frecuentes</h2>
          <div className="space-y-3">
            {filteredFaqs.map((faq) => (
              <div key={faq.id} className="border border-slate-200 rounded-xl overflow-hidden">
                <button
                  onClick={() => setExpandedFaq(expandedFaq === faq.id ? null : faq.id)}
                  className="w-full px-6 py-4 flex justify-between items-left bg-white hover:bg-slate-50 transition-colors"
                >
                  <span className="font-semibold text-left flex-1">{faq.question}</span>
                  {expandedFaq === faq.id ? (
                    <ChevronUp className="w-5 h-5 text-slate-400 ml-4 flex-shrink-0" />
                  ) : (
                    <ChevronDown className="w-5 h-5 text-slate-400 ml-4 flex-shrink-0" />
                  )}
                </button>
                {expandedFaq === faq.id && (
                  <div className="px-6 py-4 bg-slate-50 border-t border-slate-200">
                    <p className="text-slate-700">{faq.answer}</p>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Contact Section */}
        <div className="bg-slate-50 border border-slate-200 rounded-xl p-6">
          <h2 className="text-2xl font-bold text-slate-900 mb-4">Contacto de Soporte</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {contactMethods.map((method, index) => {
              const Icon = method.icon;
              return (
                <div key={index} className="flex items-start gap-4">
                  <div className="p-3 bg-white rounded-lg border border-slate-200">
                    <Icon className="w-6 h-6 text-blue-600" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-slate-900">{method.title}</h3>
                    <p className="text-slate-700 font-medium">{method.value}</p>
                    <p className="text-sm text-slate-500">{method.description}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* System Status */}
        <div className="mt-8 p-6 bg-green-50 border border-green-200 rounded-xl">
          <div className="flex items-start gap-4">
            <CheckCircle className="w-6 h-6 text-green-600 mt-1" />
            <div>
              <h3 className="font-bold text-green-900 mb-2">Estado del Sistema</h3>
              <p className="text-sm text-green-800">
                Todos los servicios están operativos. Si experimentas problemas, verifica tu conexión a internet o intenta recargar la página.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Ayuda;
