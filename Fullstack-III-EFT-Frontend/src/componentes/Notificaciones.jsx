import axios from 'axios';
import { useState, useEffect } from 'react';

function Notificaciones() {
  const [notificaciones, setNotificaciones] = useState([]);

  useEffect(() => {
    const fetchNotificaciones = async () => {
      try {
        const response = await axios.get('/api/notifications/pending');
        setNotificaciones(response.data);
      } catch (error) {
        console.error('Error al obtener notificaciones:', error);
      }
    };
    fetchNotificaciones();
  }, []);

  const enviarNotificacion = async (id) => {
    try {
      await axios.post(`/api/notifications/${id}/send`);
      alert('Notificación enviada');
      // Actualizar lista
      setNotificaciones(notificaciones.filter(n => n.id !== id));
    } catch (error) {
      console.error('Error al enviar notificación:', error);
      alert('Error al enviar notificación');
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Notificaciones Pendientes</h2>

      {notificaciones.length === 0 ? (
        <p style={styles.noData}>No hay notificaciones pendientes</p>
      ) : (
        <ul style={styles.list}>
          {notificaciones.map(n => (
            <li key={n.id} style={styles.listItem}>
              <span>
                <strong>ID:</strong> {n.id} - <strong>Mensaje:</strong> {n.mensaje}
              </span>
              <button
                style={styles.button}
                onClick={() => enviarNotificacion(n.id)}
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
    color: '#007bff',
    marginBottom: '20px',
    borderBottom: '2px solid #007bff',
    paddingBottom: '10px'
  },
  list: {
    listStyle: 'none',
    padding: 0
  },
  listItem: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '12px',
    marginBottom: '8px',
    backgroundColor: '#f9f9f9',
    border: '1px solid #ddd',
    borderRadius: '5px'
  },
  button: {
    padding: '8px 16px',
    backgroundColor: '#17a2b8',
    color: 'white',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
    fontSize: '14px'
  },
  noData: {
    color: '#999',
    fontSize: '16px',
    fontStyle: 'italic',
    padding: '20px'
  }
};

export default Notificaciones;
