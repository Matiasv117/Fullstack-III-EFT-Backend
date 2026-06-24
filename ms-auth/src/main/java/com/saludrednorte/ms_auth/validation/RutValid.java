package com.saludrednorte.ms_auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotación de validación personalizada para RUT chileno.
 * Valida el formato y el dígito verificador del RUT.
 */
@Documented
@Constraint(validatedBy = RutValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RutValid {
    String message() default "RUT chileno inválido. Formato esperado: XX.XXX.XXX-X";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
