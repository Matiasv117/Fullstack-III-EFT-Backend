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
    gap: '1.5rem',
  },
  header: {
    display: 'grid',
    gap: '0.5rem',
  },
  title: {
    fontSize: '1.9rem',
    color: '#0d4f5c',
  },
  subtitle: {
    color: '#4f6771',
    lineHeight: 1.5,
  },
  panel: {
    padding: '1.25rem',
    borderRadius: '16px',
    border: '1px solid #dce8ea',
    background: '#fbfefe',
    boxShadow: '0 10px 25px rgba(16, 61, 70, 0.05)',
  },
  sectionTitle: {
    fontSize: '1.2rem',
    color: '#116a7b',
    marginBottom: '1rem',
  },
  toolbar: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: '1rem',
    flexWrap: 'wrap',
    marginBottom: '1rem',
  },
  button: {
    border: 'none',
    borderRadius: '10px',
    padding: '0.85rem 1rem',
    fontWeight: 600,
    cursor: 'pointer',
  },
  buttonPrimary: {
    background: 'linear-gradient(135deg, #116a7b, #4db6ac)',
    color: '#fff',
  },
  buttonSecondary: {
    background: '#e5f2f1',
    color: '#0d4f5c',
  },
  feedback: {
    display: 'grid',
    gap: '0.75rem',
    marginBottom: '1rem',
  },
  alert: {
    padding: '0.9rem 1rem',
    borderRadius: '12px',
    fontWeight: 600,
  },
  alertSuccess: {
    background: '#e6f7f1',
    color: '#116149',
    border: '1px solid #9dd9c3',
  },
  alertError: {
    background: '#fff1f1',
    color: '#a13131',
    border: '1px solid #f1bbbb',
  },
  filterSection: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
    gap: '1rem',
    marginBottom: '1.5rem',
    padding: '1rem',
    background: '#f0f7f8',
    borderRadius: '12px',
  },
  filterGroup: {
    display: 'grid',
    gap: '0.5rem',
  },
  label: {
    display: 'block',
    fontWeight: 600,
    color: '#116a7b',
    fontSize: '14px',
  },
  input: {
    width: '100%',
    padding: '0.7rem 0.8rem',
    borderRadius: '8px',
    border: '1px solid #cfe1e5',
    background: '#fff',
    fontSize: '14px',
  },
  statsText: {
    color: '#5f7480',
    fontSize: '14px',
  },
  list: {
    listStyle: 'none',
    display: 'grid',
    gap: '0.85rem',
    padding: 0,
    margin: 0,
  },
  listItem: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: '1rem',
    padding: '1rem',
    borderRadius: '14px',
    border: '1px solid #e3eaec',
    background: '#ffffff',
    flexWrap: 'wrap',
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
    padding: '2px 10px',
    borderRadius: '999px',
    fontSize: '12px',
    fontWeight: 'bold',
    color: '#fff',
  },
  meta: {
    color: '#5f7480',
    fontSize: '0.95rem',
  },
  actions: {
    display: 'flex',
    gap: '0.5rem',
    flexWrap: 'wrap',
  },
  emptyState: {
    color: '#5f7480',
    padding: '0.75rem 0',
  },
};

export default ListaEspera;

