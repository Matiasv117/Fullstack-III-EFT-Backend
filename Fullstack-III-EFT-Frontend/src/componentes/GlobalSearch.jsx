import { useEffect, useRef } from 'react';

const GROUP_ICONS = {
  pacientes: 'group',
  citas: 'event',
  listaEspera: 'list_alt',
  funcionarios: 'people',
};

const GROUP_LABELS = {
  pacientes: 'Pacientes',
  citas: 'Citas',
  listaEspera: 'Lista de Espera',
  funcionarios: 'Funcionarios',
};

const GROUP_SECTIONS = {
  pacientes: 'pacientes',
  citas: 'citas',
  listaEspera: 'listaespera',
  funcionarios: 'usuarios',
};

function GlobalSearch({ results, query, onNavigate, onClose }) {
  const ref = useRef(null);

  useEffect(() => {
    function handleClick(e) {
      if (ref.current && !ref.current.contains(e.target)) {
        onClose();
      }
    }
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, [onClose]);

  useEffect(() => {
    function handleKey(e) {
      if (e.key === 'Escape') onClose();
    }
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [onClose]);

  const groups = Object.entries(results).filter(([, items]) => items.length > 0);
  const total = groups.reduce((s, [, items]) => s + items.length, 0);

  if (!query || query.trim().length < 2 || total === 0) return null;

  return (
    <div
      ref={ref}
      className="absolute top-full left-0 right-0 mt-2 bg-white dark:bg-surface-container-high border border-outline-variant rounded-xl shadow-2xl overflow-hidden z-50 max-h-[70vh] overflow-y-auto"
    >
      {groups.map(([key, items]) => (
        <div key={key}>
          <div className="flex items-center gap-2 px-4 py-2 bg-surface-container-low dark:bg-surface-dim text-[10px] font-bold uppercase tracking-wider text-on-surface-variant/70">
            <span className="material-symbols-outlined text-sm">{GROUP_ICONS[key]}</span>
            {GROUP_LABELS[key]}
            <span className="ml-auto text-[9px] text-on-surface-variant/50">{items.length}</span>
          </div>
          {items.map((item, idx) => (
            <button
              key={`${key}-${idx}`}
              onClick={() => {
                onNavigate(GROUP_SECTIONS[key]);
                onClose();
              }}
              className="w-full flex items-start gap-3 px-4 py-3 text-left hover:bg-primary/5 transition-colors cursor-pointer border-b border-outline-variant/30 last:border-b-0"
            >
              <div className="w-8 h-8 rounded-full bg-primary/10 text-primary flex items-center justify-center flex-shrink-0 mt-0.5">
                <span className="material-symbols-outlined text-sm">{GROUP_ICONS[key]}</span>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-on-surface truncate">
                  {item.nombre
                    ? `${item.nombre} ${item.apellido || ''}`
                    : item.nombrePaciente
                      ? item.nombrePaciente
                      : item.username
                        ? item.username
                        : `#${item.id}`}
                </p>
                <p className="text-[11px] text-on-surface-variant/80 truncate mt-0.5">
                  {key === 'pacientes' && `DNI: ${item.dni || '—'} · ${item.email || 'Sin email'}`}
                  {key === 'citas' && `Dr. ${item.medico?.nombre || '—'} · ${item.fechaHora ? new Date(item.fechaHora).toLocaleDateString('es-CL') : '—'}`}
                  {key === 'listaEspera' && `Gravedad: ${item.gravedad || 'NORMAL'} · ${item.interconsulta || 'Sin interconsulta'}`}
                  {key === 'funcionarios' && `${item.nombreCompleto || '—'} · ${item.email || ''}`}
                </p>
              </div>
              <span className="material-symbols-outlined text-on-surface-variant/40 text-sm flex-shrink-0">chevron_right</span>
            </button>
          ))}
        </div>
      ))}
    </div>
  );
}

export default GlobalSearch;
