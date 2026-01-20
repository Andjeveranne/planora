package com.isi4.userservice.service;

import com.isi4.userservice.dto.ClasseDTO;
import com.isi4.userservice.model.Classe;
import com.isi4.userservice.repository.ClasseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des Classes
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClasseService {

    private final ClasseRepository classeRepository;

    /**
     * Récupère toutes les classes actives
     */
    @Transactional(readOnly = true)
    public List<ClasseDTO> getAllClasses() {
        log.info("Récupération de toutes les classes actives");
        return classeRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une classe par son ID
     */
    @Transactional(readOnly = true)
    public Optional<ClasseDTO> getClasseById(Long id) {
        if (id == null) {
            log.warn("ID de classe null fourni");
            return Optional.empty();
        }
        log.info("Récupération de la classe avec l'ID: {}", id);
        return classeRepository.findById(id)
                .filter(classe -> Boolean.TRUE.equals(classe.getActive()))
                .map(this::convertToDTO);
    }

    /**
     * Récupère une classe par son nom
     */
    @Transactional(readOnly = true)
    public Optional<ClasseDTO> getClasseByNom(String nomClasse) {
        log.info("Récupération de la classe avec le nom: {}", nomClasse);
        return classeRepository.findByNomClasse(nomClasse)
                .filter(classe -> classe.getActive())
                .map(this::convertToDTO);
    }

    /**
     * Récupère les classes par niveau
     */
    @Transactional(readOnly = true)
    public List<ClasseDTO> getClassesByNiveau(String niveau) {
        log.info("Récupération des classes pour le niveau: {}", niveau);
        return classeRepository.findByNiveauAndActiveTrue(niveau)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les classes par filière
     */
    @Transactional(readOnly = true)
    public List<ClasseDTO> getClassesByFiliere(String filiere) {
        log.info("Récupération des classes pour la filière: {}", filiere);
        return classeRepository.findByFiliereAndActiveTrue(filiere)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Recherche des classes
     */
    @Transactional(readOnly = true)
    public List<ClasseDTO> searchClasses(String searchTerm) {
        log.info("Recherche de classes avec le terme: {}", searchTerm);
        return classeRepository.searchClasses(searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crée une nouvelle classe
     */
    public ClasseDTO createClasse(ClasseDTO classeDTO) {
        log.info("Création d'une nouvelle classe: {}", classeDTO.getNomClasse());
        
        // Vérifier si le nom de classe existe déjà
        if (classeRepository.existsByNomClasse(classeDTO.getNomClasse())) {
            throw new IllegalArgumentException("Une classe avec ce nom existe déjà: " + classeDTO.getNomClasse());
        }

        Classe classe = convertToEntity(classeDTO);
        classe.setActive(true);
        classe.setCreatedAt(LocalDateTime.now());
        classe.setUpdatedAt(LocalDateTime.now());

        Classe savedClasse = classeRepository.save(classe);
        log.info("Classe créée avec succès avec l'ID: {}", savedClasse.getId());
        
        return convertToDTO(savedClasse);
    }

    /**
     * Met à jour une classe existante
     */
    public ClasseDTO updateClasse(Long id, ClasseDTO classeDTO) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de la classe ne peut pas être null");
        }
        if (classeDTO == null) {
            throw new IllegalArgumentException("Les données de la classe ne peuvent pas être null");
        }
        
        log.info("Mise à jour de la classe avec l'ID: {}", id);
        
        Classe existingClasse = classeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Classe non trouvée avec l'ID: " + id));

        // Vérifier si le nom de classe existe déjà (excluant la classe actuelle)
        if (classeDTO.getNomClasse() != null && 
            classeRepository.existsByNomClasseAndIdNot(classeDTO.getNomClasse(), id)) {
            throw new IllegalArgumentException("Une classe avec ce nom existe déjà: " + classeDTO.getNomClasse());
        }

        // Mettre à jour les champs
        existingClasse.setNomClasse(classeDTO.getNomClasse());
        existingClasse.setNiveau(classeDTO.getNiveau());
        existingClasse.setFiliere(classeDTO.getFiliere());
        existingClasse.setAnneeAcademique(classeDTO.getAnneeAcademique());
        existingClasse.setNombreEtudiants(classeDTO.getNombreEtudiants());
        existingClasse.setDescription(classeDTO.getDescription());
        existingClasse.setUpdatedAt(LocalDateTime.now());

        Classe updatedClasse = classeRepository.save(existingClasse);
        log.info("Classe mise à jour avec succès");
        
        return convertToDTO(updatedClasse);
    }

    /**
     * Supprime une classe (suppression logique)
     */
    public void deleteClasse(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de la classe ne peut pas être null");
        }
        
        log.info("Suppression de la classe avec l'ID: {}", id);
        
        Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Classe non trouvée avec l'ID: " + id));

        classe.setActive(false);
        classe.setUpdatedAt(LocalDateTime.now());
        classeRepository.save(classe);
        
        log.info("Classe supprimée avec succès");
    }

    /**
     * Récupère les statistiques des classes
     */
    @Transactional(readOnly = true)
    public ClasseStatsDTO getClasseStats() {
        log.info("Récupération des statistiques des classes");
        
        List<Classe> allClasses = classeRepository.findByActiveTrue();
        
        ClasseStatsDTO stats = new ClasseStatsDTO();
        stats.setTotalClasses(allClasses.size());
        stats.setTotalEtudiants(allClasses.stream().mapToInt(Classe::getNombreEtudiants).sum());
        
        // Statistiques par niveau
        List<Object[]> statsByNiveau = classeRepository.countClassesByNiveau();
        stats.setStatsByNiveau(statsByNiveau.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).intValue()
                )));
        
        // Statistiques par filière
        List<Object[]> statsByFiliere = classeRepository.countClassesByFiliere();
        stats.setStatsByFiliere(statsByFiliere.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).intValue()
                )));
        
        return stats;
    }

    /**
     * Convertit une entité Classe en DTO
     */
    private ClasseDTO convertToDTO(Classe classe) {
        ClasseDTO dto = new ClasseDTO();
        dto.setId(classe.getId());
        dto.setNomClasse(classe.getNomClasse());
        dto.setNiveau(classe.getNiveau());
        dto.setFiliere(classe.getFiliere());
        dto.setAnneeAcademique(classe.getAnneeAcademique());
        dto.setNombreEtudiants(classe.getNombreEtudiants());
        dto.setDescription(classe.getDescription());
        dto.setActive(classe.getActive());
        dto.setCreatedAt(classe.getCreatedAt());
        dto.setUpdatedAt(classe.getUpdatedAt());
        
        // Informations calculées
        dto.setNombreCours(classe.getNombreCours());
        dto.setTotalHeuresCours(classe.getTotalHeuresCours());
        dto.setTotalCreditsECTS(classe.getTotalCreditsECTS());
        
        // IDs des cours
        if (classe.getCourses() != null) {
            dto.setCourseIds(classe.getCourses().stream()
                    .map(course -> course.getId())
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }

    /**
     * Convertit un DTO en entité Classe
     */
    private Classe convertToEntity(ClasseDTO dto) {
        Classe classe = new Classe();
        classe.setNomClasse(dto.getNomClasse());
        classe.setNiveau(dto.getNiveau());
        classe.setFiliere(dto.getFiliere());
        classe.setAnneeAcademique(dto.getAnneeAcademique());
        classe.setNombreEtudiants(dto.getNombreEtudiants());
        classe.setDescription(dto.getDescription());
        classe.setActive(dto.getActive() != null ? dto.getActive() : true);
        
        return classe;
    }

    /**
     * DTO pour les statistiques des classes
     */
    @lombok.Data
    public static class ClasseStatsDTO {
        private Integer totalClasses;
        private Integer totalEtudiants;
        private java.util.Map<String, Integer> statsByNiveau;
        private java.util.Map<String, Integer> statsByFiliere;
    }
}