package com.isi4.planningservice.service;

import com.isi4.planningservice.dto.UEClasseDTO;
import com.isi4.planningservice.entity.UEClasse;
import com.isi4.planningservice.repository.UEClasseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UEClasseService {

    private final UEClasseRepository ueClasseRepository;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public UEClasseDTO creer(UEClasseDTO dto) {
        if (ueClasseRepository.existsByUeIdAndClasseId(dto.getUeId(), dto.getClasseId())) {
            throw new IllegalArgumentException("Cette UE est déjà assignée à cette classe");
        }
        
        UEClasse ueClasse = new UEClasse();
        ueClasse.setUeId(dto.getUeId());
        ueClasse.setClasseId(dto.getClasseId());
        ueClasse.setHeuresCM(dto.getHeuresCM() != null ? dto.getHeuresCM() : 0);
        ueClasse.setHeuresTD(dto.getHeuresTD() != null ? dto.getHeuresTD() : 0);
        ueClasse.setHeuresTP(dto.getHeuresTP() != null ? dto.getHeuresTP() : 0);
        
        UEClasse saved = ueClasseRepository.save(ueClasse);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<UEClasseDTO> obtenirTous() {
        List<UEClasse> ueClasses = ueClasseRepository.findAll();
        return enrichirAvecDonnees(ueClasses);
    }

    @Transactional(readOnly = true)
    public List<UEClasseDTO> obtenirParUE(Long ueId) {
        List<UEClasse> ueClasses = ueClasseRepository.findByUeId(ueId);
        return enrichirAvecDonnees(ueClasses);
    }

    @Transactional(readOnly = true)
    public List<UEClasseDTO> obtenirParClasse(String classeId) {
        List<UEClasse> ueClasses = ueClasseRepository.findByClasseId(classeId);
        return enrichirAvecDonnees(ueClasses);
    }

    @Transactional
    public UEClasseDTO mettreAJour(Long id, UEClasseDTO dto) {
        UEClasse ueClasse = ueClasseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UE-Classe non trouvée avec l'ID: " + id));
        
        ueClasse.setHeuresCM(dto.getHeuresCM() != null ? dto.getHeuresCM() : 0);
        ueClasse.setHeuresTD(dto.getHeuresTD() != null ? dto.getHeuresTD() : 0);
        ueClasse.setHeuresTP(dto.getHeuresTP() != null ? dto.getHeuresTP() : 0);
        
        UEClasse saved = ueClasseRepository.save(ueClasse);
        return convertToDTO(saved);
    }

    @Transactional
    public void supprimer(Long id) {
        if (!ueClasseRepository.existsById(id)) {
            throw new IllegalArgumentException("UE-Classe non trouvée avec l'ID: " + id);
        }
        ueClasseRepository.deleteById(id);
    }

    private List<UEClasseDTO> enrichirAvecDonnees(List<UEClasse> ueClasses) {
        if (ueClasses.isEmpty()) {
            return List.of();
        }

        // Récupérer les données des UEs et Classes
        Map<String, Object> uesData = getUEsData();
        Map<String, Object> classesData = getClassesData();

        return ueClasses.stream()
                .map(ueClasse -> {
                    UEClasseDTO dto = convertToDTO(ueClasse);
                    
                    // Enrichir avec les données UE
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ueInfo = (Map<String, Object>) uesData.get(String.valueOf(ueClasse.getUeId()));
                    if (ueInfo != null) {
                        dto.setCodeUE((String) ueInfo.get("codeUE"));
                        dto.setNomUE((String) ueInfo.get("nomUE"));
                    }
                    
                    // Enrichir avec les données Classe
                    @SuppressWarnings("unchecked")
                    Map<String, Object> classeInfo = (Map<String, Object>) classesData.get(ueClasse.getClasseId());
                    if (classeInfo != null) {
                        dto.setNomClasse((String) classeInfo.get("nomClasse"));
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getUEsData() {
        try {
            WebClient webClient = webClientBuilder.build();
            List<Map<String, Object>> ues = webClient.get()
                    .uri("http://ue-service/api/ues")
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            
            return ues.stream()
                    .collect(Collectors.toMap(
                            ue -> String.valueOf(((Number) ue.get("id")).longValue()),
                            ue -> ue
                    ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des UEs", e);
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getClassesData() {
        try {
            WebClient webClient = webClientBuilder.build();
            List<Map<String, Object>> classes = webClient.get()
                    .uri("http://classes-service/api/classes")
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            
            return classes.stream()
                    .collect(Collectors.toMap(
                            classe -> (String) classe.get("idClasse"),
                            classe -> classe
                    ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des classes", e);
            return Map.of();
        }
    }

    private UEClasseDTO convertToDTO(UEClasse ueClasse) {
        UEClasseDTO dto = new UEClasseDTO();
        dto.setId(ueClasse.getId());
        dto.setUeId(ueClasse.getUeId());
        dto.setClasseId(ueClasse.getClasseId());
        dto.setHeuresCM(ueClasse.getHeuresCM());
        dto.setHeuresTD(ueClasse.getHeuresTD());
        dto.setHeuresTP(ueClasse.getHeuresTP());
        return dto;
    }
}