import GestionPacientesView from './GestionPacientesView';
import { useGestionPacientes } from '../hooks/useGestionPacientes';

function GestionPacientes({ onSectionChange }) {
  const gestionPacientes = useGestionPacientes();

  return <GestionPacientesView {...gestionPacientes} onSectionChange={onSectionChange} />;
}

export default GestionPacientes;
