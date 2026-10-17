package com.isi4.salleservice.service;

import com.isi4.salleservice.dto.SalleDTO;
import com.isi4.salleservice.entity.Salle;
import com.isi4.salleservice.enums.StatutSalle;
import com.isi4.salleservice.enums.TypeSalle;
import com.isi4.salleservice.repository.SalleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SalleServiceTest {

    private final SalleRepository salleRepository = mock(SalleRepository.class);
    private final SalleService salleService = new SalleService(salleRepository);

    @Test
    void creerSallePreservesClasseAssociationForComputerLab() {
        SalleDTO dto = salleDto("ICTU SWE Lab", TypeSalle.SALLE_INFORMATIQUE, "class-swe");
        when(salleRepository.existsByNomSalleAndIdEcole(dto.getNomSalle(), dto.getIdEcole()))
                .thenReturn(false);
        when(salleRepository.save(any(Salle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SalleDTO saved = salleService.creerSalle(dto);

        assertEquals("class-swe", saved.getIdClasse());
    }

    @Test
    void mettreAJourSallePreservesClasseAssociationForComputerLab() {
        Salle existing = Salle.builder()
                .idSalle("lab-1")
                .nomSalle("ICTU SWE Lab")
                .type(TypeSalle.SALLE_INFORMATIQUE)
                .localisation("ICTU")
                .capacite(40)
                .capaciteExam(30)
                .idEcole("ICTU")
                .statut(StatutSalle.DISPONIBLE)
                .build();
        SalleDTO dto = salleDto("ICTU SWE Lab", TypeSalle.SALLE_INFORMATIQUE, "class-swe");
        when(salleRepository.findById("lab-1")).thenReturn(Optional.of(existing));
        when(salleRepository.save(any(Salle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalleDTO saved = salleService.mettreAJourSalle("lab-1", dto);

        assertEquals("class-swe", saved.getIdClasse());
    }

    private static SalleDTO salleDto(String name, TypeSalle type, String classId) {
        SalleDTO dto = new SalleDTO();
        dto.setNomSalle(name);
        dto.setType(type);
        dto.setLocalisation("ICTU");
        dto.setCapacite(40);
        dto.setCapaciteExam(30);
        dto.setIdEcole("ICTU");
        dto.setIdClasse(classId);
        dto.setStatut(StatutSalle.DISPONIBLE);
        return dto;
    }
}
