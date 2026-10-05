package com.isi4.authentification.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class InstitutionalEmailValidator implements ConstraintValidator<ValidInstitutionalEmail, String> {
    
    // Pattern pour valider le format prenom.nom@ictuniversity.org
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-z]+\\.[a-z]+@ictuniversity\\.org$",
        Pattern.CASE_INSENSITIVE
    );
    
    @Override
    public void initialize(ValidInstitutionalEmail constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
