package com.saludrednorte.ms_auth.validation;

import com.saludrednorte.ms_auth.util.RutUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador para la anotación @RutValid.
 * Utiliza RutUtil para validar el formato y dígito verificador del RUT chileno.
 */
public class RutValidator implements ConstraintValidator<RutValid, String> {

    @Override
    public void initialize(RutValid constraintAnnotation) {
        // No se requiere inicialización
    }

    @Override
    public boolean isValid(String rut, ConstraintValidatorContext context) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }
        return RutUtil.validarFormato(rut);
    }
}
