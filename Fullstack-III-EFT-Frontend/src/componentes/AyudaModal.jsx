import { X } from 'lucide-react';

const AyudaModal = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50" onClick={onClose}>
      <div className="bg-white dark:bg-surface-dim rounded-xl shadow-2xl w-full max-w-lg mx-4 p-6" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-on-surface dark:text-white">Ayuda del Sistema</h2>
          <button onClick={onClose} className="p-2 hover:bg-surface-container-high rounded-lg cursor-pointer">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="space-y-4 text-sm">
          <section>
            <h3 className="font-semibold text-primary mb-2">RedNorte — Sistema de Gestión Hospitalaria</h3>
            <p className="text-on-surface-variant leading-relaxed">
              Plataforma integral para la administración de pacientes, listas de espera,
              optimización de citas y notificaciones en establecimientos de salud pública.
            </p>
          </section>

          <section>
            <h3 className="font-semibold mb-1">Módulos Principales</h3>
            <ul className="text-on-surface-variant space-y-1 list-disc list-inside">
              <li><strong>Dashboard</strong> — Resumen del estado del sistema</li>
              <li><strong>Pacientes</strong> — Registro y gestión de pacientes</li>
              <li><strong>Lista de Espera</strong> — Administración de esperas por gravedad</li>
              <li><strong>Optimización</strong> — Asignación inteligente de citas</li>
              <li><strong>Notificaciones</strong> — Envío de alertas a pacientes</li>
              <li><strong>Reportes</strong> — Estadísticas y auditoría del sistema</li>
              <li><strong>Gestión de Usuarios</strong> — Administración de funcionarios y roles</li>
            </ul>
          </section>

          <section>
            <h3 className="font-semibold mb-1">Soporte</h3>
            <p className="text-on-surface-variant">
              Para reportar problemas o solicitar asistencia, contacta al equipo de TI.
            </p>
          </section>

          <div className="pt-2 text-xs text-on-surface-variant border-t border-outline-variant">
            <p>Versión 1.0.0 — RedNorte © 2026</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AyudaModal;
