import { useGestionPacientes } from '../hooks/useGestionPacientes'
import GestionPacientesView from './GestionPacientesView'

export default function GestionPacientes() {
  const props = useGestionPacientes()
  return <GestionPacientesView {...props} />
}
