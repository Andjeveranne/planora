package com.isi4.planningservice.service;

import com.isi4.planningservice.dto.EmploiDuTempsDTO;
import com.isi4.planningservice.model.EmploiDuTemps;
import com.isi4.planningservice.model.Disponibilite;
import com.isi4.planningservice.repository.EmploiDuTempsRepository;
import com.isi4.planningservice.repository.DisponibiliteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmploiDuTempsService {

    private final EmploiDuTempsRepository edtRepository;
    private final DisponibiliteRepository disponibiliteRepository;

    // UE, classes et salles pour la génération automatique
    private static final String[] UES = {
        "Algorithmique", "Base de données", "Réseaux", 
        "Programmation Web", "Systèmes d'exploitation",
        "Génie Logiciel", "Intelligence Artificielle"
    };
    private static final String[] CLASSES = {"ISI1", "ISI2", "ISI3", "ISI4", "Master 1"};
    private static final String[] SALLES = {"Salle 101", "Salle 202", "Labo Info 1", "Amphi A", "Salle 305"};
    private static final String[] TYPES = {"CM", "TD", "TP"};

    public List<EmploiDuTemps> getEdtByEnseignant(String enseignantId) {
        return edtRepository.findByEnseignantId(enseignantId);
    }

    public List<EmploiDuTemps> getEdtByEnseignantAndWeek(String enseignantId, LocalDate dateDebut, LocalDate dateFin) {
        return edtRepository.findByEnseignantIdAndDateJourBetween(enseignantId, dateDebut, dateFin);
    }

    public EmploiDuTemps addCours(EmploiDuTempsDTO dto) {
        EmploiDuTemps edt = new EmploiDuTemps();
        edt.setEnseignantId(dto.getEnseignantId());
        edt.setDateJour(dto.getDateJour());
        edt.setJour(dto.getJour());
        edt.setHeureDebut(dto.getHeureDebut());
        edt.setHeureFin(dto.getHeureFin());
        edt.setUe(dto.getUe());
        edt.setClasse(dto.getClasse());
        edt.setSalle(dto.getSalle());
        edt.setTypeCours(dto.getTypeCours());
        return edtRepository.save(edt);
    }

    @Transactional
    public List<EmploiDuTemps> genererEdtFromDisponibilites(String enseignantId) {
        // Récupérer les disponibilités
        List<Disponibilite> disponibilites = disponibiliteRepository.findByEnseignantId(enseignantId);

        if (disponibilites.isEmpty()) {
            return new ArrayList<>();
        }

        // Date d'aujourd'hui pour ne pas modifier les cours passés
        LocalDate today = LocalDate.now();

        // Supprimer uniquement les cours futurs (pas les cours passés)
        List<EmploiDuTemps> existingEdt = edtRepository.findByEnseignantId(enseignantId);
        for (EmploiDuTemps edt : existingEdt) {
            if (!edt.getDateJour().isBefore(today)) {
                edtRepository.delete(edt);
            }
        }

        List<EmploiDuTemps> edtGenere = new ArrayList<>();
        int index = 0;

        for (Disponibilite dispo : disponibilites) {
            LocalDate dateJour = dispo.getDateHeureDebut().toLocalDate();
            
            // Ne pas générer de cours pour les jours passés
            if (dateJour.isBefore(today)) {
                continue;
            }
            
            int heureDebut = dispo.getDateHeureDebut().getHour();
            int heureFin = dispo.getDateHeureFin().getHour();

            // Obtenir le nom du jour en français
            String jour = dateJour.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.FRENCH);
            jour = jour.substring(0, 1).toUpperCase() + jour.substring(1);

            EmploiDuTemps edt = new EmploiDuTemps();
            edt.setEnseignantId(enseignantId);
            edt.setDateJour(dateJour);
            edt.setJour(jour);
            edt.setHeureDebut(heureDebut);
            edt.setHeureFin(heureFin);
            edt.setUe(UES[index % UES.length]);
            edt.setClasse(CLASSES[index % CLASSES.length]);
            edt.setSalle(SALLES[index % SALLES.length]);
            edt.setTypeCours(TYPES[index % TYPES.length]);

            edtGenere.add(edtRepository.save(edt));
            index++;
        }

        // Ajouter les cours passés existants au résultat
        List<EmploiDuTemps> pastCourses = edtRepository.findByEnseignantId(enseignantId)
                .stream()
                .filter(e -> e.getDateJour().isBefore(today))
                .toList();
        
        List<EmploiDuTemps> allCourses = new ArrayList<>(pastCourses);
        allCourses.addAll(edtGenere);
        
        return allCourses;
    }

    @Transactional
    public void deleteEdtByEnseignant(String enseignantId) {
        edtRepository.deleteByEnseignantId(enseignantId);
    }

    public void deleteCours(Long id) {
        edtRepository.deleteById(id);
    }
}
