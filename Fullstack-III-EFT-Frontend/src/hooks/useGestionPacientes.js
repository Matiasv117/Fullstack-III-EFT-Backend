import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  agregarPacienteAListaEspera,
  obtenerPacientes,
  registrarPaciente,
  eliminarPaciente,
} from '../api/gestionPacientesApi';

const PACIENTE_INICIAL = {
  nombre: '',
  apellido: '',
  dni: '',
  telefono: '',
  email: '',
};

export function useGestionPacientes() {
  const [pacientes, setPacientes] = useState([]);
  const [nuevoPaciente, setNuevoPaciente] = useState(PACIENTE_INICIAL);
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState('');
  const [error, setError] = useState('');

  const limpiarMensajes = useCallback(() => {
    setMensaje('');
    setError('');
  }, []);

  const cargarPacientes = useCallback(async () => {
    setCargando(true);
    limpiarMensajes();

    try {
      const data = await obtenerPacientes();
      setPacientes(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'No fue posible cargar los pacientes');
    } finally {
      setCargando(false);
    }
  }, [limpiarMensajes]);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void cargarPacientes();
    }, 0);

    return () => window.clearTimeout(timeoutId);
  }, [cargarPacientes]);

  const actualizarCampo = useCallback((campo, valor) => {
    setNuevoPaciente((estadoPrevio) => ({
      ...estadoPrevio,
      [campo]: valor,
    }));
  }, []);

  const formValido = useMemo(() => {
    return [nuevoPaciente.nombre, nuevoPaciente.apellido, nuevoPaciente.dni].every(
      (campo) => campo.trim().length > 0,
    );
  }, [nuevoPaciente.apellido, nuevoPaciente.dni, nuevoPaciente.nombre]);

  const registrar = useCallback(async () => {
    if (!formValido) {
      setError('Completa nombre, apellido y DNI antes de registrar.');
      return;
    }

    setCargando(true);
    limpiarMensajes();

    try {
      const pacienteGuardado = await registrarPaciente({
        nombre: nuevoPaciente.nombre.trim(),
        apellido: nuevoPaciente.apellido.trim(),
        dni: nuevoPaciente.dni.trim(),
        telefono: nuevoPaciente.telefono.trim() || null,
        email: nuevoPaciente.email.trim() || null,
      });

      setPacientes((pacientesActuales) => [...pacientesActuales, pacienteGuardado]);
      setNuevoPaciente(PACIENTE_INICIAL);
      setMensaje('Paciente registrado correctamente.');
    } catch (err) {
      setError(err.message || 'No fue posible registrar el paciente');
    } finally {
      setCargando(false);
    }
  }, [formValido, limpiarMensajes, nuevoPaciente.apellido, nuevoPaciente.dni, nuevoPaciente.email, nuevoPaciente.nombre, nuevoPaciente.telefono]);

  const agregarALista = useCallback(async (pacienteId) => {
    setCargando(true);
    limpiarMensajes();

    try {
      await agregarPacienteAListaEspera(pacienteId);
      setMensaje(`Paciente ${pacienteId} agregado a lista de espera.`);
    } catch (err) {
      setError(err.message || 'No fue posible agregar el paciente a la lista de espera');
    } finally {
      setCargando(false);
    }
  }, [limpiarMensajes]);

  const borrarPaciente = useCallback(async (pacienteId) => {
    setCargando(true);
    limpiarMensajes();

    try {
      await eliminarPaciente(pacienteId);
      setPacientes((pacientesActuales) =>
        pacientesActuales.filter((p) => p.id !== pacienteId),
      );
      setMensaje(`Paciente ${pacienteId} eliminado correctamente.`);
    } catch (err) {
      setError(err.message || 'No fue posible eliminar el paciente');
    } finally {
      setCargando(false);
    }
  }, [limpiarMensajes]);

  return {
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
    recargarPacientes: cargarPacientes,
  };
}


