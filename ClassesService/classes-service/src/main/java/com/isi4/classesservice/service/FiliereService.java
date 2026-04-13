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
        if (filiereRepository.existsByNom(dto.getNom())) {
            throw new ValidationException("Une filière avec le nom '" + dto.getNom() + "' existe déjà");
        }
        Filiere f = new Filiere();
        f.setIdFiliere(dto.getIdFiliere() != null ? dto.getIdFiliere() : UUID.randomUUID().toString());
        f.setNom(dto.getNom());
        f.setDescription(dto.getDescription());
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
        Filiere f = filiereRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée : " + id));
        if (!f.getNom().equals(dto.getNom()) && filiereRepository.existsByNom(dto.getNom())) {
            throw new ValidationException("Une filière avec le nom '" + dto.getNom() + "' existe déjà");
        }
        f.setNom(dto.getNom());
        f.setDescription(dto.getDescription());
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
        if (f.getClasses() != null) {
            dto.setClasses(f.getClasses().stream().map(c -> {
                ClasseDTO cd = new ClasseDTO();
                cd.setIdClasse(c.getIdClasse());
                cd.setNomClasse(c.getNomClasse());
                cd.setEffectifs(c.getEffectifs());
                cd.setNiveau(c.getNiveau());
                cd.setIdFiliere(f.getIdFiliere());
                cd.setNomFiliere(f.getNom());
                return cd;
            }).collect(Collectors.toList()));
        }
        return dto;
    }
}
