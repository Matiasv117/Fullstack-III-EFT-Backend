import httpClient from './httpClient';

function match(text, query) {
  if (!text) return false;
  return text.toLowerCase().includes(query.toLowerCase());
}

function matchAny(texts, query) {
  return texts.some(t => match(t, query));
}

export async function buscarGlobal(query, role) {
  if (!query || query.trim().length < 2) {
    return { pacientes: [], citas: [], listaEspera: [], funcionarios: [] };
  }

  const q = query.trim();
  const isFuncionarioOrAdmin = role === 'ROLE_ADMIN' || role === 'ROLE_FUNCIONARIO';

  const calls = [];

  if (isFuncionarioOrAdmin) {
    calls.push(
      httpClient.get('/api/pacientes').catch(() => ({ data: [] })),
      httpClient.get('/api/citas').catch(() => ({ data: [] })),
      httpClient.get('/api/lista-espera').catch(() => ({ data: [] })),
    );
  }

  if (role === 'ROLE_ADMIN') {
    calls.push(httpClient.get('/api/admin/funcionarios').catch(() => ({ data: [] })));
  }

  const [pacientesRes, citasRes, listaRes, funcionariosRes] = await Promise.all(calls);

  const pacientes = (pacientesRes?.data || [])
    .filter(p => matchAny([p.nombre, p.apellido, p.dni, p.email, p.telefono], q))
    .slice(0, 5);

  const citas = (citasRes?.data || [])
    .filter(c => matchAny([
      c.pacienteNombre,
      c.medico?.nombre,
      c.motivo,
      String(c.pacienteId),
    ], q))
    .slice(0, 5);

  const listaEspera = (listaRes?.data || [])
    .filter(l => matchAny([
      l.nombrePaciente,
      l.interconsulta,
      l.gravedad,
      l.estado,
    ], q))
    .slice(0, 5);

  const funcionarios = role === 'ROLE_ADMIN' ? (funcionariosRes?.data || [])
    .filter(f => matchAny([f.username, f.nombreCompleto, f.email], q))
    .slice(0, 5) : [];

  return { pacientes, citas, listaEspera, funcionarios };
}
