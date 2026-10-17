package com.isi4.planningservice.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmploiDuTempsServiceV2Test {

    @Test
    void filtersClassesBySelectedSpecialty() {
        Map<String, Object> targetSpecialty = Map.of(
                "idEcole", "school-1",
                "nomFiliere", "Informatique",
                "specialite", "Réseaux",
                "idClasse", "class-networks");
        Map<String, Object> otherSpecialty = Map.of(
                "idEcole", "school-1",
                "nomFiliere", "Informatique",
                "specialite", "Développement",
                "idClasse", "class-dev");

        assertTrue(EmploiDuTempsServiceV2.matchesGenerationScope(
                targetSpecialty, "school-1", "Informatique", "Réseaux"));
        assertFalse(EmploiDuTempsServiceV2.matchesGenerationScope(
                otherSpecialty, "school-1", "Informatique", "Réseaux"));
    }

    @Test
    void excludesClassesWithoutSchoolAssignment() {
        Map<String, Object> unassignedClass = Map.of(
                "nomFiliere", "Informatique",
                "specialite", "Réseaux",
                "idClasse", "class-unassigned");

        assertFalse(EmploiDuTempsServiceV2.matchesGenerationScope(
                unassignedClass, null, "Informatique", "Réseaux"));
    }

    @Test
    void matchesEngineeringClassLevelWithItsUeLevel() {
        assertTrue(EmploiDuTempsServiceV2.niveauxCompatibles("L1", "ING1"));
        assertTrue(EmploiDuTempsServiceV2.niveauxCompatibles("L5", "ING5"));
        assertFalse(EmploiDuTempsServiceV2.niveauxCompatibles("L1", "ING2"));
    }

    @Test
    void matchesMasterLevelAliases() {
        assertTrue(EmploiDuTempsServiceV2.niveauxCompatibles("Master1", "M1"));
        assertTrue(EmploiDuTempsServiceV2.niveauxCompatibles("Master2", "M2"));
        assertFalse(EmploiDuTempsServiceV2.niveauxCompatibles("Master1", "M2"));
    }
}
