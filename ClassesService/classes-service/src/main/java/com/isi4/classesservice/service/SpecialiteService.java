package com.isi4.classesservice.service;

import com.isi4.classesservice.dto.SpecialiteDTO;
import com.isi4.classesservice.entity.Filiere;
import com.isi4.classesservice.entity.Specialite;
import com.isi4.classesservice.exception.ResourceNotFoundException;
import com.isi4.classesservice.exception.ValidationException;
import com.isi4.classesservice.repository.FiliereRepository;
import com.isi4.classesservice.repository.SpecialiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SpecialiteService {

    private final SpecialiteRepository specialiteRepository;
    private final FiliereRepository filiereRepository;

    @Transactional(readOnly = true)
    public List<SpecialiteDTO> obtenirToutes() {
        return specialiteRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<SpecialiteDTO> obtenirParDepartement(String idFiliere) {
        return specialiteRepository.findByIdFiliere(idFiliere).stream().map(this::toDTO).toList();
    }

    public SpecialiteDTO creer(SpecialiteDTO dto) {
        obtenirDepartement(dto.getIdFiliere());
        if (specialiteRepository.existsByNomAndIdFiliere(dto.getNom().trim(), dto.getIdFiliere())) {
            throw new ValidationException("Cette spécialité existe déjà dans le département.");
        }
        Specialite specialite = new Specialite();
        specialite.setIdSpecialite(UUID.randomUUID().toString());
        mettreAJour(specialite, dto);
        return toDTO(specialiteRepository.save(specialite));
    }

    public SpecialiteDTO mettreAJour(String id, SpecialiteDTO dto) {
        Specialite specialite = specialiteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Spécialité non trouvée : " + id));
        obtenirDepartement(dto.getIdFiliere());
        if (specialiteRepository.existsByNomAndIdFiliereAndIdSpecialiteNot(
                dto.getNom().trim(), dto.getIdFiliere(), id)) {
            throw new ValidationException("Cette spécialité existe déjà dans le département.");
        }
        mettreAJour(specialite, dto);
        return toDTO(specialiteRepository.save(specialite));
    }

    public void supprimer(String id) {
        if (!specialiteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Spécialité non trouvée : " + id);
        }
        specialiteRepository.deleteById(id);
    }

    private void mettreAJour(Specialite specialite, SpecialiteDTO dto) {
        specialite.setNom(dto.getNom().trim());
        specialite.setDescription(dto.getDescription());
        specialite.setIdFiliere(dto.getIdFiliere());
    }

    private Filiere obtenirDepartement(String idFiliere) {
        return filiereRepository.findById(idFiliere)
            .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé : " + idFiliere));
    }

    private SpecialiteDTO toDTO(Specialite specialite) {
        Filiere departement = obtenirDepartement(specialite.getIdFiliere());
        SpecialiteDTO dto = new SpecialiteDTO();
        dto.setIdSpecialite(specialite.getIdSpecialite());
        dto.setNom(specialite.getNom());
        dto.setDescription(specialite.getDescription());
        dto.setIdFiliere(departement.getIdFiliere());
        dto.setNomDepartement(departement.getNom());
        dto.setIdEcole(departement.getIdEcole());
        return dto;
    }
}
