/**
 * Validaciones y mensajes de error amigables para los usuarios
 */

// Validar RUT chileno (formato: XX.XXX.XXX-K o XXXXXXXX-K o sin separadores)
export const validarRUT = (rut) => {
  if (!rut || !rut.trim()) {
    return { valido: false, mensaje: 'El RUT es requerido' };
  }

  // Limpiar el RUT (remover espacios y puntos)
  const rutLimpio = rut.replace(/\s+/g, '').replace(/\./g, '').toUpperCase();

  // Formato básico: dígitos (+ opcional guión) y dígito verificador (0-9 o K)
  if (!/^[0-9]+-?[0-9K]$/.test(rutLimpio)) {
    return { valido: false, mensaje: 'El RUT debe tener formato válido. Ejemplos: 12345678-9 o 12.345.678-9' };
  }

  // Separar número y dígito verificador correctamente
  let numeros;
  let dvIngresado;
  if (rutLimpio.includes('-')) {
    const partes = rutLimpio.split('-');
    numeros = partes[0];
    dvIngresado = partes[1];
  } else {
    // Si no tiene guión, el último carácter es el DV
    numeros = rutLimpio.slice(0, -1);
    dvIngresado = rutLimpio.slice(-1);
  }

  // Validar longitud del número (6 a 8 dígitos)
  if (numeros.length < 6 || numeros.length > 8) {
    return { valido: false, mensaje: 'El RUT debe contener entre 6 y 8 números antes del dígito verificador' };
  }

  // Calcular dígito verificador
  let suma = 0;
  let multiplicador = 2;

  for (let i = numeros.length - 1; i >= 0; i--) {
    suma += parseInt(numeros[i]) * multiplicador;
    multiplicador = multiplicador === 7 ? 2 : multiplicador + 1;
  }

  const dvCalculado = 11 - (suma % 11);
  const dvEsperado =
    dvCalculado === 11 ? '0' : dvCalculado === 10 ? 'K' : String(dvCalculado);

  if (dvIngresado !== dvEsperado) {
    return {
      valido: false,
      mensaje: 'El RUT ingresado no es válido. Verifica los números y el dígito verificador.',
    };
  }

  return { valido: true, mensaje: '' };
};

// Validar nombre (al menos 2 caracteres, sin números)
export const validarNombre = (nombre) => {
  if (!nombre || !nombre.trim()) {
    return { valido: false, mensaje: 'El nombre es requerido' };
  }

  if (nombre.trim().length < 2) {
    return { valido: false, mensaje: 'El nombre debe tener al menos 2 caracteres' };
  }

  if (/\d/.test(nombre)) {
    return { valido: false, mensaje: 'El nombre no puede contener números' };
  }

  return { valido: true, mensaje: '' };
};

// Validar apellido (al menos 2 caracteres, sin números)
export const validarApellido = (apellido) => {
  if (!apellido || !apellido.trim()) {
    return { valido: false, mensaje: 'El apellido es requerido' };
  }

  if (apellido.trim().length < 2) {
    return { valido: false, mensaje: 'El apellido debe tener al menos 2 caracteres' };
  }

  if (/\d/.test(apellido)) {
    return { valido: false, mensaje: 'El apellido no puede contener números' };
  }

  return { valido: true, mensaje: '' };
};

// Validar email (formato básico)
export const validarEmail = (email) => {
  if (!email || !email.trim()) {
    return { valido: true, mensaje: '' }; // Email opcional
  }

  const regexEmail =
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!regexEmail.test(email)) {
    return { valido: false, mensaje: 'Ingresa un correo electrónico válido' };
  }

  return { valido: true, mensaje: '' };
};

// Validar teléfono (al menos 9 dígitos)
export const validarTelefono = (telefono) => {
  if (!telefono || !telefono.trim()) {
    return { valido: true, mensaje: '' }; // Teléfono opcional
  }

  const soloDigitos = telefono.replace(/\D/g, '');

  if (soloDigitos.length < 9) {
    return { valido: false, mensaje: 'El teléfono debe tener al menos 9 dígitos' };
  }

  if (soloDigitos.length > 15) {
    return { valido: false, mensaje: 'El teléfono no puede tener más de 15 dígitos' };
  }

  return { valido: true, mensaje: '' };
};

// Validar DNI (al menos 6 dígitos, máximo 12)
export const validarDNI = (dni) => {
  if (!dni || !dni.trim()) {
    return { valido: false, mensaje: 'El DNI es requerido' };
  }

  const soloDigitos = dni.replace(/\D/g, '');

  if (soloDigitos.length < 6) {
    return { valido: false, mensaje: 'El DNI debe tener al menos 6 dígitos' };
  }

  if (soloDigitos.length > 12) {
    return { valido: false, mensaje: 'El DNI no puede tener más de 12 dígitos' };
  }

  return { valido: true, mensaje: '' };
};

// Validar contraseña (al menos 6 caracteres)
export const validarPassword = (password) => {
  if (!password || !password.trim()) {
    return { valido: false, mensaje: 'La contraseña es requerida' };
  }

  if (password.length < 6) {
    return { valido: false, mensaje: 'La contraseña debe tener al menos 6 caracteres' };
  }

  return { valido: true, mensaje: '' };
};

// Validar usuario (al menos 3 caracteres)
export const validarUsuario = (usuario) => {
  if (!usuario || !usuario.trim()) {
    return { valido: false, mensaje: 'El usuario es requerido' };
  }

  if (usuario.trim().length < 3) {
    return { valido: false, mensaje: 'El usuario debe tener al menos 3 caracteres' };
  }

  return { valido: true, mensaje: '' };
};



