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
  disabledButton: {
    opacity: 0.65,
    cursor: 'not-allowed',
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
  },
  listDetails: {
    display: 'grid',
    gap: '0.25rem',
  },
  meta: {
    color: '#5f7480',
    fontSize: '0.95rem',
  },
  form: {
    display: 'grid',
    gap: '0.9rem',
  },
  formRow: {
    display: 'grid',
    gap: '0.9rem',
    gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
  },
  input: {
    width: '100%',
    padding: '0.9rem 1rem',
    borderRadius: '10px',
    border: '1px solid #cfe1e5',
    background: '#fff',
  },
  emptyState: {
    color: '#5f7480',
    padding: '0.75rem 0',
  },
};

function GestionPacientesView({
  pacientes,
  nuevoPaciente,
  cargando,
  mensaje,
  error,
  formValido,
  actualizarCampo,
  registrar,
  agregarALista,
  borrarPaciente,
  recargarPacientes,
}) {
  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <h2 style={styles.title}>Gestión de Pacientes</h2>
        <p style={styles.subtitle}>
          Registro de pacientes y derivación a la lista de espera desde un único flujo UI.
        </p>
      </header>

      <section style={styles.panel} aria-live="polite">
        <div style={styles.toolbar}>
          <h3 style={styles.sectionTitle}>Pacientes registrados</h3>
          <button
            type="button"
            style={{ ...styles.button, ...styles.buttonSecondary }}
            onClick={recargarPacientes}
            disabled={cargando}
          >
            {cargando ? 'Actualizando…' : 'Actualizar lista'}
          </button>
        </div>

        <div style={styles.feedback}>
          {mensaje ? <div style={{ ...styles.alert, ...styles.alertSuccess }}>{mensaje}</div> : null}
          {error ? <div style={{ ...styles.alert, ...styles.alertError }}>{error}</div> : null}
        </div>

        {pacientes.length === 0 ? (
          <p style={styles.emptyState}>No hay pacientes registrados todavía.</p>
        ) : (
          <ul style={styles.list}>
             {pacientes.map((paciente) => (
               <li key={paciente.id} style={styles.listItem}>
                 <div style={styles.listDetails}>
                   <strong>
                     {paciente.nombre} {paciente.apellido}
                   </strong>
                   <span style={styles.meta}>DNI: {paciente.dni}</span>
                   <span style={styles.meta}>
                     Contacto: {paciente.telefono || 'Sin teléfono'} · {paciente.email || 'Sin email'}
                   </span>
                 </div>
                 <div style={{ display: 'flex', gap: '0.5rem' }}>
                   <button
                     type="button"
                     style={{ ...styles.button, ...styles.buttonPrimary }}
                     onClick={() => agregarALista(paciente.id)}
                     disabled={cargando}
                   >
                     Agregar a lista
                   </button>
                   <button
                     type="button"
                     style={{
                       ...styles.button,
                       background: '#e74c3c',
                       color: '#fff',
                       padding: '0.85rem 1rem',
                     }}
                     onClick={() => {
                       if (window.confirm('¿Estás seguro de que deseas eliminar este paciente?')) {
                         borrarPaciente(paciente.id);
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

      <section style={styles.panel}>
        <h3 style={styles.sectionTitle}>Registrar nuevo paciente</h3>
        <div style={styles.form}>
          <div style={styles.formRow}>
            <input
              style={styles.input}
              value={nuevoPaciente.nombre}
              onChange={(event) => actualizarCampo('nombre', event.target.value)}
              placeholder="Nombre *"
              autoComplete="given-name"
            />
            <input
              style={styles.input}
              value={nuevoPaciente.apellido}
              onChange={(event) => actualizarCampo('apellido', event.target.value)}
              placeholder="Apellido *"
              autoComplete="family-name"
            />
            <input
              style={styles.input}
              value={nuevoPaciente.dni}
              onChange={(event) => actualizarCampo('dni', event.target.value)}
              placeholder="DNI *"
              autoComplete="off"
            />
          </div>
          <div style={styles.formRow}>
            <input
              style={styles.input}
              value={nuevoPaciente.telefono}
              onChange={(event) => actualizarCampo('telefono', event.target.value)}
              placeholder="Teléfono (opcional)"
              autoComplete="tel"
            />
            <input
              style={styles.input}
              type="email"
              value={nuevoPaciente.email}
              onChange={(event) => actualizarCampo('email', event.target.value)}
              placeholder="Correo electrónico (opcional)"
              autoComplete="email"
            />
          </div>
          <button
            type="button"
            style={{
              ...styles.button,
              ...styles.buttonPrimary,
              ...(formValido && !cargando ? {} : styles.disabledButton),
            }}
            onClick={registrar}
            disabled={!formValido || cargando}
          >
            {cargando ? 'Procesando…' : 'Registrar paciente'}
          </button>
        </div>
      </section>
    </div>
  );
}

export default GestionPacientesView;

