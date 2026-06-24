package com.saludrednorte.ms_auth.util;

import java.util.regex.Pattern;

/**
 * Utilidad para validación y formateo de RUT chileno.
 * Formato estándar: XX.XXX.XXX-X (ej: 12.345.678-9)
 */
public class RutUtil {

    private static final Pattern RUT_PATTERN = Pattern.compile("^[0-9]+-[0-9Kk]$");
    private static final Pattern RUT_CLEAN_PATTERN = Pattern.compile("^[0-9]+$");

    /**
     * Limpia un RUT removiendo puntos y guiones, dejando solo números y dígito verificador.
     * Ejemplo: "12.345.678-9" -> "123456789"
     */
    public static String limpiarRut(String rut) {
        if (rut == null) {
            return null;
        }
        return rut.replaceAll("[.\\s]", "").replace("-", "");
    }

    /**
     * Formatea un RUT al formato estándar chileno XX.XXX.XXX-X.
     * Acepta RUTs con o sin formato.
     * Ejemplo: "123456789" -> "12.345.678-9"
     */
    public static String formatearRut(String rut) {
        if (rut == null || rut.isEmpty()) {
            return rut;
        }

        String rutLimpio = limpiarRut(rut);
        if (rutLimpio.length() < 2) {
            return rut;
        }

        // Separar cuerpo y dígito verificador
        String cuerpo = rutLimpio.substring(0, rutLimpio.length() - 1);
        String dv = rutLimpio.substring(rutLimpio.length() - 1);

        // Formatear cuerpo con puntos
        StringBuilder sb = new StringBuilder();
        int contador = 0;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            sb.insert(0, cuerpo.charAt(i));
            contador++;
            if (contador == 3 && i > 0) {
                sb.insert(0, ".");
                contador = 0;
            }
        }

        return sb.toString() + "-" + dv.toUpperCase();
    }

    /**
     * Valida el formato de un RUT chileno.
     * Acepta RUTs con o sin formato (con puntos y guion).
     */
    public static boolean validarFormato(String rut) {
        if (rut == null || rut.isEmpty()) {
            return false;
        }

        String rutLimpio = limpiarRut(rut);
        
        // Debe tener al menos 2 caracteres (1 número + dígito verificador)
        if (rutLimpio.length() < 2) {
            return false;
        }

        // Debe contener solo números y un dígito verificador al final
        if (!RUT_CLEAN_PATTERN.matcher(rutLimpio).matches()) {
            return false;
        }

        // Validar dígito verificador
        return validarDigitoVerificador(rutLimpio);
    }

    /**
     * Valida el dígito verificador del RUT usando el algoritmo módulo 11.
     */
    private static boolean validarDigitoVerificador(String rutLimpio) {
        String cuerpo = rutLimpio.substring(0, rutLimpio.length() - 1);
        char dvEsperado = rutLimpio.charAt(rutLimpio.length() - 1);

        int suma = 0;
        int multiplicador = 2;

        // Calcular suma ponderada de derecha a izquierda
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador++;
            if (multiplicador > 7) {
                multiplicador = 2;
            }
        }

        int dvCalculado = 11 - (suma % 11);
        char dvChar;

        if (dvCalculado == 11) {
            dvChar = '0';
        } else if (dvCalculado == 10) {
            dvChar = 'K';
        } else {
            dvChar = Character.forDigit(dvCalculado, 10);
        }

        return Character.toUpperCase(dvEsperado) == dvChar;
    }

    /**
     * Normaliza un RUT: limpia, valida y formatea.
     * Lanza IllegalArgumentException si el formato es inválido.
     */
    public static String normalizarRut(String rut) {
        if (!validarFormato(rut)) {
            throw new IllegalArgumentException("RUT inválido: " + rut);
        }
        return formatearRut(rut);
    }
}
