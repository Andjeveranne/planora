package com.isi4.userservice.service;

import com.isi4.userservice.dto.EnseignantDTO;
import com.isi4.userservice.dto.UtilisateurDTO;
import com.isi4.userservice.dto.UtilisateurResponseDTO;
import com.isi4.userservice.exception.ResourceNotFoundException;
import com.isi4.userservice.exception.ValidationException;
import com.isi4.userservice.model.Enseignant;
import com.isi4.userservice.model.Utilisateur;
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
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UtilisateurResponseDTO creerUtilisateur(UtilisateurDTO dto) {
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("Un utilisateur avec cet email existe déjà.");
        }

        if (dto instanceof EnseignantDTO enseignantDTO) {
            return creerEnseignant(enseignantDTO);
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setIdUser(UUID.randomUUID().toString());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setMdp(passwordEncoder.encode(dto.getMdp()));
        utilisateur.setNomUser(dto.getNomUser());
        utilisateur.setPrenomUser(dto.getPrenomUser());
        utilisateur.setTelephone(dto.getTelephone());
        utilisateur.setRole(dto.getRole() != null ? dto.getRole() : "ENSEIGNANT");

        utilisateur = utilisateurRepository.save(utilisateur);
        return toResponse(utilisateur);
    }

    @Transactional
    public UtilisateurResponseDTO creerEnseignant(EnseignantDTO dto) {
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("Un utilisateur avec cet email existe déjà.");
        }

        Enseignant enseignant = new Enseignant();
        enseignant.setIdUser(UUID.randomUUID().toString());
        enseignant.setEmail(dto.getEmail());
        enseignant.setMdp(passwordEncoder.encode(dto.getMdp()));
        enseignant.setNomUser(dto.getNomUser());
        enseignant.setPrenomUser(dto.getPrenomUser());
        enseignant.setTelephone(dto.getTelephone());
        enseignant.setRole("ENSEIGNANT");
        enseignant.setSpecialite(dto.getSpecialite());
        enseignant.setDepartement(dto.getDepartement());

        enseignant = enseignantRepository.save(enseignant);
        return toResponse(enseignant);
    }

    @Transactional(readOnly = true)
    public List<UtilisateurResponseDTO> getAllUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UtilisateurResponseDTO> getEnseignants() {
        // Inclut les enseignants de la table `enseignants` (JPA) ET les utilisateurs
        // créés via AuthentificationService avec role='ENSEIGNANT' dans `utilisateurs`
        return utilisateurRepository.findByRole("ENSEIGNANT").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UtilisateurResponseDTO getUtilisateurById(String id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + id));
        return toResponse(utilisateur);
    }

    @Transactional
    public UtilisateurResponseDTO updateUtilisateur(String id, EnseignantDTO dto) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + id));

        if (!utilisateur.getEmail().equals(dto.getEmail()) && utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("Un utilisateur avec cet email existe déjà.");
        }

        utilisateur.setEmail(dto.getEmail());
        utilisateur.setNomUser(dto.getNomUser());
        utilisateur.setPrenomUser(dto.getPrenomUser());
        utilisateur.setTelephone(dto.getTelephone());

        if (dto.getMdp() != null && !dto.getMdp().isBlank()) {
            utilisateur.setMdp(passwordEncoder.encode(dto.getMdp()));
        }

        if (utilisateur instanceof Enseignant enseignant) {
            enseignant.setSpecialite(dto.getSpecialite());
            enseignant.setDepartement(dto.getDepartement());
        }

        utilisateur = utilisateurRepository.save(utilisateur);
        return toResponse(utilisateur);
    }

    @Transactional
    public void deleteUtilisateur(String id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + id);
        }
        utilisateurRepository.deleteById(id);
    }

    private UtilisateurResponseDTO toResponse(Utilisateur utilisateur) {
        UtilisateurResponseDTO dto = new UtilisateurResponseDTO();
        dto.setIdUser(utilisateur.getIdUser());
        dto.setEmail(utilisateur.getEmail());
        dto.setNomUser(utilisateur.getNomUser());
        dto.setPrenomUser(utilisateur.getPrenomUser());
        dto.setTelephone(utilisateur.getTelephone());
        dto.setRole(utilisateur.getRole());

        if (utilisateur instanceof Enseignant enseignant) {
            dto.setSpecialite(enseignant.getSpecialite());
            dto.setDepartement(enseignant.getDepartement());
        }

        return dto;
    }
}
