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
    color: 'rgba(255, 255, 255, 0.7)',
    lineHeight: 1.6,
    fontSize: '1.05rem',
  },
  panel: {
    padding: '1.75rem',
    borderRadius: '20px',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    background: 'rgba(255, 255, 255, 0.05)',
    backdropFilter: 'blur(20px)',
    boxShadow: '0 8px 32px rgba(0, 0, 0, 0.3)',
    transition: 'all 0.3s ease',
  },
  sectionTitle: {
    fontSize: '1.3rem',
    fontWeight: 700,
    color: '#38bdf8',
    marginBottom: '1.25rem',
    letterSpacing: '-0.01em',
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
    background: 'linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(5, 150, 105, 0.15) 100%)',
    color: '#34d399',
    border: '1px solid rgba(16, 185, 129, 0.3)',
    backdropFilter: 'blur(10px)',
  },
  alertError: {
    background: 'linear-gradient(135deg, rgba(239, 68, 68, 0.2) 0%, rgba(220, 38, 38, 0.15) 100%)',
    color: '#f87171',
    border: '1px solid rgba(239, 68, 68, 0.3)',
    backdropFilter: 'blur(10px)',
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
  buttonPrimaryHover: {
    transform: 'translateY(-2px)',
    boxShadow: '0 6px 16px rgba(14, 165, 233, 0.4)',
  },
  buttonSecondary: {
    background: '#e0f2fe',
    color: '#0369a1',
    border: '2px solid #bae6fd',
  },
  buttonSecondaryHover: {
    background: '#bae6fd',
  },
  disabledButton: {
    opacity: 0.5,
    cursor: 'not-allowed',
    transform: 'none !important',
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
    border: '1px solid rgba(255, 255, 255, 0.1)',
    background: 'rgba(255, 255, 255, 0.05)',
    backdropFilter: 'blur(10px)',
    transition: 'all 0.3s ease',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.2)',
  },
  listItemHover: {
    transform: 'translateY(-2px)',
    boxShadow: '0 8px 24px rgba(0, 0, 0, 0.3)',
    borderColor: 'rgba(56, 189, 248, 0.3)',
    background: 'rgba(255, 255, 255, 0.08)',
  },
  listDetails: {
    display: 'grid',
    gap: '0.375rem',
  },
  meta: {
    color: 'rgba(255, 255, 255, 0.6)',
    fontSize: '0.9rem',
    fontWeight: 500,
  },
  form: {
    display: 'grid',
    gap: '1.25rem',
  },
  formRow: {
    display: 'grid',
    gap: '1.25rem',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
  },
  input: {
    width: '100%',
    padding: '0.875rem 1.125rem',
    borderRadius: '12px',
    border: '1px solid rgba(255, 255, 255, 0.2)',
    background: 'rgba(255, 255, 255, 0.08)',
    fontSize: '0.95rem',
    transition: 'all 0.3s ease',
    outline: 'none',
    color: 'white',
    backdropFilter: 'blur(10px)',
  },
  inputFocus: {
    borderColor: '#38bdf8',
    background: 'rgba(255, 255, 255, 0.12)',
    boxShadow: '0 0 0 3px rgba(56, 189, 248, 0.2)',
  },
  emptyState: {
    color: 'rgba(255, 255, 255, 0.5)',
    padding: '2rem',
    textAlign: 'center',
    fontSize: '1rem',
    fontWeight: 500,
    background: 'rgba(255, 255, 255, 0.03)',
    borderRadius: '16px',
    border: '2px dashed rgba(255, 255, 255, 0.1)',
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

