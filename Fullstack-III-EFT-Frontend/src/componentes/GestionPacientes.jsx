import GestionPacientesView from './GestionPacientesView';
import { useGestionPacientes } from '../hooks/useGestionPacientes';

function GestionPacientes({ searchTerm = '' }) {
  const gestionPacientes = useGestionPacientes();

  return <GestionPacientesView {...gestionPacientes} searchTerm={searchTerm} />;
}

export default GestionPacientes;
