package com.isi4.classesservice.service;

import com.isi4.classesservice.dto.ClasseDTO;
import com.isi4.classesservice.dto.FiliereDTO;
import com.isi4.classesservice.entity.Filiere;
import com.isi4.classesservice.exception.ResourceNotFoundException;
import com.isi4.classesservice.exception.ValidationException;
import com.isi4.classesservice.repository.FiliereRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FiliereService {

    private final FiliereRepository filiereRepository;

    public FiliereDTO creer(FiliereDTO dto) {
        validerEcole(dto);
        if (filiereRepository.existsByNomAndIdEcole(dto.getNom(), dto.getIdEcole())) {
            throw new ValidationException("Un département avec le nom '" + dto.getNom() + "' existe déjà dans cette école");
        }
        Filiere f = new Filiere();
        f.setIdFiliere(dto.getIdFiliere() != null ? dto.getIdFiliere() : UUID.randomUUID().toString());
        f.setNom(dto.getNom());
        f.setDescription(dto.getDescription());
        f.setIdEcole(dto.getIdEcole());
        return toDTO(filiereRepository.save(f));
    }

    @Transactional(readOnly = true)
    public List<FiliereDTO> obtenirToutes() {
        return filiereRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FiliereDTO obtenirParId(String id) {
        return toDTO(filiereRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée : " + id)));
    }

    public FiliereDTO mettreAJour(String id, FiliereDTO dto) {
        validerEcole(dto);
        Filiere f = filiereRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée : " + id));
        if (filiereRepository.existsByNomAndIdEcoleAndIdFiliereNot(dto.getNom(), dto.getIdEcole(), id)) {
            throw new ValidationException("Un département avec le nom '" + dto.getNom() + "' existe déjà dans cette école");
        }
        f.setNom(dto.getNom());
        f.setDescription(dto.getDescription());
        f.setIdEcole(dto.getIdEcole());
        return toDTO(filiereRepository.save(f));
    }

    public void supprimer(String id) {
        if (!filiereRepository.existsById(id)) {
            throw new ResourceNotFoundException("Filière non trouvée : " + id);
        }
        filiereRepository.deleteById(id);
    }

    public FiliereDTO toDTO(Filiere f) {
        FiliereDTO dto = new FiliereDTO();
        dto.setIdFiliere(f.getIdFiliere());
        dto.setNom(f.getNom());
        dto.setDescription(f.getDescription());
        dto.setIdEcole(f.getIdEcole());
        if (f.getClasses() != null) {
            dto.setClasses(f.getClasses().stream().map(c -> {
                ClasseDTO cd = new ClasseDTO();
                cd.setIdClasse(c.getIdClasse());
                cd.setNomClasse(c.getNomClasse());
                cd.setEffectifs(c.getEffectifs());
                cd.setNiveau(c.getNiveau());
                cd.setIdFiliere(f.getIdFiliere());
                cd.setSpecialite(c.getSpecialite());
                cd.setNomFiliere(f.getNom());
                cd.setIdEcole(f.getIdEcole());
                return cd;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private void validerEcole(FiliereDTO dto) {
        if (dto.getIdEcole() == null || dto.getIdEcole().isBlank()) {
            throw new ValidationException("L'école du département est obligatoire");
        }
    }
}
