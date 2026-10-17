package com.isi4.userservice.service;

import com.isi4.userservice.dto.EnseignantUEDTO;
import com.isi4.userservice.entity.EnseignantUE;
import com.isi4.userservice.exception.ValidationException;
import com.isi4.userservice.repository.EnseignantUERepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnseignantUEServiceTest {

    @Mock
    private EnseignantUERepository repository;

    @InjectMocks
    private EnseignantUEService service;

    @Test
    void rejectsSecondTeacherForSameUEAndSchool() {
        EnseignantUEDTO dto = new EnseignantUEDTO();
        dto.setUeId(12L);
        dto.setEnseignantId("teacher-b");
        dto.setIdEcole("school-a");
        dto.setCodeUE("INFO101");
        dto.setNomUE("Algorithmique");

        EnseignantUE owner = new EnseignantUE();
        owner.setEnseignantId("teacher-a");
        when(repository.findByEnseignantIdAndUeIdAndIdEcole("teacher-b", 12L, "school-a"))
                .thenReturn(Optional.empty());
        when(repository.findByUeIdAndIdEcole(12L, "school-a")).thenReturn(Optional.of(owner));

        assertThrows(ValidationException.class, () -> service.ajouterCompetence(dto));
    }
}
