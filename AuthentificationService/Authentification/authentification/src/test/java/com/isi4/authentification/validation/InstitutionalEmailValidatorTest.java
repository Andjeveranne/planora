package com.isi4.authentification.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstitutionalEmailValidatorTest {

    private final InstitutionalEmailValidator validator = new InstitutionalEmailValidator();

    @Test
    void acceptsIctUniversityInstitutionalAddress() {
        assertTrue(validator.isValid("jean.dupont@ictuniversity.org", null));
    }

    @Test
    void rejectsFormerInstitutionalDomain() {
        assertFalse(validator.isValid("jean.dupont@saintjeaningenieur.org", null));
    }
}
