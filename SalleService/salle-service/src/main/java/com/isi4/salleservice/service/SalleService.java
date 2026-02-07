package com.isi4.salleservice.service;

import com.isi4.salleservice.dto.SalleDTO;
import com.isi4.salleservice.entity.Salle;
import com.isi4.salleservice.enums.StatutSalle;
import com.isi4.salleservice.enums.TypeSalle;
import com.isi4.salleservice.exception.ResourceNotFoundException;
import com.isi4.salleservice.exception.ValidationException;
import com.isi4.salleservice.repository.SalleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalleService {
    
    private final SalleRepository salleRepository;
    
    // Créer une nouvelle salle
    public SalleDTO creerSalle(SalleDTO salleDTO) {
        // Vérifier si une salle avec le même nom existe déjà dans cette école
        if (salleRepository.existsByNomSalleAndIdEcole(salleDTO.getNomSalle(), salleDTO.getIdEcole())) {
            throw new ValidationException("Une salle avec ce nom existe déjà dans cette école");
        }
        
        Salle salle = convertirDTOVersEntity(salleDTO);
        // L'ID sera généré automatiquement par JPA
        salle.setIdSalle(null);
        Salle salleSauvegardee = salleRepository.save(salle);
        return convertirEntityVersDTO(salleSauvegardee);
    }
    
    // Obtenir toutes les salles
    @Transactional(readOnly = true)
    public List<SalleDTO> obtenirToutesLesSalles() {
        return salleRepository.findAll().stream()
                .map(this::convertirEntityVersDTO)
                .collect(Collectors.toList());
    }
    
    // Obtenir une salle par ID
    @Transactional(readOnly = true)
    public SalleDTO obtenirSalleParId(String idSalle) {
        Salle salle = salleRepository.findById(idSalle)
                .orElseThrow(() -> new ResourceNotFoundException("Salle non trouvée avec l'ID: " + idSalle));
        return convertirEntityVersDTO(salle);
    }
    
    // Obtenir les salles par école
    @Transactional(readOnly = true)
    public List<SalleDTO> obtenirSallesParEcole(String idEcole) {
        return salleRepository.findByIdEcole(idEcole).stream()
                .map(this::convertirEntityVersDTO)
                .collect(Collectors.toList());
    }
    
    // Obtenir les salles par type
    @Transactional(readOnly = true)
    public List<SalleDTO> obtenirSallesParType(TypeSalle type) {
        return salleRepository.findByType(type).stream()
                .map(this::convertirEntityVersDTO)
                .collect(Collectors.toList());
    }
    
    // Obtenir les salles par statut
    @Transactional(readOnly = true)
    public List<SalleDTO> obtenirSallesParStatut(StatutSalle statut) {
        return salleRepository.findByStatut(statut).stream()
                .map(this::convertirEntityVersDTO)
                .collect(Collectors.toList());
    }
    
    // Obtenir les salles disponibles avec capacité minimale
    @Transactional(readOnly = true)
    public List<SalleDTO> obtenirSallesDisponibles(String idEcole, Integer capaciteMin) {
        return salleRepository.findSallesDisponibles(idEcole, capaciteMin).stream()
                .map(this::convertirEntityVersDTO)
                .collect(Collectors.toList());
    }
    
    // Mettre à jour une salle
    public SalleDTO mettreAJourSalle(String idSalle, SalleDTO salleDTO) {
        Salle salleExistante = salleRepository.findById(idSalle)
                .orElseThrow(() -> new ResourceNotFoundException("Salle non trouvée avec l'ID: " + idSalle));
        
        // Vérifier si le nouveau nom n'existe pas déjà (sauf pour la salle actuelle)
        if (!salleExistante.getNomSalle().equals(salleDTO.getNomSalle()) &&
            salleRepository.existsByNomSalleAndIdEcole(salleDTO.getNomSalle(), salleDTO.getIdEcole())) {
            throw new ValidationException("Une salle avec ce nom existe déjà dans cette école");
        }
        
        // Mettre à jour les champs
        salleExistante.setNomSalle(salleDTO.getNomSalle());
        salleExistante.setType(salleDTO.getType());
        salleExistante.setLocalisation(salleDTO.getLocalisation());
        salleExistante.setCapacite(salleDTO.getCapacite());
        salleExistante.setCapaciteExam(salleDTO.getCapaciteExam());
        salleExistante.setIdEcole(salleDTO.getIdEcole());
        salleExistante.setStatut(salleDTO.getStatut());
        
        Salle salleMiseAJour = salleRepository.save(salleExistante);
        return convertirEntityVersDTO(salleMiseAJour);
    }
    
    // Changer le statut d'une salle
    public SalleDTO changerStatutSalle(String idSalle, StatutSalle nouveauStatut) {
        Salle salle = salleRepository.findById(idSalle)
                .orElseThrow(() -> new ResourceNotFoundException("Salle non trouvée avec l'ID: " + idSalle));
        
        salle.setStatut(nouveauStatut);
        Salle salleMiseAJour = salleRepository.save(salle);
        return convertirEntityVersDTO(salleMiseAJour);
    }
    
    // Supprimer une salle
    public void supprimerSalle(String idSalle) {
        if (!salleRepository.existsById(idSalle)) {
            throw new ResourceNotFoundException("Salle non trouvée avec l'ID: " + idSalle);
        }
        salleRepository.deleteById(idSalle);
    }
    
    // Méthodes utilitaires
    private Salle convertirDTOVersEntity(SalleDTO dto) {
        Salle salle = new Salle();
        salle.setIdSalle(dto.getIdSalle());
        salle.setNomSalle(dto.getNomSalle());
        salle.setType(dto.getType());
        salle.setLocalisation(dto.getLocalisation());
        salle.setCapacite(dto.getCapacite());
        salle.setCapaciteExam(dto.getCapaciteExam());
        salle.setIdEcole(dto.getIdEcole());
        salle.setStatut(dto.getStatut());
        return salle;
    }
    
    private SalleDTO convertirEntityVersDTO(Salle salle) {
        SalleDTO dto = new SalleDTO();
        dto.setIdSalle(salle.getIdSalle());
        dto.setNomSalle(salle.getNomSalle());
        dto.setType(salle.getType());
        dto.setLocalisation(salle.getLocalisation());
        dto.setCapacite(salle.getCapacite());
        dto.setCapaciteExam(salle.getCapaciteExam());
        dto.setIdEcole(salle.getIdEcole());
        dto.setStatut(salle.getStatut());
        return dto;
    }
}