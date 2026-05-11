import axios from 'axios';
import { useState, useEffect } from 'react';

function Optimizacion() {
  const [listaEspera, setListaEspera] = useState([]);

  useEffect(() => {
    const fetchLista = async () => {
      try {
        const response = await axios.get('/optimizacion/lista-espera');
        setListaEspera(response.data);
      } catch (error) {
        console.error('Error al obtener lista de espera:', error);
      }
    };
    fetchLista();
  }, []);

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Lista de Espera Optimizada</h2>

      {listaEspera.length === 0 ? (
        <p style={styles.noData}>No hay pacientes en la lista de espera</p>
      ) : (
        <ul style={styles.list}>
          {listaEspera.map(item => (
            <li key={item.id} style={styles.listItem}>
              <div>
                <strong>Paciente:</strong> {item.pacienteNombre || 'N/A'} <br />
                <strong>Prioridad:</strong> <span style={styles.priority}>{item.prioridad || 'NORMAL'}</span>
              </div>
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
    padding: '12px',
    marginBottom: '8px',
    backgroundColor: '#f9f9f9',
    border: '1px solid #ddd',
    borderRadius: '5px',
    fontSize: '14px'
  },
  priority: {
    padding: '2px 8px',
    backgroundColor: '#ffc107',
    borderRadius: '3px',
    fontWeight: 'bold'
  },
  noData: {
    color: '#999',
    fontSize: '16px',
    fontStyle: 'italic',
    padding: '20px'
  }
};

export default Optimizacion;
