import { useState } from 'react';
import { useListaEspera } from '../hooks/useListaEspera';

function ListaEsperaView({
  listaEspera,
  cargando,
  mensaje,
  error,
  eliminarDeListaEspera,
  actualizarEstado,
  recargarListaEspera,
}) {
  const [filtroGravedad, setFiltroGravedad] = useState('TODOS');
  const [filtroEstado, setFiltroEstado] = useState('TODOS');

  const listaFiltrada = listaEspera.filter((item) => {
    if (filtroGravedad !== 'TODOS' && (item.gravedad || 'NORMAL') !== filtroGravedad) {
      return false;
    }
    if (filtroEstado !== 'TODOS' && (item.estado || 'PENDIENTE') !== filtroEstado) {
      return false;
    }
    return true;
  });

  const gravedadColor = (gravedad) => {
    const mapa = {
      ALTA: '#e74c3c',
      MEDIA: '#f39c12',
      BAJA: '#27ae60',
      NORMAL: '#3498db',
    };
    return mapa[gravedad] || '#95a5a6';
  };

  const estadoColor = (estado) => {
    const mapa = {
      PENDIENTE: '#e74c3c',
      ATENDIDO: '#27ae60',
      CANCELADO: '#95a5a6',
    };
    return mapa[estado] || '#3498db';
  };

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <h2 style={styles.title}>Lista de Espera</h2>
        <p style={styles.subtitle}>
          Monitorea y gestiona los pacientes que esperan atención médica.
        </p>
      </header>

      <section style={styles.panel} aria-live="polite">
        <div style={styles.toolbar}>
          <h3 style={styles.sectionTitle}>Pacientes en lista de espera</h3>
          <button
            type="button"
            style={{ ...styles.button, ...styles.buttonSecondary }}
            onClick={recargarListaEspera}
            disabled={cargando}
          >
            {cargando ? 'Actualizando…' : 'Actualizar'}
          </button>
        </div>

        <div style={styles.feedback}>
          {mensaje ? <div style={{ ...styles.alert, ...styles.alertSuccess }}>{mensaje}</div> : null}
          {error ? <div style={{ ...styles.alert, ...styles.alertError }}>{error}</div> : null}
        </div>

        {/* Filtros */}
        <div style={styles.filterSection}>
          <div style={styles.filterGroup}>
            <label style={styles.label}>Gravedad</label>
            <select
              style={styles.input}
              value={filtroGravedad}
              onChange={(e) => setFiltroGravedad(e.target.value)}
            >
              <option value="TODOS">Todos</option>
              <option value="ALTA">Alta</option>
              <option value="MEDIA">Media</option>
              <option value="BAJA">Baja</option>
              <option value="NORMAL">Normal</option>
            </select>
          </div>
          <div style={styles.filterGroup}>
            <label style={styles.label}>Estado</label>
            <select
              style={styles.input}
              value={filtroEstado}
              onChange={(e) => setFiltroEstado(e.target.value)}
            >
              <option value="TODOS">Todos</option>
              <option value="PENDIENTE">Pendiente</option>
              <option value="ATENDIDO">Atendido</option>
              <option value="CANCELADO">Cancelado</option>
            </select>
          </div>
          <div style={{ ...styles.filterGroup, display: 'flex', alignItems: 'flex-end' }}>
            <span style={styles.statsText}>
              Mostrando: <strong>{listaFiltrada.length}</strong> de{' '}
              <strong>{listaEspera.length}</strong>
            </span>
          </div>
        </div>

        {listaFiltrada.length === 0 ? (
          <p style={styles.emptyState}>No hay pacientes en la lista de espera con esos filtros.</p>
        ) : (
          <ul style={styles.list}>
            {listaFiltrada.map((item) => (
              <li key={item.id} style={styles.listItem}>
                <div style={styles.listDetails}>
                  <div style={styles.listHeader}>
                    <strong>Paciente ID: {item.pacienteId ?? 'N/A'}</strong>
                    <span style={{ ...styles.badge, background: gravedadColor(item.gravedad) }}>
                      {item.gravedad || 'NORMAL'}
                    </span>
                    <span style={{ ...styles.badge, background: estadoColor(item.estado) }}>
                      {item.estado || 'PENDIENTE'}
                    </span>
                  </div>
                  <span style={styles.meta}>
                    Interconsulta: {item.interconsulta || 'Sin especificar'}
                  </span>
                  <span style={styles.meta}>ID Registro: {item.id}</span>
                </div>

                <div style={styles.actions}>
                  <select
                    style={{
                      ...styles.button,
                      ...styles.buttonSecondary,
                      padding: '0.6rem 0.8rem',
                      cursor: 'pointer',
                    }}
                    onChange={(e) => {
                      if (e.target.value) {
                        actualizarEstado(item.id, e.target.value);
                        e.target.value = '';
                      }
                    }}
                    disabled={cargando}
                  >
                    <option value="">Cambiar estado</option>
                    <option value="PENDIENTE">Pendiente</option>
                    <option value="ATENDIDO">Atendido</option>
                    <option value="CANCELADO">Cancelado</option>
                  </select>
                  <button
                    type="button"
                    style={{
                      ...styles.button,
                      background: '#e74c3c',
                      color: '#fff',
                    }}
                    onClick={() => {
                      if (window.confirm('¿Estás seguro de que deseas eliminar este registro?')) {
                        eliminarDeListaEspera(item.id);
                      }
                    }}
                    disabled={cargando}
                  >
                    Eliminar
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}

function ListaEspera() {
  const {
    listaEspera,
    cargando,
    mensaje,
    error,
    cargarListaEspera,
    eliminarDeListaEspera,
    actualizarEstado,
  } = useListaEspera();

  return (
    <ListaEsperaView
      listaEspera={listaEspera}
      cargando={cargando}
      mensaje={mensaje}
      error={error}
      eliminarDeListaEspera={eliminarDeListaEspera}
      actualizarEstado={actualizarEstado}
      recargarListaEspera={cargarListaEspera}
    />
  );
}

const styles = {
  container: {
    display: 'grid',
    gap: '2rem',
  },
  header: {
    display: 'grid',
    gap: '0.75rem',
  },
  title: {
    fontSize: '2rem',
    fontWeight: 800,
    background: 'linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%)',
    WebkitBackgroundClip: 'text',
    WebkitTextFillColor: 'transparent',
    backgroundClip: 'text',
    letterSpacing: '-0.02em',
  },
  subtitle: {
    color: '#64748b',
    lineHeight: 1.6,
    fontSize: '1.05rem',
  },
  panel: {
    padding: '1.75rem',
    borderRadius: '20px',
    border: '2px solid rgba(14, 165, 233, 0.2)',
    background: 'linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 252, 0.98) 100%)',
    boxShadow: '0 10px 25px rgba(14, 165, 233, 0.12), 0 0 0 1px rgba(14, 165, 233, 0.05), inset 0 1px 0 rgba(255, 255, 255, 0.8)',
  },
  sectionTitle: {
    fontSize: '1.3rem',
    fontWeight: 700,
    color: '#0ea5e9',
    marginBottom: '1.25rem',
    letterSpacing: '-0.01em',
  },
  toolbar: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: '1rem',
    flexWrap: 'wrap',
    marginBottom: '1.5rem',
  },
  button: {
    border: 'none',
    borderRadius: '12px',
    padding: '0.875rem 1.25rem',
    fontWeight: 600,
    fontSize: '0.95rem',
    cursor: 'pointer',
    transition: 'all 0.25s cubic-bezier(0.4, 0, 0.2, 1)',
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.5rem',
  },
  buttonPrimary: {
    background: 'linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%)',
    color: '#fff',
    boxShadow: '0 4px 12px rgba(14, 165, 233, 0.3)',
  },
  buttonSecondary: {
    background: '#e0f2fe',
    color: '#0369a1',
    border: '2px solid #bae6fd',
  },
  buttonDanger: {
    background: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)',
    color: '#fff',
    boxShadow: '0 4px 12px rgba(239, 68, 68, 0.3)',
  },
  feedback: {
    display: 'grid',
    gap: '0.75rem',
    marginBottom: '1.25rem',
  },
  alert: {
    padding: '1rem 1.25rem',
    borderRadius: '14px',
    fontWeight: 600,
    fontSize: '0.95rem',
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
  },
  alertSuccess: {
    background: 'linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%)',
    color: '#065f46',
    border: '1px solid #6ee7b7',
  },
  alertError: {
    background: 'linear-gradient(135deg, #fee2e2 0%, #fecaca 100%)',
    color: '#991b1b',
    border: '1px solid #fca5a5',
  },
  filterSection: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '1.25rem',
    marginBottom: '1.5rem',
    padding: '1.5rem',
    background: 'linear-gradient(135deg, rgba(224, 242, 254, 0.5) 0%, rgba(186, 230, 253, 0.3) 100%)',
    borderRadius: '16px',
    border: '2px solid rgba(14, 165, 233, 0.2)',
    boxShadow: '0 4px 12px rgba(14, 165, 233, 0.1)',
  },
  filterGroup: {
    display: 'grid',
    gap: '0.625rem',
  },
  label: {
    display: 'block',
    fontWeight: 600,
    color: '#0ea5e9',
    fontSize: '0.9rem',
    letterSpacing: '0.02em',
    textTransform: 'uppercase',
  },
  input: {
    width: '100%',
    padding: '0.75rem 1rem',
    borderRadius: '12px',
    border: '2px solid #e2e8f0',
    background: '#ffffff',
    fontSize: '0.95rem',
    transition: 'all 0.25s ease',
    outline: 'none',
    cursor: 'pointer',
  },
  inputFocus: {
    borderColor: '#0ea5e9',
    boxShadow: '0 0 0 3px rgba(14, 165, 233, 0.1)',
  },
  statsText: {
    color: '#64748b',
    fontSize: '0.95rem',
    fontWeight: 500,
  },
  list: {
    listStyle: 'none',
    display: 'grid',
    gap: '1rem',
    padding: 0,
    margin: 0,
  },
  listItem: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: '1.25rem',
    padding: '1.25rem',
    borderRadius: '16px',
    border: '2px solid rgba(14, 165, 233, 0.15)',
    background: 'linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.95) 100%)',
    flexWrap: 'wrap',
    transition: 'all 0.25s ease',
    boxShadow: '0 4px 12px rgba(14, 165, 233, 0.08)',
  },
  listItemHover: {
    transform: 'translateY(-2px)',
    boxShadow: '0 8px 20px rgba(0, 0, 0, 0.08)',
    borderColor: '#0ea5e9',
  },
  listDetails: {
    display: 'grid',
    gap: '0.5rem',
    flex: 1,
    minWidth: '300px',
  },
  listHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
    flexWrap: 'wrap',
  },
  badge: {
    padding: '4px 12px',
    borderRadius: '999px',
    fontSize: '0.75rem',
    fontWeight: 700,
    color: '#fff',
    letterSpacing: '0.05em',
    textTransform: 'uppercase',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.15)',
  },
  meta: {
    color: '#64748b',
    fontSize: '0.9rem',
    fontWeight: 500,
  },
  actions: {
    display: 'flex',
    gap: '0.75rem',
    flexWrap: 'wrap',
  },
  emptyState: {
    color: '#94a3b8',
    padding: '2rem',
    textAlign: 'center',
    fontSize: '1rem',
    fontWeight: 500,
    background: '#f8fafc',
    borderRadius: '12px',
    border: '2px dashed #e2e8f0',
  },
};

export default ListaEspera;

