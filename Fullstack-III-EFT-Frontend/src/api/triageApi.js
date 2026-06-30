import httpClient from './httpClient';

export const enviarAutotriage = async (datos) => {
  try {
    const { data } = await httpClient.post('/api/autotriage', datos);
    return data;
  } catch (err) {
    throw new Error(err.message || 'Error al procesar triage');
  }
};
