import axios from 'axios';
import { useState, useEffect } from 'react';

function GestionPacientes() {
  const [pacientes, setPacientes] = useState([]);
  const [nuevoPaciente, setNuevoPaciente] = useState({ nombre: '', apellido: '', dni: '' });

  useEffect(() => {
    const fetchPacientes = async () => {
      try {
        const response = await axios.get('/pacientes');
        setPacientes(response.data);
      } catch (error) {
        console.error('Error al obtener pacientes:', error);
      }
    };
    fetchPacientes();
  }, []);

  const registrarPaciente = async () => {
    try {
      const response = await axios.post('/pacientes', nuevoPaciente);
      setPacientes([...pacientes, response.data]);
      setNuevoPaciente({ nombre: '', apellido: '', dni: '' });
      alert('Paciente registrado exitosamente');
    } catch (error) {
      console.error('Error al registrar paciente:', error);
      alert('Error al registrar paciente');
    }
  };

  const agregarALista = async (pacienteId) => {
    try {
      // El API espera la forma JPA: paciente anidado con id (no pacienteId suelto).
      await axios.post('/lista-espera', {
        paciente: { id: pacienteId },
        gravedad: 'MEDIA',
        interconsulta: null,
      });
      alert('Agregado a lista de espera');
    } catch (error) {
      console.error('Error al agregar a lista:', error);
      alert('Error al agregar a lista');
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Gestión de Pacientes</h2>

      <div style={styles.section}>
        <h3 style={styles.subtitle}>Lista de Pacientes</h3>
        {pacientes.length === 0 ? (
          <p style={styles.noData}>No hay pacientes registrados</p>
        ) : (
          <ul style={styles.list}>
            {pacientes.map(p => (
              <li key={p.id} style={styles.listItem}>
                <span>{p.nombre} {p.apellido} - DNI: {p.dni}</span>
                <button
                  style={styles.button}
                  onClick={() => agregarALista(p.id)}
                >
                  Agregar a Lista
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div style={styles.section}>
        <h3 style={styles.subtitle}>Registrar Nuevo Paciente</h3>
        <div style={styles.form}>
          <input
            style={styles.input}
            value={nuevoPaciente.nombre}
            onChange={e => setNuevoPaciente({...nuevoPaciente, nombre: e.target.value})}
            placeholder="Nombre"
          />
          <input
            style={styles.input}
            value={nuevoPaciente.apellido}
            onChange={e => setNuevoPaciente({...nuevoPaciente, apellido: e.target.value})}
            placeholder="Apellido"
          />
          <input
            style={styles.input}
            value={nuevoPaciente.dni}
            onChange={e => setNuevoPaciente({...nuevoPaciente, dni: e.target.value})}
            placeholder="DNI"
          />
          <button
            style={{...styles.button, ...styles.buttonPrimary}}
            onClick={registrarPaciente}
          >
            Registrar
          </button>
        </div>
      </div>
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
  subtitle: {
    fontSize: '20px',
    color: '#333',
    marginBottom: '15px'
  },
  section: {
    marginBottom: '30px'
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
    backgroundColor: '#28a745',
    color: 'white',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
    fontSize: '14px'
  },
  buttonPrimary: {
    backgroundColor: '#007bff',
    padding: '12px 24px',
    fontSize: '16px',
    width: '100%'
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px'
  },
  input: {
    padding: '12px',
    border: '1px solid #ddd',
    borderRadius: '5px',
    fontSize: '14px',
    fontFamily: 'inherit'
  },
  noData: {
    color: '#999',
    fontSize: '16px',
    fontStyle: 'italic'
  }
};

export default GestionPacientes;
