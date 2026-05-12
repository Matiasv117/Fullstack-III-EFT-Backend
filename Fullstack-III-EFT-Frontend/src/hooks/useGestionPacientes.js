import { useState, useEffect, useCallback } from 'react'
import { gestionPacientesApi } from '../api/gestionPacientesApi'

/**
 * Container: datos y acciones. La vista solo pinta (presenter).
 */
export function useGestionPacientes() {
  const [pacientes, setPacientes] = useState([])
  const [nuevoPaciente, setNuevoPaciente] = useState({
    nombre: '',
    apellido: '',
    dni: '',
    telefono: '',
    email: '',
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [actionMsg, setActionMsg] = useState(null)

  const cargar = useCallback(async () => {
    setError(null)
    setLoading(true)
    try {
      const { data } = await gestionPacientesApi.listarPacientes()
      setPacientes(data)
    } catch (e) {
      setError('No se pudo cargar la lista de pacientes.')
      console.error(e)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    cargar()
  }, [cargar])

  const registrarPaciente = async () => {
    setActionMsg(null)
    setError(null)
    try {
      const { data } = await gestionPacientesApi.registrarPaciente(nuevoPaciente)
      setPacientes((prev) => [...prev, data])
      setNuevoPaciente({ nombre: '', apellido: '', dni: '', telefono: '', email: '' })
      setActionMsg('Paciente registrado correctamente.')
    } catch (e) {
      setError('No se pudo registrar el paciente (DNI duplicado o error de red).')
      console.error(e)
    }
  }

  const agregarALista = async (pacienteId) => {
    setActionMsg(null)
    setError(null)
    try {
      await gestionPacientesApi.agregarAListaEspera({
        paciente: { id: pacienteId },
        gravedad: 'MEDIA',
        interconsulta: null,
      })
      setActionMsg('Paciente agregado a la lista de espera.')
    } catch (e) {
      setError('No se pudo agregar a la lista de espera.')
      console.error(e)
    }
  }

  return {
    pacientes,
    nuevoPaciente,
    setNuevoPaciente,
    loading,
    error,
    actionMsg,
    setActionMsg,
    registrarPaciente,
    agregarALista,
    recargar: cargar,
  }
}
