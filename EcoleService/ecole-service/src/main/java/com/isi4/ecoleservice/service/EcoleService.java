package com.isi4.ecoleservice.service;

import com.isi4.ecoleservice.dto.EcoleDTO;
import com.isi4.ecoleservice.entity.Ecole;
import com.isi4.ecoleservice.exception.ResourceNotFoundException;
import com.isi4.ecoleservice.exception.ValidationException;
import com.isi4.ecoleservice.repository.EcoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service CRUD pour l'entité Ecole (diagramme de classes).
 * Gère uniquement les écoles ; les relations Filiere, Classe, Salle
 * sont gérées dans leurs services respectifs.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EcoleService {

    private final EcoleRepository ecoleRepository;

    /**
     * Créer une nouvelle école.
     * Si idEcole n'est pas fourni, un UUID est généré.
     */
    public EcoleDTO creer(EcoleDTO dto) {
        Ecole ecole = toEntity(dto);
        if (ecole.getIdEcole() == null || ecole.getIdEcole().isBlank()) {
            ecole.setIdEcole(UUID.randomUUID().toString());
        } else if (ecoleRepository.existsByIdEcole(ecole.getIdEcole())) {
            throw new ValidationException("Une école avec l'ID " + ecole.getIdEcole() + " existe déjà");
        }
        Ecole saved = ecoleRepository.save(ecole);
        return toDTO(saved);
    }

    /**
     * Récupérer toutes les écoles.
     */
    @Transactional(readOnly = true)
    public List<EcoleDTO> obtenirToutes() {
        return ecoleRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer une école par son identifiant.
     */
    @Transactional(readOnly = true)
    public EcoleDTO obtenirParId(String idEcole) {
        Ecole ecole = ecoleRepository.findById(idEcole)
                .orElseThrow(() -> new ResourceNotFoundException("École non trouvée avec l'ID: " + idEcole));
        return toDTO(ecole);
    }

    /**
     * Mettre à jour une école existante.
     */
    public EcoleDTO mettreAJour(String idEcole, EcoleDTO dto) {
        Ecole ecole = ecoleRepository.findById(idEcole)
                .orElseThrow(() -> new ResourceNotFoundException("École non trouvée avec l'ID: " + idEcole));
        ecole.setNomEcole(dto.getNomEcole());
        Ecole updated = ecoleRepository.save(ecole);
        return toDTO(updated);
    }

    /**
     * Supprimer une école par son identifiant.
     * Note : les entités liées (Filiere, Classe, Salle) dans d'autres services
     * ne sont pas supprimées par ce service.
     */
    public void supprimer(String idEcole) {
        if (!ecoleRepository.existsByIdEcole(idEcole)) {
            throw new ResourceNotFoundException("École non trouvée avec l'ID: " + idEcole);
        }
        ecoleRepository.deleteById(idEcole);
    }

    private Ecole toEntity(EcoleDTO dto) {
        Ecole e = new Ecole();
        e.setIdEcole(dto.getIdEcole());
        e.setNomEcole(dto.getNomEcole());
        return e;
    }

    private EcoleDTO toDTO(Ecole e) {
        EcoleDTO dto = new EcoleDTO();
        dto.setIdEcole(e.getIdEcole());
        dto.setNomEcole(e.getNomEcole());
        return dto;
    }
}
