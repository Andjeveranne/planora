package com.isi4.userservice.service;

import com.isi4.userservice.model.Disponibilite;
import com.isi4.userservice.repository.DisponibiliteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service pour les vérifications de sécurité et d'autorisation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    private final DisponibiliteRepository disponibiliteRepository;

    /**
     * Vérifie si l'utilisateur actuel est le propriétaire de l'ID donné
     */
    public boolean isCurrentUser(String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUserId = authentication.getName(); // Le JWT contient l'ID utilisateur
        return userId != null && userId.equals(currentUserId);
    }

    /**
     * Vérifie si l'utilisateur actuel est le propriétaire d'une disponibilité
     */
    public boolean isOwnerOfDisponibilite(Long disponibiliteId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUserId = authentication.getName();

        Optional<Disponibilite> disponibilite = disponibiliteRepository.findById(disponibiliteId);
        if (disponibilite.isPresent()) {
            // Utilisation de ID User de l'enseignant (String)
            return disponibilite.get().getEnseignant().getIdUser().equals(currentUserId);
        }
        return false;
    }

    /**
     * Vérifie si l'utilisateur actuel a le rôle ADMIN
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Vérifie si l'utilisateur actuel a le rôle ENSEIGNANT
     */
    public boolean isEnseignant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ENSEIGNANT"));
    }

    /**
     * Récupère l'ID de l'utilisateur actuel
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return authentication.getName();
    }
}
