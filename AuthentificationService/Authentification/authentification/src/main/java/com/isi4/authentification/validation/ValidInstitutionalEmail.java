package com.isi4.authentification.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = InstitutionalEmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidInstitutionalEmail {
    String message() default "L'email doit être au format prenom.nom@ictuniversity.org";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
