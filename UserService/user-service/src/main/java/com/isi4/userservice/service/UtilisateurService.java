package com.isi4.userservice.service;

import com.isi4.userservice.dto.EnseignantDTO;
import com.isi4.userservice.dto.UtilisateurDTO;
import com.isi4.userservice.dto.UtilisateurResponseDTO;
import com.isi4.userservice.model.Administrateur;
import com.isi4.userservice.model.Enseignant;
import com.isi4.userservice.model.Utilisateur;
import com.isi4.userservice.repository.AdministrateurRepository;
import com.isi4.userservice.repository.EnseignantRepository;
import com.isi4.userservice.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurService {
    
    private final UtilisateurRepository utilisateurRepository;
    private final EnseignantRepository enseignantRepository;
    private final AdministrateurRepository administrateurRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public UtilisateurResponseDTO creerUtilisateur(UtilisateurDTO dto) {
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }
        
        Utilisateur utilisateur;
        
        if ("ENSEIGNANT".equalsIgnoreCase(dto.getRole())) {
            Enseignant enseignant = new Enseignant();
            if (dto instanceof EnseignantDTO) {
                EnseignantDTO enseignantDTO = (EnseignantDTO) dto;
                enseignant.setSpecialite(enseignantDTO.getSpecialite());
                enseignant.setDepartement(enseignantDTO.getDepartement());
            }
            utilisateur = enseignant;
        } else if ("ADMIN".equalsIgnoreCase(dto.getRole())) {
            utilisateur = new Administrateur();
        } else {
            throw new RuntimeException("Rôle invalide. Utilisez ADMIN ou ENSEIGNANT");
        }
        
        utilisateur.setIdUser(dto.getIdUser() != null ? dto.getIdUser() : UUID.randomUUID().toString());
        utilisateur.setMdp(passwordEncoder.encode(dto.getMdp()));
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setNomUser(dto.getNomUser());
        utilisateur.setPrenomUser(dto.getPrenomUser());
        utilisateur.setTelephone(dto.getTelephone());
        utilisateur.setRole(dto.getRole().toUpperCase());
        
        if (utilisateur instanceof Enseignant) {
            enseignantRepository.save((Enseignant) utilisateur);
        } else {
            administrateurRepository.save((Administrateur) utilisateur);
        }
        
        return mapToResponseDTO(utilisateur);
    }
    
    public List<UtilisateurResponseDTO> getAllUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public UtilisateurResponseDTO getUtilisateurById(String id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return mapToResponseDTO(utilisateur);
    }
    
    @Transactional
    public UtilisateurResponseDTO updateUtilisateur(String id, UtilisateurDTO dto) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        if (dto.getEmail() != null && !dto.getEmail().equals(utilisateur.getEmail())) {
            if (utilisateurRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Un utilisateur avec cet email existe déjà");
            }
            utilisateur.setEmail(dto.getEmail());
        }
        
        if (dto.getMdp() != null && !dto.getMdp().isEmpty()) {
            utilisateur.setMdp(passwordEncoder.encode(dto.getMdp()));
        }
        
        if (dto.getNomUser() != null) utilisateur.setNomUser(dto.getNomUser());
        if (dto.getPrenomUser() != null) utilisateur.setPrenomUser(dto.getPrenomUser());
        if (dto.getTelephone() != null) utilisateur.setTelephone(dto.getTelephone());
        
        if (utilisateur instanceof Enseignant && dto instanceof EnseignantDTO) {
            Enseignant enseignant = (Enseignant) utilisateur;
            EnseignantDTO enseignantDTO = (EnseignantDTO) dto;
            if (enseignantDTO.getSpecialite() != null) {
                enseignant.setSpecialite(enseignantDTO.getSpecialite());
            }
            if (enseignantDTO.getDepartement() != null) {
                enseignant.setDepartement(enseignantDTO.getDepartement());
            }
        }
        
        utilisateur = utilisateurRepository.save(utilisateur);
        return mapToResponseDTO(utilisateur);
    }
    
    @Transactional
    public void deleteUtilisateur(String id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        utilisateurRepository.deleteById(id);
    }
    
    public List<UtilisateurResponseDTO> getEnseignants() {
        // Récupérer tous les utilisateurs avec le rôle ENSEIGNANT depuis la table utilisateurs
        return utilisateurRepository.findByRole("ENSEIGNANT").stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    private UtilisateurResponseDTO mapToResponseDTO(Utilisateur utilisateur) {
        UtilisateurResponseDTO dto = new UtilisateurResponseDTO();
        dto.setIdUser(utilisateur.getIdUser());
        dto.setEmail(utilisateur.getEmail());
        dto.setNomUser(utilisateur.getNomUser());
        dto.setPrenomUser(utilisateur.getPrenomUser());
        dto.setTelephone(utilisateur.getTelephone());
        dto.setRole(utilisateur.getRole());
        
        if (utilisateur instanceof Enseignant) {
            Enseignant enseignant = (Enseignant) utilisateur;
            dto.setSpecialite(enseignant.getSpecialite());
            dto.setDepartement(enseignant.getDepartement());
        }
        
        return dto;
    }
}
