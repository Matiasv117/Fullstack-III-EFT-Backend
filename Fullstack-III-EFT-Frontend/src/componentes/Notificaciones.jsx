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
      const data = await obtenerNotificacionesPendientes();
      setNotificaciones(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'No fue posible cargar las notificaciones');
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
    } catch (err) {
      setError(err.message || 'No fue posible enviar la notificación');
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
    padding: '20px 0'
  },
  title: {
    fontSize: '28px',
    color: '#0d4f5c',
    marginBottom: '20px',
    borderBottom: '2px solid #4db6ac',
    paddingBottom: '10px'
  },
  list: {
    display: 'grid',
    gap: '0.85rem',
    listStyle: 'none',
    padding: 0
  },
  listItem: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '12px',
    marginBottom: '8px',
    backgroundColor: '#ffffff',
    border: '1px solid #e3eaec',
    borderRadius: '14px'
  },
  details: {
    display: 'grid',
    gap: '0.25rem'
  },
  meta: {
    color: '#567',
    fontSize: '0.95rem'
  },
  button: {
    padding: '8px 16px',
    backgroundColor: '#116a7b',
    color: 'white',
    border: 'none',
    borderRadius: '999px',
    cursor: 'pointer',
    fontSize: '14px'
  },
  feedback: {
    display: 'grid',
    gap: '0.75rem',
    marginBottom: '1rem'
  },
  alert: {
    padding: '0.9rem 1rem',
    borderRadius: '10px',
    fontWeight: 600
  },
  alertSuccess: {
    backgroundColor: '#e6f7f1',
    color: '#116149',
    border: '1px solid #9dd9c3'
  },
  alertError: {
    backgroundColor: '#fff1f1',
    color: '#a13131',
    border: '1px solid #f1bbbb'
  },
  noData: {
    color: '#5f7480',
    fontSize: '16px',
    fontStyle: 'italic',
    padding: '20px'
  }
};

export default Notificaciones;
