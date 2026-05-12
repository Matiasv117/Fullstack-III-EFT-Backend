import GestionPacientesView from './GestionPacientesView';
import { useGestionPacientes } from '../hooks/useGestionPacientes';

function GestionPacientes() {
  const gestionPacientes = useGestionPacientes();

  return <GestionPacientesView {...gestionPacientes} />;
}

export default GestionPacientes;
