import { useState, useEffect } from 'react';
import { Plus, Trash2, Shield, Power, Check, X } from 'lucide-react';
import adminApi from '../api/adminApi';
import { validarUsuario, validarPassword, validarEmail } from '../utils/validations';

function AdminGestionUsuarios() {
  const [funcionarios, setFuncionarios] = useState([]);
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState('');
  const [error, setError] = useState('');
  const [showFormNuevo, setShowFormNuevo] = useState(false);
  const [formNuevo, setFormNuevo] = useState({ username: '', password: '', nombreCompleto: '', email: '' });
  const [erroresCampo, setErroresCampo] = useState({});

  useEffect(() => {
    cargarFuncionarios();
  }, []);

  const cargarFuncionarios = async () => {
    setCargando(true);
    setMensaje('');
    setError('');
    try {
      const data = await adminApi.listarFuncionarios();
      setFuncionarios(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Error al cargar funcionarios');
    } finally {
      setCargando(false);
    }
  };

  const handleCrear = async () => {
    const errores = {};
    const validUser = validarUsuario(formNuevo.username);
    if (!validUser.valido) errores.username = validUser.mensaje;
    const validPass = validarPassword(formNuevo.password);
    if (!validPass.valido) errores.password = validPass.mensaje;
    if (formNuevo.email) {
      const validMail = validarEmail(formNuevo.email);
      if (!validMail.valido) errores.email = validMail.mensaje;
    }

    if (Object.keys(errores).length > 0) {
      setErroresCampo(errores);
      return;
    }

    setCargando(true);
    try {
      await adminApi.crearFuncionario(formNuevo.username, formNuevo.password, formNuevo.nombreCompleto, formNuevo.email);
      setMensaje('Funcionario creado exitosamente');
      setFormNuevo({ username: '', password: '', nombreCompleto: '', email: '' });
      setShowFormNuevo(false);
      setErroresCampo({});
      await cargarFuncionarios();
    } catch (err) {
      setError(err.message || 'Error');
    } finally {
      setCargando(false);
    }
  };

  const handleEliminar = async (id) => {
    if (window.confirm('¿Eliminar este funcionario?')) {
      try {
        await adminApi.eliminarFuncionario(id);
        setMensaje('Eliminado');
        await cargarFuncionarios();
      } catch (err) {
        setError(err.message);
      }
    }
  };

  const handleCambiarEstado = async (id, activo) => {
    try {
      await adminApi.cambiarEstado(id, !activo);
      await cargarFuncionarios();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleCambiarRol = async (id, rol) => {
    if (window.confirm(`¿Cambiar a ${rol === 'ROLE_ADMIN' ? 'Funcionario' : 'Admin'}?`)) {
      try {
        await adminApi.cambiarRol(id, rol === 'ROLE_ADMIN' ? 'ROLE_FUNCIONARIO' : 'ROLE_ADMIN');
        await cargarFuncionarios();
      } catch (err) {
        setError(err.message);
      }
    }
  };

  return (
    <div className="space-y-6">
      <header className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold">Gestión de Funcionarios</h2>
          <p className="text-sm text-slate-600 dark:text-slate-400">Administra usuarios del sistema</p>
        </div>
        <button onClick={() => setShowFormNuevo(!showFormNuevo)} className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg flex items-center gap-2 cursor-pointer">
          <Plus className="w-4 h-4" /> Crear
        </button>
      </header>

      {mensaje && <div className="bg-emerald-50 border border-emerald-200 p-4 rounded-lg text-emerald-800 flex gap-2"><Check className="w-4 h-4" /> {mensaje}</div>}
      {error && <div className="bg-red-50 border border-red-200 p-4 rounded-lg text-red-800 flex gap-2"><X className="w-4 h-4" /> {error}</div>}

      {showFormNuevo && (
        <div className="bg-white dark:bg-slate-800 border rounded-lg p-6 space-y-4">
          <h3 className="font-bold text-lg">Crear Nuevo Funcionario</h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <input type="text" value={formNuevo.username} onChange={(e) => setFormNuevo({...formNuevo, username: e.target.value})} placeholder="Usuario *" className="w-full border rounded p-2 dark:bg-slate-700" />
              {erroresCampo.username && <p className="text-red-600 text-xs mt-1">⚠ {erroresCampo.username}</p>}
            </div>
            <div>
              <input type="password" value={formNuevo.password} onChange={(e) => setFormNuevo({...formNuevo, password: e.target.value})} placeholder="Contraseña *" className="w-full border rounded p-2 dark:bg-slate-700" />
              {erroresCampo.password && <p className="text-red-600 text-xs mt-1">⚠ {erroresCampo.password}</p>}
            </div>
            <input type="text" value={formNuevo.nombreCompleto} onChange={(e) => setFormNuevo({...formNuevo, nombreCompleto: e.target.value})} placeholder="Nombre" className="w-full border rounded p-2 dark:bg-slate-700" />
            <div>
              <input type="email" value={formNuevo.email} onChange={(e) => setFormNuevo({...formNuevo, email: e.target.value})} placeholder="Email" className="w-full border rounded p-2 dark:bg-slate-700" />
              {erroresCampo.email && <p className="text-red-600 text-xs mt-1">⚠ {erroresCampo.email}</p>}
            </div>
          </div>
          <div className="flex gap-2">
            <button onClick={handleCrear} disabled={cargando} className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded cursor-pointer">Guardar</button>
            <button onClick={() => { setShowFormNuevo(false); setFormNuevo({}); }} className="px-4 py-2 bg-slate-400 hover:bg-slate-500 text-white rounded cursor-pointer">Cancelar</button>
          </div>
        </div>
      )}

      <div className="bg-white dark:bg-slate-800 border rounded-lg overflow-hidden">
        <div className="p-4 border-b bg-slate-50 dark:bg-slate-900">
          <h3 className="font-bold">Funcionarios ({funcionarios.length})</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-slate-100 dark:bg-slate-900 text-xs font-semibold uppercase">
              <tr>
                <th className="px-4 py-3 text-left">Usuario</th>
                <th className="px-4 py-3 text-left">Nombre</th>
                <th className="px-4 py-3 text-left">Email</th>
                <th className="px-4 py-3 text-center">Rol</th>
                <th className="px-4 py-3 text-center">Estado</th>
                <th className="px-4 py-3 text-center">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {funcionarios.map(f => (
                <tr key={f.id} className="hover:bg-slate-50 dark:hover:bg-slate-900/50">
                  <td className="px-4 py-3 font-semibold">{f.username}</td>
                  <td className="px-4 py-3">{f.nombreCompleto || '—'}</td>
                  <td className="px-4 py-3">{f.email || '—'}</td>
                  <td className="px-4 py-3 text-center"><span className={`px-2 py-1 rounded text-xs font-bold ${f.role === 'ROLE_ADMIN' ? 'bg-red-100 text-red-700' : 'bg-blue-100 text-blue-700'}`}>{f.role === 'ROLE_ADMIN' ? 'Admin' : 'Func'}</span></td>
                  <td className="px-4 py-3 text-center"><span className={`px-2 py-1 rounded text-xs font-bold ${f.activo ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-700'}`}>{f.activo ? 'ON' : 'OFF'}</span></td>
                  <td className="px-4 py-3 text-center flex gap-1 justify-center">
                    <button onClick={() => handleCambiarRol(f.id, f.role)} className="p-1.5 bg-purple-100 text-purple-700 rounded hover:bg-purple-200 cursor-pointer" title="Rol"><Shield className="w-4 h-4" /></button>
                    <button onClick={() => handleCambiarEstado(f.id, f.activo)} className="p-1.5 bg-yellow-100 text-yellow-700 rounded hover:bg-yellow-200 cursor-pointer" title="Estado"><Power className="w-4 h-4" /></button>
                    <button onClick={() => handleEliminar(f.id)} className="p-1.5 bg-red-100 text-red-700 rounded hover:bg-red-200 cursor-pointer" title="Eliminar"><Trash2 className="w-4 h-4" /></button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default AdminGestionUsuarios;

