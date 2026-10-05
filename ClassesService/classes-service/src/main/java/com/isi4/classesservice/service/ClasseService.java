package com.isi4.classesservice.service;

import com.isi4.classesservice.dto.ClasseDTO;
import com.isi4.classesservice.entity.Classe;
import com.isi4.classesservice.entity.Filiere;
import com.isi4.classesservice.exception.ResourceNotFoundException;
import com.isi4.classesservice.exception.ValidationException;
import com.isi4.classesservice.repository.ClasseRepository;
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
public class ClasseService {

    private final ClasseRepository classeRepository;
    private final FiliereRepository filiereRepository;

    public ClasseDTO creer(ClasseDTO dto) {
        if (classeRepository.existsByNomClasse(dto.getNomClasse())) {
            throw new ValidationException("Une classe '" + dto.getNomClasse() + "' existe déjà");
        }
        Filiere filiere = filiereRepository.findById(dto.getIdFiliere())
                .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée : " + dto.getIdFiliere()));
        Classe c = new Classe();
        c.setIdClasse(dto.getIdClasse() != null ? dto.getIdClasse() : UUID.randomUUID().toString());
        c.setNomClasse(dto.getNomClasse());
        c.setEffectifs(dto.getEffectifs());
        c.setNiveau(dto.getNiveau());
        c.setFiliere(filiere);
        return toDTO(classeRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<ClasseDTO> obtenirToutes() {
        return classeRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClasseDTO obtenirParId(String id) {
        return toDTO(classeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée : " + id)));
    }

    @Transactional(readOnly = true)
    public List<ClasseDTO> obtenirParFiliere(String idFiliere) {
        return classeRepository.findByFiliereIdFiliere(idFiliere).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClasseDTO> obtenirParEcole(String idEcole) {
        return classeRepository.findByFiliereIdEcole(idEcole).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ClasseDTO mettreAJour(String id, ClasseDTO dto) {
        Classe c = classeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée : " + id));
        if (!c.getNomClasse().equals(dto.getNomClasse()) && classeRepository.existsByNomClasse(dto.getNomClasse())) {
            throw new ValidationException("Une classe '" + dto.getNomClasse() + "' existe déjà");
        }
        Filiere filiere = filiereRepository.findById(dto.getIdFiliere())
                .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée : " + dto.getIdFiliere()));
        c.setNomClasse(dto.getNomClasse());
        c.setEffectifs(dto.getEffectifs());
        c.setNiveau(dto.getNiveau());
        c.setFiliere(filiere);
        return toDTO(classeRepository.save(c));
    }

    public void supprimer(String id) {
        if (!classeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Classe non trouvée : " + id);
        }
        classeRepository.deleteById(id);
    }

    public ClasseDTO toDTO(Classe c) {
        ClasseDTO dto = new ClasseDTO();
        dto.setIdClasse(c.getIdClasse());
        dto.setNomClasse(c.getNomClasse());
        dto.setEffectifs(c.getEffectifs());
        dto.setNiveau(c.getNiveau());
        if (c.getFiliere() != null) {
            dto.setIdFiliere(c.getFiliere().getIdFiliere());
            dto.setNomFiliere(c.getFiliere().getNom());
            dto.setIdEcole(c.getFiliere().getIdEcole());
        }
        return dto;
    }
}
