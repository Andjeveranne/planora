package com.isi4.userservice.service;

import com.isi4.userservice.dto.UEDTO;
import com.isi4.userservice.exception.ResourceNotFoundException;
import com.isi4.userservice.exception.ValidationException;
import com.isi4.userservice.model.Enseignant;
import com.isi4.userservice.model.UE;
import com.isi4.userservice.model.Course;
import com.isi4.userservice.model.Classe;
import com.isi4.userservice.repository.EnseignantRepository;
import com.isi4.userservice.repository.UERepository;
import com.isi4.userservice.repository.CourseRepository;
import com.isi4.userservice.repository.ClasseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des Unités d'Enseignement (UE)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UEService {

    private final UERepository ueRepository;
    private final EnseignantRepository enseignantRepository;
    private final CourseRepository courseRepository;
    private final ClasseRepository classeRepository;

    /**
     * Récupère toutes les UEs actives
     */
    public List<UEDTO> getAllActiveUEs() {
        log.info("Récupération de toutes les UEs actives");
        List<UE> ues = ueRepository.findByActiveTrue();
        return ues.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les UEs avec pagination
     */
    public Page<UEDTO> getAllActiveUEs(Pageable pageable) {
        log.info("Récupération des UEs actives avec pagination");
        List<UE> ues = ueRepository.findByActiveTrue();
        List<UEDTO> ueDTOs = ues.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), ueDTOs.size());
        
        return new PageImpl<>(ueDTOs.subList(start, end), pageable, ueDTOs.size());
    }

    /**
     * Récupère une UE par son ID
     */
    public UEDTO getUEById(Long id) {
        log.info("Récupération de l'UE avec l'ID: {}", id);
        UE ue = ueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UE non trouvée avec l'ID: " + id));
        return convertToDTO(ue);
    }

    /**
     * Récupère une UE par son code
     */
    public UEDTO getUEByCode(String codeUE) {
        log.info("Récupération de l'UE avec le code: {}", codeUE);
        UE ue = ueRepository.findByCodeUE(codeUE)
                .orElseThrow(() -> new ResourceNotFoundException("UE non trouvée avec le code: " + codeUE));
        return convertToDTO(ue);
    }

    /**
     * Crée une nouvelle UE
     */
    @Transactional
    public UEDTO createUE(UEDTO ueDTO) {
        log.info("Création d'une nouvelle UE: {}", ueDTO.getCodeUE());
        
        // Validation
        validateUE(ueDTO);
        
        // Vérification de l'unicité du code
        if (ueRepository.existsByCodeUE(ueDTO.getCodeUE())) {
            throw new ValidationException("Une UE avec le code '" + ueDTO.getCodeUE() + "' existe déjà");
        }
        
        // Création de l'entité
        UE ue = convertToEntity(ueDTO);
        
        // Sauvegarde
        UE savedUE = ueRepository.save(ue);
        
        log.info("UE créée avec succès avec l'ID: {}", savedUE.getId());
        return convertToDTO(savedUE);
    }

    /**
     * Met à jour une UE existante
     */
    @Transactional
    public UEDTO updateUE(Long id, UEDTO ueDTO) {
        log.info("Mise à jour de l'UE avec l'ID: {}", id);
        
        UE existingUE = ueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UE non trouvée avec l'ID: " + id));
        
        // Validation
        validateUE(ueDTO);
        
        // Vérification de l'unicité du code (en excluant l'UE actuelle)
        if (ueRepository.existsByCodeUEAndIdNot(ueDTO.getCodeUE(), id)) {
            throw new ValidationException("Une autre UE avec le code '" + ueDTO.getCodeUE() + "' existe déjà");
        }
        
        // Mise à jour des champs
        updateUEFields(existingUE, ueDTO);
        
        // Sauvegarde
        UE updatedUE = ueRepository.save(existingUE);
        
        log.info("UE mise à jour avec succès");
        return convertToDTO(updatedUE);
    }

    /**
     * Supprime une UE (suppression logique)
     */
    @Transactional
    public void deleteUE(Long id) {
        log.info("Suppression de l'UE avec l'ID: {}", id);
        
        UE ue = ueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UE non trouvée avec l'ID: " + id));
        
        // Suppression logique
        ue.setActive(false);
        ueRepository.save(ue);
        
        log.info("UE supprimée avec succès (suppression logique)");
    }

    /**
     * Restaure une UE supprimée
     */
    @Transactional
    public UEDTO restoreUE(Long id) {
        log.info("Restauration de l'UE avec l'ID: {}", id);
        
        UE ue = ueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UE non trouvée avec l'ID: " + id));
        
        ue.setActive(true);
        UE restoredUE = ueRepository.save(ue);
        
        log.info("UE restaurée avec succès");
        return convertToDTO(restoredUE);
    }

    /**
     * Recherche des UEs
     */
    public List<UEDTO> searchUEs(String searchTerm) {
        log.info("Recherche d'UEs avec le terme: {}", searchTerm);
        List<UE> ues = ueRepository.searchUEs(searchTerm);
        return ues.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les UEs par semestre
     */
    public List<UEDTO> getUEsBySemestre(String semestre) {
        log.info("Récupération des UEs pour le semestre: {}", semestre);
        List<UE> ues = ueRepository.findBySemestreAndActiveTrue(semestre);
        return ues.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les UEs par niveau
     */
    public List<UEDTO> getUEsByNiveau(String niveau) {
        log.info("Récupération des UEs pour le niveau: {}", niveau);
        List<UE> ues = ueRepository.findByNiveauAndActiveTrue(niveau);
        return ues.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les UEs d'un enseignant
     */
    public List<UEDTO> getUEsByEnseignant(String enseignantId) {
        log.info("Récupération des UEs pour l'enseignant: {}", enseignantId);
        List<UE> ues = ueRepository.findByEnseignantId(enseignantId);
        return ues.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Assigne un enseignant à une UE
     */
    @Transactional
    public UEDTO assignEnseignantToUE(Long ueId, String enseignantId) {
        log.info("Assignation de l'enseignant {} à l'UE {}", enseignantId, ueId);
        
        try {
            UE ue = ueRepository.findById(ueId)
                    .orElseThrow(() -> new ResourceNotFoundException("UE non trouvée avec l'ID: " + ueId));
            
            Enseignant enseignant = enseignantRepository.findById(enseignantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Enseignant non trouvé avec l'ID: " + enseignantId));
            
            // Vérifier si l'enseignant est déjà assigné à cette UE
            if (ue.getEnseignants().contains(enseignant)) {
                log.info("L'enseignant {} est déjà assigné à l'UE {}", enseignantId, ueId);
                return convertToDTO(ue);
            }
            
            // Assigner l'enseignant à l'UE de manière sécurisée
            // Ajouter la relation côté UE
            ue.getEnseignants().add(enseignant);
            
            // Ajouter la relation côté Enseignant
            enseignant.getUes().add(ue);
            
            // Sauvegarder les deux entités
            enseignantRepository.save(enseignant);
            UE savedUE = ueRepository.save(ue);
            
            // Créer automatiquement des cours pour toutes les classes du même niveau
            try {
                createCoursesForUEAndEnseignant(savedUE, enseignant);
                log.info("Enseignant assigné avec succès à l'UE et cours créés");
            } catch (Exception e) {
                log.warn("Erreur lors de la création automatique des cours: {}", e.getMessage());
                log.info("Enseignant assigné avec succès à l'UE (sans création automatique des cours)");
            }
            
            return convertToDTO(savedUE);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'assignation de l'enseignant {} à l'UE {}: {}", 
                    enseignantId, ueId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Assigne un enseignant à une UE (version simplifiée sans création automatique de cours)
     */
    @Transactional
    public UEDTO assignEnseignantToUESimple(Long ueId, String enseignantId) {
        log.info("Assignation simple de l'enseignant {} à l'UE {}", enseignantId, ueId);
        
        try {
            UE ue = ueRepository.findById(ueId)
                    .orElseThrow(() -> new ResourceNotFoundException("UE non trouvée avec l'ID: " + ueId));
            
            Enseignant enseignant = enseignantRepository.findById(enseignantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Enseignant non trouvé avec l'ID: " + enseignantId));
            
            // Vérifier si l'enseignant est déjà assigné à cette UE
            if (ue.getEnseignants().contains(enseignant)) {
                log.info("L'enseignant {} est déjà assigné à l'UE {}", enseignantId, ueId);
                return convertToDTO(ue);
            }
            
            // Assigner l'enseignant à l'UE de manière sécurisée
            try {
                // Ajouter la relation côté UE
                ue.getEnseignants().add(enseignant);
                
                // Ajouter la relation côté Enseignant
                enseignant.getUes().add(ue);
                
                // Sauvegarder les deux entités
                enseignantRepository.save(enseignant);
                UE savedUE = ueRepository.save(ue);
                
                log.info("Enseignant assigné avec succès à l'UE (sans création automatique de cours)");
                return convertToDTO(savedUE);
                
            } catch (Exception saveException) {
                log.error("Erreur lors de la sauvegarde de l'assignation: {}", saveException.getMessage());
                throw new RuntimeException("Impossible d'assigner l'enseignant à l'UE: " + saveException.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de l'assignation simple de l'enseignant {} à l'UE {}: {}", 
                    enseignantId, ueId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retire un enseignant d'une UE
     */
    @Transactional
    public UEDTO removeEnseignantFromUE(Long ueId, String enseignantId) {
        log.info("Retrait de l'enseignant {} de l'UE {}", enseignantId, ueId);
        
        UE ue = ueRepository.findById(ueId)
                .orElseThrow(() -> new ResourceNotFoundException("UE non trouvée avec l'ID: " + ueId));
        
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant non trouvé avec l'ID: " + enseignantId));
        
        // Retirer l'enseignant de l'UE
        ue.removeEnseignant(enseignant);
        UE savedUE = ueRepository.save(ue);
        
        // Supprimer les cours associés à cet enseignant et cette UE
        removeCoursesForUEAndEnseignant(savedUE, enseignant);
        
        log.info("Enseignant retiré avec succès de l'UE et cours supprimés");
        return convertToDTO(savedUE);
    }

    /**
     * Crée automatiquement des cours pour une UE et un enseignant
     */
    private void createCoursesForUEAndEnseignant(UE ue, Enseignant enseignant) {
        log.info("Création automatique des cours pour UE {} et enseignant {}", ue.getCodeUE(), enseignant.getIdUser());
        
        try {
            // Vérifier si la table classes existe et récupérer les classes du même niveau que l'UE
            List<Classe> classes = classeRepository.findByNiveauAndActiveTrue(ue.getNiveau());
            
            if (classes.isEmpty()) {
                log.warn("Aucune classe trouvée pour le niveau {} de l'UE {}", ue.getNiveau(), ue.getCodeUE());
                return;
            }
            
            for (Classe classe : classes) {
                try {
                    // Créer des cours pour chaque type (CM, TD, TP) si l'UE a des heures pour ce type
                    if (ue.getHeuresCM() != null && ue.getHeuresCM() > 0) {
                        createCourseIfNotExists(ue, enseignant, classe, "CM", ue.getHeuresCM());
                    }
                    if (ue.getHeuresTD() != null && ue.getHeuresTD() > 0) {
                        createCourseIfNotExists(ue, enseignant, classe, "TD", ue.getHeuresTD());
                    }
                    if (ue.getHeuresTP() != null && ue.getHeuresTP() > 0) {
                        createCourseIfNotExists(ue, enseignant, classe, "TP", ue.getHeuresTP());
                    }
                } catch (Exception e) {
                    log.warn("Erreur lors de la création du cours pour la classe {}: {}", 
                            classe.getNomClasse(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de la création automatique des cours: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Crée un cours s'il n'existe pas déjà
     */
    private void createCourseIfNotExists(UE ue, Enseignant enseignant, Classe classe, String typeCours, Integer nombreHeures) {
        try {
            String codeCourse = String.format("%s-%s-%s", ue.getCodeUE(), typeCours, classe.getNomClasse());
            
            // Vérifier si le cours existe déjà
            if (courseRepository.existsByCodeCourse(codeCourse)) {
                log.debug("Cours déjà existant: {}", codeCourse);
                return;
            }
            
            Course course = new Course();
            course.setCodeCourse(codeCourse);
            course.setTypeCours(typeCours);
            course.setNombreHeures(nombreHeures);
            course.setSemestre(ue.getSemestre());
            course.setDescription(String.format("%s de %s pour la classe %s", 
                    getTypeCoursDescription(typeCours), ue.getNomUE(), classe.getNomClasse()));
            course.setUe(ue);
            course.setEnseignant(enseignant);
            course.setClasse(classe);
            course.setActive(true);
            course.setCreatedAt(LocalDateTime.now());
            course.setUpdatedAt(LocalDateTime.now());
            
            Course savedCourse = courseRepository.save(course);
            log.info("Cours créé avec succès: {} (ID: {})", codeCourse, savedCourse.getId());
            
        } catch (Exception e) {
            log.error("Erreur lors de la création du cours {}-{}-{}: {}", 
                    ue.getCodeUE(), typeCours, classe.getNomClasse(), e.getMessage());
            throw e;
        }
    }

    /**
     * Supprime les cours associés à une UE et un enseignant
     */
    private void removeCoursesForUEAndEnseignant(UE ue, Enseignant enseignant) {
        log.info("Suppression des cours pour UE {} et enseignant {}", ue.getCodeUE(), enseignant.getIdUser());
        
        // Trouver tous les cours de cet enseignant pour cette UE
        List<Course> courses = courseRepository.findByUeAndEnseignantAndActiveTrue(ue, enseignant);
        
        for (Course course : courses) {
            course.setActive(false);
            course.setUpdatedAt(LocalDateTime.now());
            courseRepository.save(course);
            log.info("Cours désactivé: {}", course.getCodeCourse());
        }
    }

    /**
     * Retourne la description du type de cours
     */
    private String getTypeCoursDescription(String typeCours) {
        switch (typeCours) {
            case "CM": return "Cours magistral";
            case "TD": return "Travaux dirigés";
            case "TP": return "Travaux pratiques";
            default: return "Cours";
        }
    }

    /**
     * Récupère les statistiques des UEs
     */
    public Map<String, Object> getUEStatistics() {
        log.info("Récupération des statistiques des UEs");
        
        try {
            Object[] stats = ueRepository.getUEStatistics();
            List<Object[]> statsBySemestre = ueRepository.countUEsBySemestre();
            List<Object[]> statsByNiveau = ueRepository.countUEsByNiveau();
            
            // Handle null values when no UEs exist
            Long totalUEs = stats != null && stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
            Long totalCredits = stats != null && stats[1] != null ? ((Number) stats[1]).longValue() : 0L;
            Double avgCredits = stats != null && stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0;
            Long totalHeures = stats != null && stats[3] != null ? ((Number) stats[3]).longValue() : 0L;
            
            return Map.of(
                "totalUEs", totalUEs,
                "totalCredits", totalCredits,
                "avgCredits", avgCredits,
                "totalHeures", totalHeures,
                "statsBySemestre", statsBySemestre != null ? statsBySemestre : List.of(),
                "statsByNiveau", statsByNiveau != null ? statsByNiveau : List.of()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques UE", e);
            // Return default empty statistics
            return Map.of(
                "totalUEs", 0L,
                "totalCredits", 0L,
                "avgCredits", 0.0,
                "totalHeures", 0L,
                "statsBySemestre", List.of(),
                "statsByNiveau", List.of()
            );
        }
    }

    // Méthodes privées de validation et conversion

    private void validateUE(UEDTO ueDTO) {
        if (ueDTO.getCodeUE() == null || ueDTO.getCodeUE().trim().isEmpty()) {
            throw new ValidationException("Le code UE est obligatoire");
        }
        
        if (ueDTO.getNomUE() == null || ueDTO.getNomUE().trim().isEmpty()) {
            throw new ValidationException("Le nom de l'UE est obligatoire");
        }
        
        if (ueDTO.getHeuresCM() == null || ueDTO.getHeuresCM() < 0) {
            throw new ValidationException("Le nombre d'heures CM doit être positif ou nul");
        }
        
        if (ueDTO.getHeuresTD() == null || ueDTO.getHeuresTD() < 0) {
            throw new ValidationException("Le nombre d'heures TD doit être positif ou nul");
        }
        
        if (ueDTO.getHeuresTP() == null || ueDTO.getHeuresTP() < 0) {
            throw new ValidationException("Le nombre d'heures TP doit être positif ou nul");
        }
        
        if (ueDTO.getCreditsECTS() == null || ueDTO.getCreditsECTS() <= 0) {
            throw new ValidationException("Le nombre de crédits ECTS doit être positif");
        }
        
        if (ueDTO.getSemestre() == null || ueDTO.getSemestre().trim().isEmpty()) {
            throw new ValidationException("Le semestre est obligatoire");
        }
        
        if (ueDTO.getNiveau() == null || ueDTO.getNiveau().trim().isEmpty()) {
            throw new ValidationException("Le niveau est obligatoire");
        }
        
        // Validation du total d'heures
        if (ueDTO.getTotalHeures() == 0) {
            throw new ValidationException("L'UE doit avoir au moins une heure de cours (CM, TD ou TP)");
        }
    }

    private void updateUEFields(UE ue, UEDTO ueDTO) {
        ue.setCodeUE(ueDTO.getCodeUE());
        ue.setNomUE(ueDTO.getNomUE());
        ue.setDescription(ueDTO.getDescription());
        ue.setHeuresCM(ueDTO.getHeuresCM());
        ue.setHeuresTD(ueDTO.getHeuresTD());
        ue.setHeuresTP(ueDTO.getHeuresTP());
        ue.setCreditsECTS(ueDTO.getCreditsECTS());
        ue.setSemestre(ueDTO.getSemestre());
        ue.setNiveau(ueDTO.getNiveau());
        if (ueDTO.getActive() != null) {
            ue.setActive(ueDTO.getActive());
        }
    }

    private UE convertToEntity(UEDTO dto) {
        UE ue = new UE();
        ue.setCodeUE(dto.getCodeUE());
        ue.setNomUE(dto.getNomUE());
        ue.setDescription(dto.getDescription());
        ue.setHeuresCM(dto.getHeuresCM());
        ue.setHeuresTD(dto.getHeuresTD());
        ue.setHeuresTP(dto.getHeuresTP());
        ue.setCreditsECTS(dto.getCreditsECTS());
        ue.setSemestre(dto.getSemestre());
        ue.setNiveau(dto.getNiveau());
        ue.setActive(dto.getActive() != null ? dto.getActive() : true);
        return ue;
    }

    private UEDTO convertToDTO(UE ue) {
        UEDTO dto = new UEDTO();
        dto.setId(ue.getId());
        dto.setCodeUE(ue.getCodeUE());
        dto.setNomUE(ue.getNomUE());
        dto.setDescription(ue.getDescription());
        dto.setHeuresCM(ue.getHeuresCM());
        dto.setHeuresTD(ue.getHeuresTD());
        dto.setHeuresTP(ue.getHeuresTP());
        dto.setCreditsECTS(ue.getCreditsECTS());
        dto.setSemestre(ue.getSemestre());
        dto.setNiveau(ue.getNiveau());
        dto.setActive(ue.getActive());
        dto.setCreatedAt(ue.getCreatedAt());
        dto.setUpdatedAt(ue.getUpdatedAt());
        
        // Relations
        if (ue.getEnseignants() != null) {
            dto.setEnseignantIds(ue.getEnseignants().stream()
                    .map(e -> e.getIdUser())
                    .collect(Collectors.toList()));
            dto.setEnseignantNames(ue.getEnseignants().stream()
                    .map(e -> e.getPrenomUser() + " " + e.getNomUser())
                    .collect(Collectors.toList()));
            dto.setNombreEnseignants(ue.getEnseignants().size());
        }
        
        if (ue.getEmploisDuTemps() != null) {
            dto.setNombreEmploisDuTemps(ue.getEmploisDuTemps().size());
        }
        
        return dto;
    }
}