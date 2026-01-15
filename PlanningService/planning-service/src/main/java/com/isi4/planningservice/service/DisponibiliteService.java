package com.isi4.planningservice.service;

import com.isi4.planningservice.model.Disponibilite;
import com.isi4.planningservice.repository.DisponibiliteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisponibiliteService {

    private final DisponibiliteRepository disponibiliteRepository;

    public List<Disponibilite> getDisponibilitesByEnseignant(String enseignantId) {
        return disponibiliteRepository.findByEnseignantIdOrderByDateHeureDebutAsc(enseignantId);
    }

    @Transactional
    public Disponibilite addDisponibilite(String enseignantId, LocalDateTime debut, LocalDateTime fin,
            String commentaire) {
        if (debut.isAfter(fin)) {
            throw new IllegalArgumentException("La date de début doit être avant la date de fin");
        }

        Disponibilite dispo = new Disponibilite();
        dispo.setEnseignantId(enseignantId);
        dispo.setDateHeureDebut(debut);
        dispo.setDateHeureFin(fin);
        dispo.setCommentaire(commentaire);

        return disponibiliteRepository.save(dispo);
    }

    @Transactional
    public void deleteDisponibilite(Long id) {
        if (!disponibiliteRepository.existsById(id)) {
            throw new RuntimeException("Disponibilité introuvable");
        }
        disponibiliteRepository.deleteById(id);
    }
}
