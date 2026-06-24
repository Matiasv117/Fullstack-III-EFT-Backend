import { describe, it, expect } from 'vitest';
import {
  validarRUT,
  validarNombre,
  validarApellido,
  validarEmail,
  validarTelefono,
  validarDNI,
  validarPassword,
  validarUsuario,
} from './validations';

describe('Validaciones Frontend', () => {
  // Pruebas de RUT
  describe('validarRUT', () => {
    it('deberia aceptar RUT válido con puntos: 12.345.678-5', () => {
      const resultado = validarRUT('12.345.678-5');
      expect(resultado.valido).toBe(true);
    });

    it('deberia aceptar RUT válido sin puntos: 12345678-5', () => {
      const resultado = validarRUT('12345678-5');
      expect(resultado.valido).toBe(true);
    });

    it('debería rechazar RUT vacío', () => {
      const resultado = validarRUT('');
      expect(resultado.valido).toBe(false);
      expect(resultado.mensaje).toBe('El RUT es requerido');
    });

    it('debería rechazar RUT con formato inválido', () => {
      const resultado = validarRUT('invalid');
      expect(resultado.valido).toBe(false);
    });

    it('debería rechazar RUT con dígito verificador incorrecto', () => {
      const resultado = validarRUT('12.345.678-0');
      expect(resultado.valido).toBe(false);
    });
  });

  // Pruebas de Nombre
  describe('validarNombre', () => {
    it('debería aceptar nombre válido', () => {
      const resultado = validarNombre('Juan');
      expect(resultado.valido).toBe(true);
    });

    it('debería rechazar nombre vacío', () => {
      const resultado = validarNombre('');
      expect(resultado.valido).toBe(false);
      expect(resultado.mensaje).toBe('El nombre es requerido');
    });

    it('debería rechazar nombre muy corto', () => {
      const resultado = validarNombre('A');
      expect(resultado.valido).toBe(false);
    });

    it('debería rechazar nombre con números', () => {
      const resultado = validarNombre('Juan123');
      expect(resultado.valido).toBe(false);
      expect(resultado.mensaje).toBe('El nombre no puede contener números');
    });
  });

  // Pruebas de Apellido
  describe('validarApellido', () => {
    it('debería aceptar apellido válido', () => {
      const resultado = validarApellido('García');
      expect(resultado.valido).toBe(true);
    });

    it('debería rechazar apellido vacío', () => {
      const resultado = validarApellido('');
      expect(resultado.valido).toBe(false);
      expect(resultado.mensaje).toBe('El apellido es requerido');
    });

    it('debería rechazar apellido con números', () => {
      const resultado = validarApellido('García123');
      expect(resultado.valido).toBe(false);
    });
  });

  // Pruebas de Email
  describe('validarEmail', () => {
    it('debería aceptar email válido', () => {
      const resultado = validarEmail('usuario@ejemplo.com');
      expect(resultado.valido).toBe(true);
    });

    it('debería aceptar email vacío (opcional)', () => {
      const resultado = validarEmail('');
      expect(resultado.valido).toBe(true);
    });

    it('debería rechazar email sin arroba', () => {
      const resultado = validarEmail('usuarioejemplo.com');
      expect(resultado.valido).toBe(false);
    });

    it('debería rechazar email sin punto', () => {
      const resultado = validarEmail('usuario@ejemplo');
      expect(resultado.valido).toBe(false);
    });
  });

  // Pruebas de Teléfono
  describe('validarTelefono', () => {
    it('debería aceptar teléfono válido', () => {
      const resultado = validarTelefono('912345678');
      expect(resultado.valido).toBe(true);
    });

    it('debería aceptar teléfono con separadores', () => {
      const resultado = validarTelefono('(9) 1234-5678');
      expect(resultado.valido).toBe(true);
    });

    it('debería aceptar teléfono vacío (opcional)', () => {
      const resultado = validarTelefono('');
      expect(resultado.valido).toBe(true);
    });

    it('debería rechazar teléfono con pocos dígitos', () => {
      const resultado = validarTelefono('91234');
      expect(resultado.valido).toBe(false);
      expect(resultado.mensaje).toBe('El teléfono debe tener al menos 9 dígitos');
    });
  });

  // Pruebas de DNI
  describe('validarDNI', () => {
    it('debería aceptar DNI válido', () => {
      const resultado = validarDNI('12345678');
      expect(resultado.valido).toBe(true);
    });

    it('debería rechazar DNI vacío', () => {
      const resultado = validarDNI('');
      expect(resultado.valido).toBe(false);
    });

    it('debería rechazar DNI muy corto', () => {
      const resultado = validarDNI('12345');
      expect(resultado.valido).toBe(false);
      expect(resultado.mensaje).toBe('El DNI debe tener al menos 6 dígitos');
    });
  });

  // Pruebas de Password
  describe('validarPassword', () => {
    it('debería aceptar contraseña válida', () => {
      const resultado = validarPassword('micontraseña123');
      expect(resultado.valido).toBe(true);
    });

    it('debería rechazar contraseña vacía', () => {
      const resultado = validarPassword('');
      expect(resultado.valido).toBe(false);
    });

    it('debería rechazar contraseña muy corta', () => {
      const resultado = validarPassword('12345');
      expect(resultado.valido).toBe(false);
      expect(resultado.mensaje).toBe('La contraseña debe tener al menos 6 caracteres');
    });
  });

  // Pruebas de Usuario
  describe('validarUsuario', () => {
    it('debería aceptar usuario válido', () => {
      const resultado = validarUsuario('admin123');
      expect(resultado.valido).toBe(true);
    });

    it('debería rechazar usuario vacío', () => {
      const resultado = validarUsuario('');
      expect(resultado.valido).toBe(false);
    });

    it('debería rechazar usuario muy corto', () => {
      const resultado = validarUsuario('ad');
      expect(resultado.valido).toBe(false);
      expect(resultado.mensaje).toBe('El usuario debe tener al menos 3 caracteres');
    });
  });
});


