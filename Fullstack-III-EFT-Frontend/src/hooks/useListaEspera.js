import { useCallback, useEffect, useState } from 'react';
import {
  obtenerListaEspera,
  eliminarDelListaEspera,
  actualizarEstadoListaEspera,
} from '../api/gestionPacientesApi';

export function useListaEspera() {
  const [listaEspera, setListaEspera] = useState([]);
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState('');
  const [error, setError] = useState('');

  const limpiarMensajes = useCallback(() => {
    setMensaje('');
    setError('');
  }, []);

  const cargarListaEspera = useCallback(async () => {
    setCargando(true);
    limpiarMensajes();

    try {
      const data = await obtenerListaEspera();
      setListaEspera(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'No fue posible cargar la lista de espera');
    } finally {
      setCargando(false);
    }
  }, [limpiarMensajes]);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void cargarListaEspera();
    }, 0);

    return () => window.clearTimeout(timeoutId);
  }, [cargarListaEspera]);

  const eliminarDeListaEspera = useCallback(async (id) => {
    setCargando(true);
    limpiarMensajes();

    try {
      await eliminarDelListaEspera(id);
      setListaEspera((actual) => actual.filter((item) => item.id !== id));
      setMensaje(`Paciente ${id} eliminado de la lista de espera.`);
    } catch (err) {
      setError(err.message || 'No fue posible eliminar de la lista de espera');
    } finally {
      setCargando(false);
    }
  }, [limpiarMensajes]);

  const actualizarEstado = useCallback(async (id, nuevoEstado) => {
    setCargando(true);
    limpiarMensajes();

    try {
      const pacienteActualizado = await actualizarEstadoListaEspera(id, nuevoEstado);
      setListaEspera((actual) =>
        actual.map((item) => (item.id === id ? pacienteActualizado : item)),
      );
      setMensaje(`Estado actualizado a ${nuevoEstado}`);
    } catch (err) {
      setError(err.message || 'No fue posible actualizar el estado');
    } finally {
      setCargando(false);
    }
  }, [limpiarMensajes]);

  return {
    listaEspera,
    cargando,
    mensaje,
    error,
    cargarListaEspera,
    eliminarDeListaEspera,
    actualizarEstado,
  };
}

