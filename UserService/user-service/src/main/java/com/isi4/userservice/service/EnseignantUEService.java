package com.isi4.userservice.service;

import com.isi4.userservice.dto.EnseignantUEDTO;
import com.isi4.userservice.entity.EnseignantUE;
import com.isi4.userservice.repository.EnseignantUERepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnseignantUEService {

    private final EnseignantUERepository enseignantUERepository;

    public List<EnseignantUEDTO> getUEsByEnseignant(String enseignantId) {
        return enseignantUERepository.findByEnseignantId(enseignantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EnseignantUEDTO> getEnseignantsByUE(Long ueId) {
        return enseignantUERepository.findByUeId(ueId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EnseignantUEDTO ajouterCompetence(EnseignantUEDTO dto) {
        if (dto.getIdEcole() == null || dto.getIdEcole().isBlank()) {
            throw new IllegalArgumentException("L'école de l'assignation est obligatoire.");
        }
        // Vérifier si la compétence existe déjà
        var existing = enseignantUERepository.findByEnseignantIdAndUeId(dto.getEnseignantId(), dto.getUeId());
        var owner = enseignantUERepository.findByUeIdAndIdEcole(dto.getUeId(), dto.getIdEcole());
        if (owner.isPresent() && !owner.get().getEnseignantId().equals(dto.getEnseignantId())) {
            throw new IllegalArgumentException("Cette UE est déjà assignée à un autre enseignant dans cette école.");
        }
        
        EnseignantUE entity;
        if (existing.isPresent() && dto.getIdEcole().equals(existing.get().getIdEcole())) {
            entity = existing.get();
            // Mettre à jour les compétences
            entity.setPeutCM(dto.getPeutCM());
            entity.setPeutTD(dto.getPeutTD());
            entity.setPeutTP(dto.getPeutTP());
            entity.setPreference(dto.getPreference());
            entity.setCommentaire(dto.getCommentaire());
        } else {
            entity = convertToEntity(dto);
        }
        
        entity = enseignantUERepository.save(entity);
        return convertToDTO(entity);
    }

    @Transactional
    public void supprimerCompetence(Long id) {
        enseignantUERepository.deleteById(id);
    }

    public List<EnseignantUEDTO> getEnseignantsDisponiblesPourUE(Long ueId, String typeCours) {
        List<EnseignantUE> enseignants;
        
        switch (typeCours.toUpperCase()) {
            case "CM":
                enseignants = enseignantUERepository.findByUeId(ueId).stream()
                        .filter(e -> Boolean.TRUE.equals(e.getPeutCM()))
                        .collect(Collectors.toList());
                break;
            case "TD":
                enseignants = enseignantUERepository.findByUeId(ueId).stream()
                        .filter(e -> Boolean.TRUE.equals(e.getPeutTD()))
                        .collect(Collectors.toList());
                break;
            case "TP":
                enseignants = enseignantUERepository.findByUeId(ueId).stream()
                        .filter(e -> Boolean.TRUE.equals(e.getPeutTP()))
                        .collect(Collectors.toList());
                break;
            default:
                enseignants = enseignantUERepository.findByUeId(ueId);
        }
        
        return enseignants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private EnseignantUEDTO convertToDTO(EnseignantUE entity) {
        EnseignantUEDTO dto = new EnseignantUEDTO();
        dto.setId(entity.getId());
        dto.setEnseignantId(entity.getEnseignantId());
        dto.setUeId(entity.getUeId());
        dto.setIdEcole(entity.getIdEcole());
        dto.setCodeUE(entity.getCodeUE());
        dto.setNomUE(entity.getNomUE());
        dto.setPeutCM(entity.getPeutCM());
        dto.setPeutTD(entity.getPeutTD());
        dto.setPeutTP(entity.getPeutTP());
        dto.setPreference(entity.getPreference());
        dto.setCommentaire(entity.getCommentaire());
        return dto;
    }

    private EnseignantUE convertToEntity(EnseignantUEDTO dto) {
        EnseignantUE entity = new EnseignantUE();
        entity.setId(dto.getId());
        entity.setEnseignantId(dto.getEnseignantId());
        entity.setUeId(dto.getUeId());
        entity.setIdEcole(dto.getIdEcole());
        entity.setCodeUE(dto.getCodeUE());
        entity.setNomUE(dto.getNomUE());
        entity.setPeutCM(dto.getPeutCM());
        entity.setPeutTD(dto.getPeutTD());
        entity.setPeutTP(dto.getPeutTP());
        entity.setPreference(dto.getPreference());
        entity.setCommentaire(dto.getCommentaire());
        return entity;
    }
}