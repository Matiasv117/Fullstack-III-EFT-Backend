import { useState } from 'react';
import { LogIn, User, Lock, AlertCircle, UserCircle, IdCard, Mail } from 'lucide-react';
import authApi from '../api/authApi';
import { validarRUT, validarNombre, validarApellido, validarPassword, validarUsuario, validarEmail } from '../utils/validations';

function Login({ onLoginSuccess }) {
  const [loginType, setLoginType] = useState('paciente'); // 'paciente' o 'funcionario'
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [nombre, setNombre] = useState('');
  const [apellido, setApellido] = useState('');
  const [rut, setRut] = useState('');
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [erroresCampo, setErroresCampo] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setErroresCampo({});
    setIsLoading(true);

    try {
      let errores = {};

      if (loginType === 'paciente') {
        // Validaciones para login de paciente
        const validNombre = validarNombre(nombre);
        if (!validNombre.valido) errores.nombre = validNombre.mensaje;

        const validApellido = validarApellido(apellido);
        if (!validApellido.valido) errores.apellido = validApellido.mensaje;

        const validRUT = validarRUT(rut);
        if (!validRUT.valido) errores.rut = validRUT.mensaje;

        const validEmail = validarEmail(email);
        if (!validEmail.valido) errores.email = validEmail.mensaje;

        if (Object.keys(errores).length > 0) {
          setErroresCampo(errores);
          setIsLoading(false);
          return;
        }

        const response = await authApi.loginPaciente(nombre, apellido, rut, email);
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify({
          username: response.username,
          role: response.role
        }));
        onLoginSuccess(response);
      } else {
        // Validaciones para login de funcionario
        const validUsuario = validarUsuario(username);
        if (!validUsuario.valido) errores.username = validUsuario.mensaje;

        const validPassword = validarPassword(password);
        if (!validPassword.valido) errores.password = validPassword.mensaje;

        if (Object.keys(errores).length > 0) {
          setErroresCampo(errores);
          setIsLoading(false);
          return;
        }

        const response = await authApi.login(username, password);
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify({
          username: response.username,
          role: response.role
        }));
        onLoginSuccess(response);
      }
    } catch (err) {
      setError(err.message || 'Error al iniciar sesión. Verifica tus datos.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 p-4">
      <div className="max-w-md w-full">
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl p-8">
          {/* Header */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-600 rounded-full mb-4">
              <LogIn className="w-8 h-8 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
              RedNorte
            </h1>
            <p className="text-gray-600 dark:text-gray-400">
              Inicia sesión para continuar
            </p>
          </div>

          {/* Login Type Selector */}
          <div className="mb-6 flex gap-2">
            <button
              type="button"
              onClick={() => setLoginType('paciente')}
              className={`flex-1 py-3 px-4 rounded-lg font-medium transition-colors ${
                loginType === 'paciente'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600'
              }`}
            >
              <UserCircle className="w-5 h-5 inline mr-2" />
              Soy Paciente
            </button>
            <button
              type="button"
              onClick={() => setLoginType('funcionario')}
              className={`flex-1 py-3 px-4 rounded-lg font-medium transition-colors ${
                loginType === 'funcionario'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600'
              }`}
            >
              <IdCard className="w-5 h-5 inline mr-2" />
              Soy Funcionario
            </button>
          </div>

          {/* Error Message */}
          {error && (
            <div className="mb-6 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg flex items-start gap-3">
              <AlertCircle className="w-5 h-5 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
              <p className="text-sm text-red-800 dark:text-red-300">{error}</p>
            </div>
          )}

          {/* Login Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            {loginType === 'paciente' ? (
              <>
           {/* Nombre Field */}
                 <div>
                   <label htmlFor="nombre" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                     Nombre
                   </label>
                   <div className="relative">
                     <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                       <UserCircle className="h-5 w-5 text-gray-400" />
                     </div>
                     <input
                       id="nombre"
                       type="text"
                       value={nombre}
                       onChange={(e) => {
                         setNombre(e.target.value);
                         if (erroresCampo.nombre) {
                           setErroresCampo(prev => ({ ...prev, nombre: '' }));
                         }
                       }}
                       className={`block w-full pl-10 pr-3 py-3 border rounded-lg focus:ring-2 focus:border-transparent dark:bg-gray-700 dark:text-white transition-colors ${
                         erroresCampo.nombre
                           ? 'border-red-500 focus:ring-red-500'
                           : 'border-gray-300 dark:border-gray-600 focus:ring-blue-500'
                       }`}
                       placeholder="Ingresa tu nombre"
                       required
                     />
                   </div>
                   {erroresCampo.nombre && (
                     <p className="text-red-600 dark:text-red-400 text-xs mt-1.5 flex items-center gap-1">
                       <span>⚠</span> {erroresCampo.nombre}
                     </p>
                   )}
                 </div>

                 {/* Apellido Field */}
                 <div>
                   <label htmlFor="apellido" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                     Apellido
                   </label>
                   <div className="relative">
                     <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                       <UserCircle className="h-5 w-5 text-gray-400" />
                     </div>
                     <input
                       id="apellido"
                       type="text"
                       value={apellido}
                       onChange={(e) => {
                         setApellido(e.target.value);
                         if (erroresCampo.apellido) {
                           setErroresCampo(prev => ({ ...prev, apellido: '' }));
                         }
                       }}
                       className={`block w-full pl-10 pr-3 py-3 border rounded-lg focus:ring-2 focus:border-transparent dark:bg-gray-700 dark:text-white transition-colors ${
                         erroresCampo.apellido
                           ? 'border-red-500 focus:ring-red-500'
                           : 'border-gray-300 dark:border-gray-600 focus:ring-blue-500'
                       }`}
                       placeholder="Ingresa tu apellido"
                       required
                     />
                   </div>
                   {erroresCampo.apellido && (
                     <p className="text-red-600 dark:text-red-400 text-xs mt-1.5 flex items-center gap-1">
                       <span>⚠</span> {erroresCampo.apellido}
                     </p>
                   )}
                 </div>

                 {/* RUT Field */}
                 <div>
                   <label htmlFor="rut" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                     RUT
                   </label>
                   <div className="relative">
                     <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                       <IdCard className="h-5 w-5 text-gray-400" />
                     </div>
                     <input
                       id="rut"
                       type="text"
                       value={rut}
                       onChange={(e) => {
                         setRut(e.target.value);
                         if (erroresCampo.rut) {
                           setErroresCampo(prev => ({ ...prev, rut: '' }));
                         }
                       }}
                       className={`block w-full pl-10 pr-3 py-3 border rounded-lg focus:ring-2 focus:border-transparent dark:bg-gray-700 dark:text-white transition-colors ${
                         erroresCampo.rut
                           ? 'border-red-500 focus:ring-red-500'
                           : 'border-gray-300 dark:border-gray-600 focus:ring-blue-500'
                       }`}
                       placeholder="Ej: 12.345.678-9 o 12345678-9"
                       required
                     />
                   </div>
                  {erroresCampo.rut && (
                      <p className="text-red-600 dark:text-red-400 text-xs mt-1.5 flex items-center gap-1">
                        <span>⚠</span> {erroresCampo.rut}
                      </p>
                    )}
                  </div>

                 {/* Email Field */}
                 <div>
                   <label htmlFor="email" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                     Correo electrónico <span className="text-gray-400 dark:text-gray-500 font-normal">(opcional)</span>
                   </label>
                   <div className="relative">
                     <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                       <Mail className="h-5 w-5 text-gray-400" />
                     </div>
                     <input
                       id="email"
                       type="email"
                       value={email}
                       onChange={(e) => {
                         setEmail(e.target.value);
                         if (erroresCampo.email) {
                           setErroresCampo(prev => ({ ...prev, email: '' }));
                         }
                       }}
                       className={`block w-full pl-10 pr-3 py-3 border rounded-lg focus:ring-2 focus:border-transparent dark:bg-gray-700 dark:text-white transition-colors ${
                         erroresCampo.email
                           ? 'border-red-500 focus:ring-red-500'
                           : 'border-gray-300 dark:border-gray-600 focus:ring-blue-500'
                       }`}
                       placeholder="ej: paciente@correo.cl"
                     />
                   </div>
                   {erroresCampo.email && (
                     <p className="text-red-600 dark:text-red-400 text-xs mt-1.5 flex items-center gap-1">
                       <span>⚠</span> {erroresCampo.email}
                     </p>
                   )}
                 </div>
               </>
             ) : (
              <>
                 {/* Username Field */}
                 <div>
                   <label htmlFor="username" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                     Usuario
                   </label>
                   <div className="relative">
                     <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                       <User className="h-5 w-5 text-gray-400" />
                     </div>
                     <input
                       id="username"
                       type="text"
                       value={username}
                       onChange={(e) => {
                         setUsername(e.target.value);
                         if (erroresCampo.username) {
                           setErroresCampo(prev => ({ ...prev, username: '' }));
                         }
                       }}
                       className={`block w-full pl-10 pr-3 py-3 border rounded-lg focus:ring-2 focus:border-transparent dark:bg-gray-700 dark:text-white transition-colors ${
                         erroresCampo.username
                           ? 'border-red-500 focus:ring-red-500'
                           : 'border-gray-300 dark:border-gray-600 focus:ring-blue-500'
                       }`}
                       placeholder="Ingresa tu usuario"
                       required
                     />
                   </div>
                   {erroresCampo.username && (
                     <p className="text-red-600 dark:text-red-400 text-xs mt-1.5 flex items-center gap-1">
                       <span>⚠</span> {erroresCampo.username}
                     </p>
                   )}
                 </div>

                 {/* Password Field */}
                 <div>
                   <label htmlFor="password" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                     Contraseña
                   </label>
                   <div className="relative">
                     <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                       <Lock className="h-5 w-5 text-gray-400" />
                     </div>
                     <input
                       id="password"
                       type="password"
                       value={password}
                       onChange={(e) => {
                         setPassword(e.target.value);
                         if (erroresCampo.password) {
                           setErroresCampo(prev => ({ ...prev, password: '' }));
                         }
                       }}
                       className={`block w-full pl-10 pr-3 py-3 border rounded-lg focus:ring-2 focus:border-transparent dark:bg-gray-700 dark:text-white transition-colors ${
                         erroresCampo.password
                           ? 'border-red-500 focus:ring-red-500'
                           : 'border-gray-300 dark:border-gray-600 focus:ring-blue-500'
                       }`}
                       placeholder="Ingresa tu contraseña"
                       required
                     />
                   </div>
                   {erroresCampo.password && (
                     <p className="text-red-600 dark:text-red-400 text-xs mt-1.5 flex items-center gap-1">
                       <span>⚠</span> {erroresCampo.password}
                     </p>
                   )}
                 </div>
              </>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-semibold py-3 px-4 rounded-lg transition-colors focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 dark:focus:ring-offset-gray-800"
            >
              {isLoading ? (
                <>
                  <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  <span>Iniciando sesión...</span>
                </>
              ) : (
                <>
                  <LogIn className="w-5 h-5" />
                  <span>{loginType === 'paciente' ? 'Ingresar como Paciente' : 'Iniciar Sesión'}</span>
                </>
              )}
            </button>
          </form>

          {/* Demo Credentials */}
          {loginType === 'funcionario' && (
            <div className="mt-8 p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
              <p className="text-sm font-medium text-blue-900 dark:text-blue-300 mb-2">
                Credenciales de demostración:
              </p>
              <div className="space-y-1 text-xs text-blue-800 dark:text-blue-400">
                <p><strong>Funcionario:</strong> funcionario / funcionario123</p>
                <p><strong>Admin:</strong> admin / admin123</p>
              </div>
            </div>
          )}
          
          {loginType === 'paciente' && (
            <div className="mt-8 p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
              <p className="text-sm font-medium text-green-900 dark:text-green-300 mb-2">
                Información para pacientes:
              </p>
              <div className="space-y-1 text-xs text-green-800 dark:text-green-400">
                <p>Ingresa tu nombre, apellido, RUT y correo (opcional) para acceder.</p>
                <p>Si es tu primera vez, se creará tu perfil automáticamente.</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Login;
