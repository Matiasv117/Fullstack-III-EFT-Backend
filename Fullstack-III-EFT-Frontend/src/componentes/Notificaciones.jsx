import { useEffect, useState } from 'react';
import {
  enviarNotificacion as enviarNotificacionApi,
  obtenerNotificacionesPendientes,
} from '../api/notificacionesApi';

function Notificaciones() {
  const [notificaciones, setNotificaciones] = useState([]);
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState('');
  const [error, setError] = useState('');

  const cargarNotificaciones = async () => {
    setCargando(true);
    setMensaje('');
    setError('');

    try {
      const datos = await obtenerNotificacionesPendientes();
      setNotificaciones(Array.isArray(datos) ? datos : []);
    } catch (errorCapturado) {
      setError(errorCapturado.message || 'No fue posible cargar las notificaciones');
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void cargarNotificaciones();
    }, 0);

    return () => window.clearTimeout(timeoutId);
  }, []);

  const manejarEnviarNotificacion = async (id) => {
    setCargando(true);
    setMensaje('');
    setError('');

    try {
      await enviarNotificacionApi(id);
      setNotificaciones((actuales) => actuales.filter((n) => n.id !== id));
      setMensaje(`Notificación ${id} enviada correctamente.`);
    } catch (errorCapturado) {
      setError(errorCapturado.message || 'No fue posible enviar la notificación');
    } finally {
      setCargando(false);
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Notificaciones Pendientes</h2>

      <div style={styles.feedback}>
        {mensaje ? <div style={{ ...styles.alert, ...styles.alertSuccess }}>{mensaje}</div> : null}
        {error ? <div style={{ ...styles.alert, ...styles.alertError }}>{error}</div> : null}
      </div>

      {cargando ? (
        <p style={styles.noData}>Cargando notificaciones…</p>
      ) : notificaciones.length === 0 ? (
        <p style={styles.noData}>No hay notificaciones pendientes</p>
      ) : (
        <ul style={styles.list}>
          {notificaciones.map(n => (
            <li key={n.id} style={styles.listItem}>
              <div style={styles.details}>
                <strong>ID: {n.id}</strong>
                <span style={styles.meta}>Paciente: {n.pacienteId ?? 'N/A'}</span>
                <span style={styles.meta}>Tipo: {n.tipo ?? 'N/A'}</span>
                <span style={styles.meta}>Estado: {n.estado ?? 'N/A'}</span>
                <span style={styles.meta}>Mensaje: {n.mensaje}</span>
              </div>
              <button
                style={styles.button}
                onClick={() => manejarEnviarNotificacion(n.id)}
                disabled={cargando}
              >
                Enviar
              </button>
            </li>
          ))}
        </ul>
      )}
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
    marginBottom: '1.5rem',
  },
  list: {
    display: 'grid',
    gap: '1rem',
    listStyle: 'none',
    padding: 0,
  },
  listItem: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: '1.25rem',
    padding: '1.25rem',
    background: 'linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.95) 100%)',
    border: '2px solid rgba(14, 165, 233, 0.15)',
    borderRadius: '16px',
    transition: 'all 0.25s ease',
    boxShadow: '0 4px 12px rgba(14, 165, 233, 0.08)',
  },
  listItemHover: {
    transform: 'translateY(-2px)',
    boxShadow: '0 8px 20px rgba(0, 0, 0, 0.08)',
    borderColor: '#0ea5e9',
  },
  details: {
    display: 'grid',
    gap: '0.375rem',
    flex: 1,
  },
  meta: {
    color: '#64748b',
    fontSize: '0.9rem',
    fontWeight: 500,
  },
  button: {
    padding: '0.75rem 1.5rem',
    background: 'linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%)',
    color: '#ffffff',
    border: 'none',
    borderRadius: '12px',
    cursor: 'pointer',
    fontSize: '0.95rem',
    fontWeight: 600,
    transition: 'all 0.25s cubic-bezier(0.4, 0, 0.2, 1)',
    boxShadow: '0 4px 12px rgba(14, 165, 233, 0.3)',
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.5rem',
  },
  buttonHover: {
    transform: 'translateY(-2px)',
    boxShadow: '0 6px 16px rgba(14, 165, 233, 0.4)',
  },
  feedback: {
    display: 'grid',
    gap: '0.75rem',
    marginBottom: '1.5rem',
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
};

export default Notificaciones;
