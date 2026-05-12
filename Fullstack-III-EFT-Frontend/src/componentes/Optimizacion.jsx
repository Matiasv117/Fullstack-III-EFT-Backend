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
        const data = await obtenerListaEsperaOptimizada();
        setListaEspera(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(err.message || 'No fue posible obtener la lista de espera');
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
      const data = await obtenerListaEsperaOptimizada();
      setListaEspera(Array.isArray(data) ? data : []);
      setCitaAnclarId(null);
      // Mostrar mensaje de éxito
      alert(`Cita ${citaAnclarId} cancelada y reasignada con estrategia ${estrategia}`);
    } catch (err) {
      setError(
        err.message ||
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
    padding: '20px 0',
  },
  title: {
    fontSize: '28px',
    color: '#0d4f5c',
    marginBottom: '10px',
    borderBottom: '2px solid #4db6ac',
    paddingBottom: '10px',
  },
  subtitle: {
    color: '#5f7480',
    marginBottom: '20px',
    fontSize: '16px',
  },
  panel: {
    padding: '1.25rem',
    borderRadius: '16px',
    border: '1px solid #dce8ea',
    background: '#fbfefe',
    marginBottom: '1.5rem',
    boxShadow: '0 10px 25px rgba(16, 61, 70, 0.05)',
  },
  sectionTitle: {
    fontSize: '1.2rem',
    color: '#116a7b',
    marginBottom: '1rem',
  },
  form: {
    display: 'grid',
    gap: '1rem',
  },
  formRow: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '1rem',
    alignItems: 'flex-start',
  },
  input: {
    width: '100%',
    padding: '0.9rem 1rem',
    borderRadius: '10px',
    border: '1px solid #cfe1e5',
    background: '#fff',
    fontSize: '14px',
    fontFamily: 'inherit',
  },
  label: {
    display: 'block',
    marginBottom: '0.5rem',
    fontWeight: 600,
    color: '#116a7b',
    fontSize: '14px',
  },
  button: {
    border: 'none',
    borderRadius: '10px',
    padding: '0.85rem 1rem',
    fontWeight: 600,
    cursor: 'pointer',
    fontSize: '14px',
  },
  buttonPrimary: {
    background: 'linear-gradient(135deg, #116a7b, #4db6ac)',
    color: '#fff',
  },
  filterRow: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
    gap: '1rem',
    alignItems: 'flex-start',
  },
  filterGroup: {
    display: 'grid',
    gap: '0.5rem',
  },
  statsText: {
    color: '#5f7480',
    fontSize: '14px',
    margin: 0,
  },
  list: {
    display: 'grid',
    gap: '0.85rem',
    listStyle: 'none',
    padding: 0,
    margin: 0,
  },
  listItem: {
    padding: '1rem',
    backgroundColor: '#ffffff',
    border: '1px solid #e3eaec',
    borderRadius: '14px',
    fontSize: '14px',
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
    padding: '2px 10px',
    borderRadius: '999px',
    fontSize: '12px',
    fontWeight: 'bold',
    color: '#fff',
  },
  meta: {
    color: '#5f7480',
    fontSize: '13px',
  },
  error: {
    padding: '0.9rem 1rem',
    borderRadius: '10px',
    backgroundColor: '#fff1f1',
    color: '#a13131',
    border: '1px solid #f1bbbb',
    marginBottom: '1rem',
  },
  noData: {
    color: '#5f7480',
    fontSize: '16px',
    fontStyle: 'italic',
    padding: '20px',
  },
  infoList: {
    listStyle: 'none',
    padding: '1rem',
    display: 'grid',
    gap: '1rem',
  },
};

export default Optimizacion;
