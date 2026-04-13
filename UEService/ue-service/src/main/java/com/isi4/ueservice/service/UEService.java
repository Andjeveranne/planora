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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UEService {

    private final UERepository ueRepository;

    public UEDTO creer(UEDTO dto) {
        if (ueRepository.existsByCodeUE(dto.getCodeUE())) {
            throw new ValidationException("Une UE avec le code " + dto.getCodeUE() + " existe déjà");
        }
        UE ue = toEntity(dto);
        ue.setId(null);
        return toDTO(ueRepository.save(ue));
    }

    @Transactional(readOnly = true)
    public List<UEDTO> obtenirToutes() {
        return ueRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UEDTO obtenirParId(Long id) {
        return toDTO(ueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UE non trouvée avec l'ID: " + id)));
    }

    @Transactional(readOnly = true)
    public List<UEDTO> obtenirParFiliere(String idFiliere) {
        return ueRepository.findByIdFiliere(idFiliere).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UEDTO> obtenirParNiveau(String niveau) {
        return ueRepository.findByNiveau(niveau).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UEDTO> obtenirParFiliereEtNiveau(String idFiliere, String niveau) {
        return ueRepository.findByIdFiliereAndNiveau(idFiliere, niveau).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public UEDTO mettreAJour(Long id, UEDTO dto) {
        UE ue = ueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UE non trouvée avec l'ID: " + id));
        if (!ue.getCodeUE().equals(dto.getCodeUE()) && ueRepository.existsByCodeUE(dto.getCodeUE())) {
            throw new ValidationException("Une autre UE utilise déjà le code " + dto.getCodeUE());
        }
        ue.setCodeUE(dto.getCodeUE());
        ue.setNomUE(dto.getNomUE());
        ue.setDescription(dto.getDescription());
        ue.setNombreCredit(dto.getNombreCredit());
        ue.setNombreHeur(dto.getNombreHeur());
        ue.setHeuresCM(dto.getHeuresCM());
        ue.setHeuresTD(dto.getHeuresTD());
        ue.setHeuresTP(dto.getHeuresTP());
        ue.setNiveau(dto.getNiveau());
        ue.setIdFiliere(dto.getIdFiliere());
        ue.setNomFiliere(dto.getNomFiliere());
        return toDTO(ueRepository.save(ue));
    }

    public void supprimer(Long id) {
        if (!ueRepository.existsById(id)) {
            throw new ResourceNotFoundException("UE non trouvée avec l'ID: " + id);
        }
        ueRepository.deleteById(id);
    }

    private UE toEntity(UEDTO dto) {
        UE u = new UE();
        u.setIdUE(dto.getIdUE());
        u.setCodeUE(dto.getCodeUE());
        u.setNomUE(dto.getNomUE());
        u.setDescription(dto.getDescription());
        u.setNombreCredit(dto.getNombreCredit());
        u.setNombreHeur(dto.getNombreHeur());
        u.setHeuresCM(dto.getHeuresCM());
        u.setHeuresTD(dto.getHeuresTD());
        u.setHeuresTP(dto.getHeuresTP());
        u.setNiveau(dto.getNiveau());
        u.setIdFiliere(dto.getIdFiliere());
        u.setNomFiliere(dto.getNomFiliere());
        return u;
    }

    private UEDTO toDTO(UE u) {
        UEDTO dto = new UEDTO();
        dto.setId(u.getId());
        dto.setIdUE(u.getIdUE());
        dto.setCodeUE(u.getCodeUE());
        dto.setNomUE(u.getNomUE());
        dto.setDescription(u.getDescription());
        dto.setNombreCredit(u.getNombreCredit());
        dto.setNombreHeur(u.getNombreHeur());
        dto.setHeuresCM(u.getHeuresCM());
        dto.setHeuresTD(u.getHeuresTD());
        dto.setHeuresTP(u.getHeuresTP());
        dto.setNiveau(u.getNiveau());
        dto.setIdFiliere(u.getIdFiliere());
        dto.setNomFiliere(u.getNomFiliere());
        return dto;
    }
}
