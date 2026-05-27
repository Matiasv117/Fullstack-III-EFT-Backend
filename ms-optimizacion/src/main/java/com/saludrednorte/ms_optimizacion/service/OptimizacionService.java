/**
 * Servicio de optimizacion: coordina reasignacion de citas y calculo de prioridad.
 */
@Service
public class OptimizacionService {

    private static final Logger logger = LoggerFactory.getLogger(OptimizacionService.class);

    @Autowired
    private OptimizacionFactory factory;

    @Autowired
    private CitaService citaService;

    @Autowired
    private ListaEsperaClient listaEsperaClient;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private PrioridadCalculadora prioridadCalculadora;

    public void procesarCancelacion(Long citaId, String estrategiaTipo) {
        citaService.cancelarCita(citaId);
        Cita citaCancelada = citaService.obtenerCitaPorId(citaId).orElse(null);
        if (citaCancelada != null) {
            EstrategiaOptimizacion estrategia = factory.getEstrategia(estrategiaTipo);
            estrategia.reasignarCita(citaCancelada);

            // Notificar reasignación de cita
            try {
                NotificationRequestDTO notif = new NotificationRequestDTO();
                notif.setPacienteId(citaCancelada.getPacienteId());
                notif.setTipo("CITA_REASIGNADA");
                notif.setMensaje("Cita reasignada para " + citaCancelada.getFechaHora());
                notificationClient.createNotification(notif);
                logger.info("Notificación de reasignación enviada para cita {}", citaCancelada.getId());
            } catch (Exception e) {
                logger.warn("Fallo al notificar reasignación de cita {} : {}", citaCancelada.getId(), e.getMessage());
            }
        }
    }

    @CircuitBreaker(name = "listaEsperaService", fallbackMethod = "fallbackListaEspera")
    public List<ListaEsperaDTO> obtenerListaEspera() {
        // Llamada a ms-gestionpacientes usando Feign
        return listaEsperaClient.getListaEspera();
    }

    public List<ListaEsperaDTO> fallbackListaEspera(Throwable t) {
        // Retornar lista vacía o datos locales
        return List.of();
    }

    /**
     * Calcula la prioridad de un paciente segun gravedad, distancia y dias de espera.
     *
     * @param gravedad nivel 1-5
     * @param distanciaKm distancia geografica en kilometros
     * @param diasEspera dias acumulados en espera
     * @return nivel de prioridad calculado
     */
    public NivelPrioridad calcularPrioridadPaciente(int gravedad, double distanciaKm, int diasEspera) {
        return prioridadCalculadora.calcularNivel(gravedad, distanciaKm, diasEspera);
    }
}
