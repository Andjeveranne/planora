package com.isi4.planningservice.service;

import com.isi4.planningservice.dto.UEEnseignantDTO;
import com.isi4.planningservice.entity.UEEnseignant;
import com.isi4.planningservice.repository.UEEnseignantRepository;
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
public class UEEnseignantService {

    private final UEEnseignantRepository ueEnseignantRepository;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public UEEnseignantDTO creer(UEEnseignantDTO dto) {
        if (ueEnseignantRepository.existsByUeIdAndEnseignantId(dto.getUeId(), dto.getEnseignantId())) {
            throw new IllegalArgumentException("Cet enseignant est déjà assigné à cette UE");
        }
        
        UEEnseignant ueEnseignant = new UEEnseignant();
        ueEnseignant.setUeId(dto.getUeId());
        ueEnseignant.setEnseignantId(dto.getEnseignantId());
        ueEnseignant.setPeutCM(dto.getPeutCM() != null ? dto.getPeutCM() : false);
        ueEnseignant.setPeutTD(dto.getPeutTD() != null ? dto.getPeutTD() : false);
        ueEnseignant.setPeutTP(dto.getPeutTP() != null ? dto.getPeutTP() : false);
        
        UEEnseignant saved = ueEnseignantRepository.save(ueEnseignant);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<UEEnseignantDTO> obtenirTous() {
        List<UEEnseignant> ueEnseignants = ueEnseignantRepository.findAll();
        return enrichirAvecDonnees(ueEnseignants);
    }

    @Transactional(readOnly = true)
    public List<UEEnseignantDTO> obtenirParUE(Long ueId) {
        List<UEEnseignant> ueEnseignants = ueEnseignantRepository.findByUeId(ueId);
        return enrichirAvecDonnees(ueEnseignants);
    }

    @Transactional(readOnly = true)
    public List<UEEnseignantDTO> obtenirParEnseignant(String enseignantId) {
        List<UEEnseignant> ueEnseignants = ueEnseignantRepository.findByEnseignantId(enseignantId);
        return enrichirAvecDonnees(ueEnseignants);
    }

    @Transactional
    public UEEnseignantDTO mettreAJour(Long id, UEEnseignantDTO dto) {
        UEEnseignant ueEnseignant = ueEnseignantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UE-Enseignant non trouvée avec l'ID: " + id));
        
        ueEnseignant.setPeutCM(dto.getPeutCM() != null ? dto.getPeutCM() : false);
        ueEnseignant.setPeutTD(dto.getPeutTD() != null ? dto.getPeutTD() : false);
        ueEnseignant.setPeutTP(dto.getPeutTP() != null ? dto.getPeutTP() : false);
        
        UEEnseignant saved = ueEnseignantRepository.save(ueEnseignant);
        return convertToDTO(saved);
    }

    @Transactional
    public void supprimer(Long id) {
        if (!ueEnseignantRepository.existsById(id)) {
            throw new IllegalArgumentException("UE-Enseignant non trouvée avec l'ID: " + id);
        }
        ueEnseignantRepository.deleteById(id);
    }

    private List<UEEnseignantDTO> enrichirAvecDonnees(List<UEEnseignant> ueEnseignants) {
        if (ueEnseignants.isEmpty()) {
            return List.of();
        }

        // Récupérer les données des UEs
        Map<String, Object> uesData = getUEsData();

        return ueEnseignants.stream()
                .map(ueEnseignant -> {
                    UEEnseignantDTO dto = convertToDTO(ueEnseignant);
                    
                    // Enrichir avec les données UE
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ueInfo = (Map<String, Object>) uesData.get(String.valueOf(ueEnseignant.getUeId()));
                    if (ueInfo != null) {
                        dto.setCodeUE((String) ueInfo.get("codeUE"));
                        dto.setNomUE((String) ueInfo.get("nomUE"));
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

    private UEEnseignantDTO convertToDTO(UEEnseignant ueEnseignant) {
        UEEnseignantDTO dto = new UEEnseignantDTO();
        dto.setId(ueEnseignant.getId());
        dto.setUeId(ueEnseignant.getUeId());
        dto.setEnseignantId(ueEnseignant.getEnseignantId());
        dto.setPeutCM(ueEnseignant.getPeutCM());
        dto.setPeutTD(ueEnseignant.getPeutTD());
        dto.setPeutTP(ueEnseignant.getPeutTP());
        return dto;
    }
}