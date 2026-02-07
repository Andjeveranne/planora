package com.isi4.ueservice.service;

import com.isi4.ueservice.dto.UEDTO;
import com.isi4.ueservice.entity.UE;
import com.isi4.ueservice.exception.ResourceNotFoundException;
import com.isi4.ueservice.exception.ValidationException;
import com.isi4.ueservice.repository.UERepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service CRUD pour les Unités d'enseignement (UE) conformes au diagramme de classes.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UEService {

    private final UERepository ueRepository;

    public UEDTO creer(UEDTO dto) {
        UE ue = toEntity(dto);
        if (ue.getIdUE() == null || ue.getIdUE().isBlank()) {
            ue.setIdUE(UUID.randomUUID().toString());
        } else if (ueRepository.existsByIdUE(ue.getIdUE())) {
            throw new ValidationException("Une UE avec l'ID " + ue.getIdUE() + " existe déjà");
        }
        if (ueRepository.existsByCodeUE(ue.getCodeUE())) {
            throw new ValidationException("Une UE avec le code " + ue.getCodeUE() + " existe déjà");
        }
        UE saved = ueRepository.save(ue);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<UEDTO> obtenirToutes() {
        return ueRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UEDTO obtenirParId(String idUE) {
        UE ue = ueRepository.findById(idUE)
                .orElseThrow(() -> new ResourceNotFoundException("UE non trouvée avec l'ID: " + idUE));
        return toDTO(ue);
    }

    public UEDTO mettreAJour(String idUE, UEDTO dto) {
        UE ue = ueRepository.findById(idUE)
                .orElseThrow(() -> new ResourceNotFoundException("UE non trouvée avec l'ID: " + idUE));
        if (!ue.getCodeUE().equals(dto.getCodeUE()) && ueRepository.existsByCodeUE(dto.getCodeUE())) {
            throw new ValidationException("Une autre UE utilise déjà le code " + dto.getCodeUE());
        }
        ue.setCodeUE(dto.getCodeUE());
        ue.setNomUE(dto.getNomUE());
        ue.setQuantite(dto.getQuantite());
        ue.setDescription(dto.getDescription());
        ue.setNCredit(dto.getNCredit());
        UE updated = ueRepository.save(ue);
        return toDTO(updated);
    }

    public void supprimer(String idUE) {
        if (!ueRepository.existsByIdUE(idUE)) {
            throw new ResourceNotFoundException("UE non trouvée avec l'ID: " + idUE);
        }
        ueRepository.deleteById(idUE);
    }

    private UE toEntity(UEDTO dto) {
        UE u = new UE();
        u.setIdUE(dto.getIdUE());
        u.setCodeUE(dto.getCodeUE());
        u.setNomUE(dto.getNomUE());
        u.setQuantite(dto.getQuantite());
        u.setDescription(dto.getDescription());
        u.setNCredit(dto.getNCredit());
        return u;
    }

    private UEDTO toDTO(UE u) {
        UEDTO dto = new UEDTO();
        dto.setIdUE(u.getIdUE());
        dto.setCodeUE(u.getCodeUE());
        dto.setNomUE(u.getNomUE());
        dto.setQuantite(u.getQuantite());
        dto.setDescription(u.getDescription());
        dto.setNCredit(u.getNCredit());
        return dto;
    }
}
