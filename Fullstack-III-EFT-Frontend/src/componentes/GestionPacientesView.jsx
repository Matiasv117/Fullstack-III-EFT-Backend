/**
 * Presenter: solo UI según props (patrón Container / Presenter).
 */
export default function GestionPacientesView({
  pacientes,
  nuevoPaciente,
  setNuevoPaciente,
  loading,
  error,
  actionMsg,
  registrarPaciente,
  agregarALista,
}) {
  return (
    <div className="salud-panel">
      <h2 className="salud-panel__title">Gestión de pacientes</h2>

      {error && (
        <div className="salud-alert salud-alert--error" role="alert">
          {error}
        </div>
      )}
      {actionMsg && (
        <div className="salud-alert salud-alert--ok" role="status">
          {actionMsg}
        </div>
      )}

      <section className="salud-section">
        <h3 className="salud-section__heading">Lista de pacientes</h3>
        {loading ? (
          <p className="salud-muted">Cargando…</p>
        ) : pacientes.length === 0 ? (
          <p className="salud-muted">No hay pacientes registrados.</p>
        ) : (
          <ul className="salud-list">
            {pacientes.map((p) => (
              <li key={p.id} className="salud-list__item">
                <div>
                  <strong>
                    {p.nombre} {p.apellido}
                  </strong>
                  <span className="salud-muted"> · DNI {p.dni}</span>
                  {p.telefono ? <span className="salud-muted"> · Tel. {p.telefono}</span> : null}
                  {p.email ? <span className="salud-muted"> · {p.email}</span> : null}
                </div>
                <button type="button" className="salud-btn salud-btn--secondary" onClick={() => agregarALista(p.id)}>
                  Agregar a lista de espera
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="salud-section">
        <h3 className="salud-section__heading">Registrar paciente</h3>
        <div className="salud-form">
          <label className="salud-label">
            Nombre
            <input
              className="salud-input"
              value={nuevoPaciente.nombre}
              onChange={(e) => setNuevoPaciente({ ...nuevoPaciente, nombre: e.target.value })}
              placeholder="Nombre"
              autoComplete="given-name"
            />
          </label>
          <label className="salud-label">
            Apellido
            <input
              className="salud-input"
              value={nuevoPaciente.apellido}
              onChange={(e) => setNuevoPaciente({ ...nuevoPaciente, apellido: e.target.value })}
              placeholder="Apellido"
              autoComplete="family-name"
            />
          </label>
          <label className="salud-label">
            DNI o documento
            <input
              className="salud-input"
              value={nuevoPaciente.dni}
              onChange={(e) => setNuevoPaciente({ ...nuevoPaciente, dni: e.target.value })}
              placeholder="DNI"
              autoComplete="off"
            />
          </label>
          <label className="salud-label">
            Teléfono <span className="salud-optional">(opcional)</span>
            <input
              className="salud-input"
              value={nuevoPaciente.telefono}
              onChange={(e) => setNuevoPaciente({ ...nuevoPaciente, telefono: e.target.value })}
              placeholder="+56 9 …"
              type="tel"
              autoComplete="tel"
            />
          </label>
          <label className="salud-label">
            Correo <span className="salud-optional">(opcional)</span>
            <input
              className="salud-input"
              value={nuevoPaciente.email}
              onChange={(e) => setNuevoPaciente({ ...nuevoPaciente, email: e.target.value })}
              placeholder="correo@ejemplo.cl"
              type="email"
              autoComplete="email"
            />
          </label>
          <button type="button" className="salud-btn salud-btn--primary" onClick={registrarPaciente}>
            Registrar paciente
          </button>
        </div>
      </section>
    </div>
  )
}
