package com.isi4.userservice.service;

import com.isi4.userservice.dto.EtudiantDTO;
import com.isi4.userservice.dto.EtudiantResponseDTO;
import com.isi4.userservice.exception.ResourceNotFoundException;
import com.isi4.userservice.exception.ValidationException;
import com.isi4.userservice.model.Etudiant;
import com.isi4.userservice.repository.EtudiantRepository;
import com.isi4.userservice.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public EtudiantService(EtudiantRepository etudiantRepository,
                          UtilisateurRepository utilisateurRepository,
                          PasswordEncoder passwordEncoder) {
        this.etudiantRepository = etudiantRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EtudiantResponseDTO createEtudiant(EtudiantDTO dto) {
        if (dto == null) {
            throw new ValidationException("Les données de l'étudiant sont obligatoires.");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new ValidationException("L'email est obligatoire.");
        }
        dto.setEmail(dto.getEmail().trim());
        if (dto.getIdEcole() == null || dto.getIdEcole().isBlank()) {
            throw new ValidationException("L'école de l'étudiant est obligatoire.");
        }
        if (utilisateurRepository.existsByEmail(dto.getEmail()) || etudiantRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("Un utilisateur avec cet email existe déjà.");
        }

        Etudiant etudiant = new Etudiant();
        etudiant.setIdUser(UUID.randomUUID().toString());
        etudiant.setEmail(dto.getEmail());
        etudiant.setMdp(passwordEncoder.encode(dto.getMdp() == null || dto.getMdp().isBlank() ? "student123" : dto.getMdp()));
        etudiant.setNomUser(dto.getNomUser());
        etudiant.setPrenomUser(dto.getPrenomUser());
        etudiant.setTelephone(dto.getTelephone());
        etudiant.setRole("ETUDIANT");
        etudiant.setIdEcole(dto.getIdEcole());
        etudiant.setDepartement(dto.getDepartement());
        etudiant.setSpecialite(dto.getSpecialite());
        etudiant.setNiveau(dto.getNiveau());
        etudiant.setClasse(dto.getClasse());
        etudiant.setStatus(dto.getStatus() == null || dto.getStatus().isBlank() ? "Actif" : dto.getStatus());

        etudiant = etudiantRepository.save(etudiant);
        return toResponse(etudiant);
    }

    @Transactional(readOnly = true)
    public List<EtudiantResponseDTO> getAllEtudiants() {
        return etudiantRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EtudiantResponseDTO> getEtudiantsByEcole(String idEcole) {
        return etudiantRepository.findByIdEcole(idEcole).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EtudiantResponseDTO getEtudiantById(String id) {
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'id: " + id));
        return toResponse(etudiant);
    }

    @Transactional
    public EtudiantResponseDTO updateEtudiant(String id, EtudiantDTO dto) {
        if (dto == null) {
            throw new ValidationException("Les données de l'étudiant sont obligatoires.");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new ValidationException("L'email est obligatoire.");
        }
        dto.setEmail(dto.getEmail().trim());
        if (etudiantRepository.existsByEmailAndIdUserNot(dto.getEmail(), id)) {
            throw new ValidationException("Un étudiant avec cet email existe déjà.");
        }

        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'id: " + id));

        etudiant.setEmail(dto.getEmail());
        etudiant.setNomUser(dto.getNomUser());
        etudiant.setPrenomUser(dto.getPrenomUser());
        etudiant.setTelephone(dto.getTelephone());
        etudiant.setRole("ETUDIANT");
        etudiant.setIdEcole(dto.getIdEcole());
        etudiant.setDepartement(dto.getDepartement());
        etudiant.setSpecialite(dto.getSpecialite());
        etudiant.setNiveau(dto.getNiveau());
        etudiant.setClasse(dto.getClasse());
        etudiant.setStatus(dto.getStatus() == null || dto.getStatus().isBlank() ? "Actif" : dto.getStatus());

        if (dto.getMdp() != null && !dto.getMdp().isBlank()) {
            etudiant.setMdp(passwordEncoder.encode(dto.getMdp()));
        }

        return toResponse(etudiantRepository.save(etudiant));
    }

    @Transactional
    public void deleteEtudiant(String id) {
        if (!etudiantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Étudiant non trouvé avec l'id: " + id);
        }
        etudiantRepository.deleteById(id);
    }

    private EtudiantResponseDTO toResponse(Etudiant etudiant) {
        EtudiantResponseDTO dto = new EtudiantResponseDTO();
        dto.setIdUser(etudiant.getIdUser());
        dto.setEmail(etudiant.getEmail());
        dto.setNomUser(etudiant.getNomUser());
        dto.setPrenomUser(etudiant.getPrenomUser());
        dto.setTelephone(etudiant.getTelephone());
        dto.setRole(etudiant.getRole());
        dto.setIdEcole(etudiant.getIdEcole());
        dto.setDepartement(etudiant.getDepartement());
        dto.setSpecialite(etudiant.getSpecialite());
        dto.setNiveau(etudiant.getNiveau());
        dto.setClasse(etudiant.getClasse());
        dto.setStatus(etudiant.getStatus());
        return dto;
    }
}
