import { useEffect, useState } from 'react';
import { obtenerListaEsperaOptimizada, cancelarCitaConEstrategia } from '../api/optimizacionApi';

function Optimizacion() {
  const [listaEspera, setListaEspera] = useState([]);
  const [listaFiltrada, setListaFiltrada] = useState([]);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState('');
  const [filtroGravedad, setFiltroGravedad] = useState('TODOS');
  const [filtroEstado, setFiltroEstado] = useState('TODOS');
  const [simulandoCancelacion, setSimulandoCancelacion] = useState(false);
  const [citaAnclarId, setCitaAnclarId] = useState(null);
  const [estrategia, setEstrategia] = useState('fifo');

  useEffect(() => {
    const fetchLista = async () => {
      setCargando(true);
      setError('');

      try {
        const datos = await obtenerListaEsperaOptimizada();
        setListaEspera(Array.isArray(datos) ? datos : []);
      } catch (errorCapturado) {
        setError(errorCapturado.message || 'No fue posible obtener la lista de espera');
      } finally {
        setCargando(false);
      }
    };

    void fetchLista();
  }, []);

  useEffect(() => {
    let filtrada = [...listaEspera];

    if (filtroGravedad !== 'TODOS') {
      filtrada = filtrada.filter((item) => (item.gravedad || 'NORMAL') === filtroGravedad);
    }

    if (filtroEstado !== 'TODOS') {
      filtrada = filtrada.filter((item) => (item.estado || 'PENDIENTE') === filtroEstado);
    }

    setListaFiltrada(filtrada);
  }, [listaEspera, filtroGravedad, filtroEstado]);

  const manejarCancelacion = async () => {
    if (!citaAnclarId) {
      setError('Selecciona una cita para cancelar');
      return;
    }

    setSimulandoCancelacion(true);
    setError('');

    try {
      await cancelarCitaConEstrategia(citaAnclarId, estrategia);
      // Recargar la lista después de la cancelación
      const datos = await obtenerListaEsperaOptimizada();
      setListaEspera(Array.isArray(datos) ? datos : []);
      setCitaAnclarId(null);
      // Mostrar mensaje de éxito
      alert(`Cita ${citaAnclarId} cancelada y reasignada con estrategia ${estrategia}`);
    } catch (errorCapturado) {
      setError(
        errorCapturado.message ||
        'No fue posible procesar la cancelación de la cita'
      );
    } finally {
      setSimulandoCancelacion(false);
    }
  };

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
      <h2 style={styles.title}>Optimización de Lista de Espera</h2>
      <p style={styles.subtitle}>
        Monitorea y optimiza la asignación de citas médicas mediante estrategias inteligentes.
      </p>

      {error ? <div style={styles.error}>{error}</div> : null}

      {/* Sección de Control de Cancelaciones */}
      <div style={styles.panel}>
        <h3 style={styles.sectionTitle}>Simular Cancelación de Cita</h3>
        <div style={styles.form}>
          <div style={styles.formRow}>
            <div>
              <label style={styles.label}>ID de Cita a Cancelar</label>
              <input
                type="number"
                style={styles.input}
                value={citaAnclarId || ''}
                onChange={(e) => setCitaAnclarId(e.target.value ? parseInt(e.target.value) : null)}
                placeholder="Ingresa el ID de la cita"
                disabled={simulandoCancelacion}
              />
            </div>
            <div>
              <label style={styles.label}>Estrategia de Reasignación</label>
              <select
                style={styles.input}
                value={estrategia}
                onChange={(e) => setEstrategia(e.target.value)}
                disabled={simulandoCancelacion}
              >
                <option value="fifo">FIFO (Primera En Llegar)</option>
                <option value="lifo">LIFO (Última En Llegar)</option>
                <option value="gravedad">Por Gravedad</option>
              </select>
            </div>
            <button
              style={{
                ...styles.button,
                ...styles.buttonPrimary,
                marginTop: '1.5rem',
              }}
              onClick={manejarCancelacion}
              disabled={simulandoCancelacion || !citaAnclarId}
            >
              {simulandoCancelacion ? 'Procesando...' : 'Procesar Cancelación'}
            </button>
          </div>
        </div>
      </div>

      {/* Sección de Filtros */}
      <div style={styles.panel}>
        <h3 style={styles.sectionTitle}>Filtros</h3>
        <div style={styles.filterRow}>
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
          <div
            style={{
              ...styles.filterGroup,
              display: 'flex',
              alignItems: 'flex-end',
            }}
          >
            <p style={styles.statsText}>
              Total: <strong>{listaFiltrada.length}</strong> pacientes
            </p>
          </div>
        </div>
      </div>

      {/* Lista de Espera */}
      <div style={styles.panel}>
        <h3 style={styles.sectionTitle}>Lista de Espera Actual</h3>

        {cargando ? (
          <p style={styles.noData}>Cargando lista de espera…</p>
        ) : listaFiltrada.length === 0 ? (
          <p style={styles.noData}>No hay pacientes en la lista de espera</p>
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
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Información sobre estrategias */}
      <div style={styles.panel}>
        <h3 style={styles.sectionTitle}>📋 Estrategias de Optimización</h3>
        <ul style={styles.infoList}>
          <li>
            <strong>FIFO (Primera En Llegar):</strong> Reasigna la cita cancelada al paciente que
            lleva más tiempo esperando.
          </li>
          <li>
            <strong>LIFO (Última En Llegar):</strong> Reasigna la cita al paciente más reciente en
            la lista.
          </li>
          <li>
            <strong>Por Gravedad:</strong> Reasigna la cita al paciente con mayor gravedad de
            salud.
          </li>
        </ul>
      </div>
    </div>
  );
}

const styles = {
  container: {
    padding: '0',
  },
  title: {
    fontSize: '2rem',
    fontWeight: 800,
    background: 'linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%)',
    WebkitBackgroundClip: 'text',
    WebkitTextFillColor: 'transparent',
    backgroundClip: 'text',
    letterSpacing: '-0.02em',
    marginBottom: '0.75rem',
  },
  subtitle: {
    color: '#64748b',
    marginBottom: '1.5rem',
    fontSize: '1.05rem',
    lineHeight: 1.6,
  },
  panel: {
    padding: '1.75rem',
    borderRadius: '20px',
    border: '2px solid rgba(14, 165, 233, 0.2)',
    background: 'linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 252, 0.98) 100%)',
    marginBottom: '1.5rem',
    boxShadow: '0 10px 25px rgba(14, 165, 233, 0.12), 0 0 0 1px rgba(14, 165, 233, 0.05), inset 0 1px 0 rgba(255, 255, 255, 0.8)',
  },
  sectionTitle: {
    fontSize: '1.3rem',
    fontWeight: 700,
    color: '#0ea5e9',
    marginBottom: '1.25rem',
    letterSpacing: '-0.01em',
  },
  form: {
    display: 'grid',
    gap: '1.25rem',
  },
  formRow: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '1.25rem',
    alignItems: 'flex-start',
  },
  input: {
    width: '100%',
    padding: '0.875rem 1.125rem',
    borderRadius: '12px',
    border: '2px solid #e2e8f0',
    background: '#ffffff',
    fontSize: '0.95rem',
    fontFamily: 'inherit',
    transition: 'all 0.25s ease',
    outline: 'none',
  },
  inputFocus: {
    borderColor: '#0ea5e9',
    boxShadow: '0 0 0 3px rgba(14, 165, 233, 0.1)',
  },
  label: {
    display: 'block',
    marginBottom: '0.625rem',
    fontWeight: 600,
    color: '#0ea5e9',
    fontSize: '0.9rem',
    letterSpacing: '0.02em',
    textTransform: 'uppercase',
  },
  button: {
    border: 'none',
    borderRadius: '12px',
    padding: '0.875rem 1.5rem',
    fontWeight: 600,
    cursor: 'pointer',
    fontSize: '0.95rem',
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
  buttonPrimaryHover: {
    transform: 'translateY(-2px)',
    boxShadow: '0 6px 16px rgba(14, 165, 233, 0.4)',
  },
  buttonDisabled: {
    opacity: 0.5,
    cursor: 'not-allowed',
    transform: 'none !important',
  },
  filterRow: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '1.25rem',
    alignItems: 'flex-start',
  },
  filterGroup: {
    display: 'grid',
    gap: '0.625rem',
  },
  statsText: {
    color: '#64748b',
    fontSize: '0.95rem',
    margin: 0,
    fontWeight: 500,
  },
  list: {
    display: 'grid',
    gap: '1rem',
    listStyle: 'none',
    padding: 0,
    margin: 0,
  },
  listItem: {
    padding: '1.25rem',
    backgroundColor: 'linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.95) 100%)',
    border: '2px solid rgba(14, 165, 233, 0.15)',
    borderRadius: '16px',
    fontSize: '0.95rem',
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
  error: {
    padding: '1rem 1.25rem',
    borderRadius: '14px',
    backgroundColor: 'linear-gradient(135deg, #fee2e2 0%, #fecaca 100%)',
    color: '#991b1b',
    border: '1px solid #fca5a5',
    marginBottom: '1.25rem',
    fontWeight: 600,
    fontSize: '0.95rem',
  },
  noData: {
    color: '#94a3b8',
    fontSize: '1rem',
    fontWeight: 500,
    padding: '2rem',
    textAlign: 'center',
    background: '#f8fafc',
    borderRadius: '12px',
    border: '2px dashed #e2e8f0',
  },
  infoList: {
    listStyle: 'none',
    padding: '1.5rem',
    display: 'grid',
    gap: '1rem',
    background: 'linear-gradient(135deg, rgba(224, 242, 254, 0.5) 0%, rgba(186, 230, 253, 0.3) 100%)',
    borderRadius: '16px',
    border: '2px solid rgba(14, 165, 233, 0.2)',
    boxShadow: '0 4px 12px rgba(14, 165, 233, 0.1)',
  },
  infoListItem: {
    padding: '0.75rem',
    background: 'linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.95) 100%)',
    borderRadius: '12px',
    border: '2px solid rgba(14, 165, 233, 0.15)',
    fontSize: '0.95rem',
    lineHeight: 1.6,
    boxShadow: '0 2px 8px rgba(14, 165, 233, 0.05)',
  },
  infoListItemStrong: {
    color: '#0ea5e9',
    fontWeight: 700,
  },
};

export default Optimizacion;
