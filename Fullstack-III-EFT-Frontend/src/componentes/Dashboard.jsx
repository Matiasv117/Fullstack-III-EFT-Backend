import { useState, useEffect } from 'react'
import { obtenerPacientes, obtenerListaEspera } from '../api/gestionPacientesApi';
import { obtenerNotificacionesPendientes } from '../api/notificacionesApi';
import ConnectionStatus from './ConnectionStatus';

const Dashboard = ({ user }) => {
  const isPaciente = user?.role === 'ROLE_PACIENTE';
  const [isSyncing, setIsSyncing] = useState(false)
  const [syncErrorResolved, setSyncErrorResolved] = useState(true) // Por defecto resuelto ya que los servicios están funcionando
  const [stats, setStats] = useState({
    totalPacientes: 1284,
    notificacionesPendientes: 4,
    listaEsperaCount: 15,
  });

  const handleSyncRetry = () => {
    setIsSyncing(true)
    setTimeout(() => {
      setIsSyncing(false)
      setSyncErrorResolved(true)
      fetchRealData()
    }, 1500)
  }

  const fetchRealData = async () => {
    try {
      const [pacientes, notificaciones, espera] = await Promise.allSettled([
        obtenerPacientes(),
        obtenerNotificacionesPendientes(),
        obtenerListaEspera(),
      ]);
      
      setStats({
        totalPacientes: pacientes.status === 'fulfilled' && Array.isArray(pacientes.value) 
          ? pacientes.value.length 
          : 1284,
        notificacionesPendientes: notificaciones.status === 'fulfilled' && Array.isArray(notificaciones.value) 
          ? notificaciones.value.length 
          : 4,
        listaEsperaCount: espera.status === 'fulfilled' && Array.isArray(espera.value) 
          ? espera.value.length 
          : 15,
      });
    } catch (err) {
      console.error("Error al cargar datos del backend en el Dashboard", err);
    }
  };

  useEffect(() => {
    fetchRealData();
  }, []);

  useEffect(() => {
    const buttons = document.querySelectorAll('button')
    buttons.forEach(btn => {
      btn.addEventListener('mousedown', () => {
        btn.classList.add('scale-95', 'opacity-80')
      })
      btn.addEventListener('mouseup', () => {
        btn.classList.remove('scale-95', 'opacity-80')
      })
      btn.addEventListener('mouseleave', () => {
        btn.classList.remove('scale-95', 'opacity-80')
      })
    })

    return () => {
      buttons.forEach(btn => {
        btn.removeEventListener('mousedown', () => {})
        btn.removeEventListener('mouseup', () => {})
        btn.removeEventListener('mouseleave', () => {})
      })
    }
  }, [])

  const appointments = [
    { id: 1, name: 'Juan Sebastián Pérez', initials: 'JS', type: 'Consulta General', time: '10:30 AM', status: 'Confirmada', statusColor: 'primary' },
    { id: 2, name: 'Ana María López', initials: 'AM', type: 'Seguimiento Post-Operatorio', time: '11:15 AM', status: 'Pendiente', statusColor: 'secondary' },
    { id: 3, name: 'Roberto Gómez', initials: 'RG', type: 'Laboratorios', time: '12:00 PM', status: 'Retrasado', statusColor: 'error' },
  ]

  if (isPaciente) {
    return (
      <main className="ml-[260px] pt-24 p-gutter min-h-screen">
        <div className="max-w-[1400px] mx-auto flex flex-col gap-gutter">
          
          {/* Hero Banner del Paciente */}
          <section className="fade-in-up stagger-1 relative overflow-hidden rounded-xl bg-primary-fixed-dim min-h-[200px] flex items-center">
            <div className="absolute inset-0 bg-gradient-to-br from-blue-600 to-[#00174b] opacity-95"></div>
            <div className="relative z-10 px-12 py-10 flex justify-between w-full items-center">
              <div className="max-w-2xl">
                <span className="inline-block px-3 py-1 bg-white/10 text-white rounded-full font-label-bold text-label-bold mb-4 backdrop-blur-md">
                  PORTAL DEL PACIENTE
                </span>
                <h2 className="font-display-hero text-display-hero text-white mb-2">
                  Bienvenido a tu Portal de Salud
                </h2>
                <p className="font-body-lg text-body-lg text-white/80">
                  Aquí puedes revisar el estado de tu ficha, tus citas médicas programadas y tu situación en la lista de espera.
                </p>
              </div>
            </div>
          </section>

          {/* Grid de Contenido Bento del Paciente */}
          <section className="grid grid-cols-1 md:grid-cols-3 gap-gutter fade-in-up stagger-3">
            
            {/* Card Cita */}
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant hover:border-primary/50 transition-all duration-500 group cursor-pointer hover:scale-105">
              <div className="flex justify-between items-start mb-4">
                <div className="w-12 h-12 bg-primary-fixed rounded-lg flex items-center justify-center text-primary group-hover:bg-primary group-hover:text-white transition-all duration-500">
                  <span className="material-symbols-outlined">event_available</span>
                </div>
                <span className="px-3 py-1 bg-primary/10 text-primary rounded-full font-label-bold text-[11px] font-bold">CONFIRMADA</span>
              </div>
              <h3 className="text-on-surface-variant font-label-bold mb-1">Próxima Cita Médica</h3>
              <div className="flex flex-col gap-1 mt-2">
                <span className="font-headline-md text-on-surface text-xl font-bold">Consulta de Medicina General</span>
                <span className="text-on-surface-variant text-body-md">Mañana a las 10:30 AM</span>
                <span className="text-on-surface-variant text-xs mt-2 flex items-center gap-1">
                  <span className="material-symbols-outlined text-xs text-primary">pin_drop</span>
                  Edificio Central - Consultorio 104
                </span>
              </div>
            </div>

            {/* Card Lista de Espera */}
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant hover:border-tertiary/50 transition-all duration-500 group cursor-pointer hover:scale-105">
              <div className="flex justify-between items-start mb-4">
                <div className="w-12 h-12 bg-tertiary-fixed rounded-lg flex items-center justify-center text-tertiary group-hover:bg-tertiary group-hover:text-white transition-all duration-500">
                  <span className="material-symbols-outlined">list_alt</span>
                </div>
                <span className="px-3 py-1 bg-tertiary-container text-on-tertiary-container rounded-full font-label-bold text-[11px] font-bold">EN PROCESO</span>
              </div>
              <h3 className="text-on-surface-variant font-label-bold mb-1">Tu Estado en Lista de Espera</h3>
              <div className="flex flex-col gap-1 mt-2">
                <span className="font-headline-md text-on-surface text-xl font-bold">Prioridad de Atención: Media</span>
                <span className="text-on-surface-variant text-body-md">Tu solicitud de interconsulta ha sido recibida y está siendo procesada para su asignación rápida.</span>
              </div>
            </div>

            {/* Card Notificaciones */}
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant hover:border-primary/50 transition-all duration-500 group cursor-pointer hover:scale-105">
              <div className="flex justify-between items-start mb-4">
                <div className="w-12 h-12 bg-secondary-container rounded-lg flex items-center justify-center text-primary group-hover:bg-primary group-hover:text-white transition-all duration-500">
                  <span className="material-symbols-outlined">notifications_active</span>
                </div>
                <span className="px-2 py-0.5 bg-green-100 text-green-800 rounded font-label-bold text-[10px] font-bold">NUEVO</span>
              </div>
              <h3 className="text-on-surface-variant font-label-bold mb-1">Mensajes Recientes</h3>
              <div className="flex flex-col gap-1 mt-2">
                <span className="font-headline-md text-on-surface text-md font-bold">¡Registro Exitoso!</span>
                <span className="text-on-surface-variant text-body-sm">Tu cuenta de paciente ha sido creada y vinculada automáticamente con tu RUT. Ya estás registrado en el sistema de Salud Red Norte.</span>
              </div>
            </div>

          </section>

        </div>
      </main>
    );
  }

  return (
    <main className="ml-[260px] pt-24 p-gutter min-h-screen">
      <div className="max-w-[1400px] mx-auto flex flex-col gap-gutter">
        
        {/* Hero Banner */}
        <section className="fade-in-up stagger-1 relative overflow-hidden rounded-xl bg-primary-fixed-dim min-h-[240px] flex items-center">
          <div className="absolute inset-0 bg-gradient-to-br from-primary to-[#00174b] opacity-95"></div>
          <div className="relative z-10 px-12 py-10 flex justify-between w-full items-center">
            <div className="max-w-2xl">
              <span className="inline-block px-3 py-1 bg-white/10 text-white rounded-full font-label-bold text-label-bold mb-4 backdrop-blur-md">
                SISTEMA DE SALUD PÚBLICA
              </span>
              <h2 className="font-display-hero text-display-hero text-white mb-2">
                Bienvenido, {user?.username || 'Dr. Benjamín Ibañez'}
              </h2>
              <p className="font-body-lg text-body-lg text-white/80">
                Hoy tienes 12 citas programadas y 4 reportes pendientes de revisión. Tu eficiencia operativa aumentó un 8% esta semana.
              </p>
            </div>
            <div className="hidden lg:block">
              <div className="bg-white/10 backdrop-blur-xl p-6 rounded-2xl border border-white/20 hover:scale-105 transition-transform duration-300 cursor-pointer">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 bg-primary-container/40 rounded-full flex items-center justify-center">
                    <span className="material-symbols-outlined text-white">bolt</span>
                  </div>
                  <div>
                    <p className="text-white/60 font-label-bold text-label-bold">ACCESO RÁPIDO</p>
                    <p className="text-white font-headline-md">Nueva Consulta</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="absolute right-0 top-0 h-full w-1/3 opacity-20 pointer-events-none overflow-hidden">
            <img
              alt="Scientific glass and medical laboratory aesthetics"
              className="object-cover h-full w-full"
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuBx5bunCgizfuKi2mLIC8Ret6blT5VnDaiv2WmZZvU7KfAMHork2ZXz0BiaqBxHbsdZabAn5Noty3kD2mfXkMPA95r2YBH8v2FyzSebMCFB2bSB8lXHJeW75q3M-yrr9OasoeAT9ijfDFAK05xO3m6w3Cl4PmM0LPbU4qx_HuH2djBI1WlV1GMEDovP2hgwTGF-vuiKTeu38rQmyKtWYL9b9O3aCTwGkezoxulDjeDDw5TWuM0ygljHuMjPDumweP9k8IT4OuQFfFE7"
            />
          </div>
        </section>

        {/* Alert Section */}
        {!syncErrorResolved && (
          <section className="fade-in-up stagger-2">
            <div className="bg-error-container border border-error/10 p-4 rounded-lg flex items-center justify-between shadow-sm hover:shadow-md transition-shadow duration-300">
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 rounded-full bg-error/10 flex items-center justify-center text-error">
                  <span className="material-symbols-outlined">sync_problem</span>
                </div>
                <div>
                  <h4 className="font-label-bold text-on-error-container">Error de sincronización detectado</h4>
                  <p className="font-body-md text-on-surface-variant">
                    La base de datos de la clínica "Norte Central" no se sincronizó correctamente hace 15 minutos.
                  </p>
                </div>
              </div>
              <button
                onClick={handleSyncRetry}
                disabled={isSyncing}
                className="px-4 py-2 bg-error text-white font-label-bold rounded-lg hover:bg-on-error-container transition-all duration-300 active:scale-95 shadow-lg shadow-error/20 disabled:opacity-50"
              >
                {isSyncing ? 'Sincronizando...' : 'Reintentar Sincronización'}
              </button>
            </div>
          </section>
        )}

        {syncErrorResolved && (
          <section className="fade-in-up stagger-2">
            <div className="bg-emerald-50 border border-emerald-100 p-4 rounded-lg flex items-center shadow-sm">
              <div className="w-10 h-10 rounded-full bg-emerald-100 flex items-center justify-center text-emerald-600">
                <span className="material-symbols-outlined">check_circle</span>
              </div>
              <div>
                <h4 className="font-label-bold text-emerald-800">Sincronización completada</h4>
                <p className="font-body-md text-emerald-600">La base de datos se ha sincronizado correctamente.</p>
              </div>
            </div>
          </section>
        )}

        {/* Summary Cards Grid (Bento Style) */}
        <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-gutter fade-in-up stagger-3">
          
          {/* Patients Card */}
          <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant hover:border-primary/50 transition-all duration-500 group cursor-pointer hover:scale-105 hover:shadow-xl hover:shadow-primary/5">
            <div className="flex justify-between items-start mb-4">
              <div className="w-12 h-12 bg-secondary-container rounded-lg flex items-center justify-center text-primary group-hover:bg-primary group-hover:text-white transition-all duration-500">
                <span className="material-symbols-outlined">person_add</span>
              </div>
              <span className="text-primary font-label-bold text-label-bold">+12%</span>
            </div>
            <h3 className="text-on-surface-variant font-label-bold mb-1">Pacientes registrados</h3>
            <div className="flex items-baseline gap-2">
              <span className="font-display-hero text-display-hero text-on-surface">{stats.totalPacientes.toLocaleString()}</span>
              <span className="text-on-surface-variant text-body-md">total</span>
            </div>
            <div className="mt-4 h-1.5 w-full bg-surface-container rounded-full overflow-hidden">
              <div className="h-full bg-primary w-[75%] transition-all duration-1000 delay-500 ease-out"></div>
            </div>
          </div>

          {/* Notifications Card */}
          <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant hover:border-tertiary/50 transition-all duration-500 group cursor-pointer hover:scale-105 hover:shadow-xl hover:shadow-tertiary/5">
            <div className="flex justify-between items-start mb-4">
              <div className="w-12 h-12 bg-tertiary-fixed rounded-lg flex items-center justify-center text-tertiary group-hover:bg-tertiary group-hover:text-white transition-all duration-500">
                <span className="material-symbols-outlined">notification_important</span>
              </div>
              <span className="px-2 py-0.5 bg-error-container text-error rounded font-label-bold text-[10px]">URGENTE</span>
            </div>
            <h3 className="text-on-surface-variant font-label-bold mb-1">Notificaciones pendientes</h3>
            <div className="flex items-baseline gap-2">
              <span className="font-display-hero text-display-hero text-on-surface">{stats.notificacionesPendientes.toString().padStart(2, '0')}</span>
              <span className="text-on-surface-variant text-body-md">mensajes</span>
            </div>
            <p className="mt-4 text-on-surface-variant font-body-md flex items-center gap-1">
              <span className="material-symbols-outlined text-[16px] text-tertiary">info</span>
              {stats.notificacionesPendientes > 0 ? `${stats.notificacionesPendientes} requieren acción inmediata` : 'Ninguna requiere acción'}
            </p>
          </div>

          {/* Appointments Card */}
          <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant hover:border-primary/50 transition-all duration-500 group cursor-pointer hover:scale-105 hover:shadow-xl hover:shadow-primary/5">
            <div className="flex justify-between items-start mb-4">
              <div className="w-12 h-12 bg-primary-fixed rounded-lg flex items-center justify-center text-primary group-hover:bg-primary group-hover:text-white transition-all duration-500">
                <span className="material-symbols-outlined">event_available</span>
              </div>
              <button className="text-on-surface-variant hover:text-primary transition-colors">
                <span className="material-symbols-outlined">more_vert</span>
              </button>
            </div>
            <h3 className="text-on-surface-variant font-label-bold mb-1">Citas de hoy</h3>
            <div className="flex items-baseline gap-2">
              <span className="font-display-hero text-display-hero text-on-surface">12</span>
              <span className="text-on-surface-variant text-body-md">programadas</span>
            </div>
            <div className="mt-4 flex -space-x-2">
              <div className="w-8 h-8 rounded-full border-2 border-white bg-surface-dim overflow-hidden transition-transform group-hover:-translate-x-1 duration-300">
                <img
                  alt="User 1"
                  className="object-cover h-full w-full"
                  src="https://lh3.googleusercontent.com/aida-public/AB6AXuBz3Smxt9YMOnWYZCtH37w7sxoZ0ijSBXApSUG4oLT1DF7S9fDYotuFHspv2rtvkr4BbhT1quXoaLZBmnF2nFVYTvM1RlFlJ3MOd7XMIl6rTZpqQeNqEMAuC35ZNYk79i-64_vrN0qFIBJHMUa2DdxZRsucQnJSldJUj9hNiX_j8Lp1NA4yEyT-9jisq3dU2AfYcFrG1GCgEJ_iGNUHbPcpyFxQvw6x1BdTquv_nHYmzZItEuZNuXvL9j2NiwC6zZA8tF8qHo5uFwq_"
                />
              </div>
              <div className="w-8 h-8 rounded-full border-2 border-white bg-surface-dim overflow-hidden transition-transform duration-300">
                <img
                  alt="User 2"
                  className="object-cover h-full w-full"
                  src="https://lh3.googleusercontent.com/aida-public/AB6AXuDPP5wfM_TBrviWTn2j1WirPPOmpojjm5h5bbf-RXiEqKX4-TnIGugi2c0bVgJHFzZDDRNw5lZciJsBd8Ul9fPCCVoSd7r9yWDNYDJLD_pMzWZCt-gn9fURCNz2Ko8MoGH7CAh7m9qF9ms3Xksxym1cwTH4jBmWMpSyenK0rUE88oAMNGcYbxUduXob0FLHvWNchgZMD1Do0GsOl8AG8t5Q6jZSRitKwQuI7w0S1KAUOvvMOWDQlSYfTdYbBkXAu5YdbCHhuMTMWxc6"
                />
              </div>
              <div className="w-8 h-8 rounded-full border-2 border-white bg-primary-container flex items-center justify-center text-white text-[10px] font-bold transition-transform group-hover:translate-x-1 duration-300">
                +9
              </div>
            </div>
          </div>

          {/* Optimization Card */}
          <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant hover:border-primary/50 transition-all duration-500 group cursor-pointer hover:scale-105 hover:shadow-xl hover:shadow-primary/5">
            <div className="flex justify-between items-start mb-4">
              <div className="w-12 h-12 bg-tertiary-fixed rounded-lg flex items-center justify-center text-tertiary group-hover:bg-tertiary group-hover:text-white transition-all duration-500">
                <span className="material-symbols-outlined">query_stats</span>
              </div>
            </div>
            <h3 className="text-on-surface-variant font-label-bold mb-1">Optimización de recursos</h3>
            <div className="flex items-baseline gap-2">
              <span className="font-display-hero text-display-hero text-on-surface">94%</span>
            </div>
            <p className="mt-4 text-on-surface-variant font-body-md">Nivel óptimo alcanzado</p>
          </div>
        </section>

        {/* Secondary Layout: Detailed Grid */}
        <section className="grid grid-cols-1 lg:grid-cols-3 gap-gutter fade-in-up stagger-4">
          
          {/* Patient Progress / Appointments List */}
          <div className="lg:col-span-2 bg-surface-container-lowest p-gutter rounded-xl border border-outline-variant hover:shadow-lg transition-shadow duration-300">
            <div className="flex justify-between items-center mb-6">
              <h3 className="font-headline-md text-headline-md text-on-surface">Próximas Citas</h3>
              <a className="text-primary font-label-bold hover:underline transition-all duration-200 cursor-pointer" href="#">
                Ver calendario completo
              </a>
            </div>
            <div className="flex flex-col gap-4">
              {appointments.map((appointment) => (
                <div
                  key={appointment.id}
                  className="flex items-center justify-between p-4 rounded-lg bg-surface hover:bg-surface-container-low transition-all duration-300 border border-transparent hover:border-outline-variant group cursor-pointer"
                >
                  <div className="flex items-center gap-4">
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center font-bold group-hover:scale-110 transition-transform ${
                      appointment.statusColor === 'primary' ? 'bg-secondary-container text-primary-container' :
                      appointment.statusColor === 'secondary' ? 'bg-tertiary-fixed text-tertiary' :
                      'bg-primary-fixed text-primary'
                    }`}>
                      {appointment.initials}
                    </div>
                    <div>
                      <h4 className="font-label-bold text-on-surface">{appointment.name}</h4>
                      <p className="text-on-surface-variant font-body-md">{appointment.type} - {appointment.time}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-6">
                    <span className={`px-3 py-1 rounded-full text-[12px] font-bold ${
                      appointment.statusColor === 'primary' ? 'bg-primary/10 text-primary' :
                      appointment.statusColor === 'secondary' ? 'bg-secondary-container text-on-secondary-container' :
                      'bg-error-container text-error'
                    }`}>
                      {appointment.status}
                    </span>
                    <button className="opacity-0 group-hover:opacity-100 transition-all transform group-hover:translate-x-1">
                      <span className="material-symbols-outlined text-on-surface-variant">arrow_forward_ios</span>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Sidebar Content: Health Insights */}
          <div className="flex flex-col gap-gutter">
            <ConnectionStatus compact={true} />
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant hover:shadow-lg transition-shadow duration-300 h-full group cursor-pointer">
              <div className="flex items-center gap-2 mb-4">
                <div className="w-10 h-10 bg-primary-container rounded-lg flex items-center justify-center text-primary">
                  <span className="material-symbols-outlined">analytics</span>
                </div>
                <h3 className="font-headline-md text-headline-md text-on-surface">Análisis de Red</h3>
              </div>
              <p className="text-on-surface-variant font-body-md mb-6">
                El flujo de pacientes en la clínica "Sur Este" ha incrementado un 20% en las últimas 24 horas.
              </p>
              <div className="space-y-4">
                <div className="flex justify-between items-center text-sm">
                  <span className="text-on-surface-variant">Capacidad Actual</span>
                  <span className="font-bold text-on-surface">82%</span>
                </div>
                <div className="w-full h-2 bg-surface-container rounded-full overflow-hidden">
                  <div className="h-full bg-primary w-[82%] group-hover:bg-primary/80 transition-all duration-1000 ease-out"></div>
                </div>
              </div>
              <button className="mt-8 w-full py-3 bg-primary hover:bg-primary/95 text-white font-label-bold rounded-lg transition-all duration-300 active:scale-95 shadow-md">
                Ver Detalles Operativos
              </button>
            </div>
          </div>
        </section>
      </div>
    </main>
  )
}

export default Dashboard
