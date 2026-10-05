package com.isi4.planningservice.service;

import com.isi4.planningservice.algorithm.FordFulkersonScheduler;
import com.isi4.planningservice.dto.EmploiDuTempsDTO;
import com.isi4.planningservice.entity.EmploiDuTemps;
import com.isi4.planningservice.repository.EmploiDuTempsRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.time.temporal.TemporalAdjusters;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmploiDuTempsServiceV2 {

    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final DisponibiliteService disponibiliteService;
    private final FordFulkersonScheduler scheduler;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public List<EmploiDuTempsDTO> genererEmploisDuTemps(String idEcole) {
        return genererEmploisDuTemps(idEcole, null, null);
    }

    @Transactional
    public List<EmploiDuTempsDTO> genererEmploisDuTemps(String idEcole, String departement, String specialite) {
        log.info("Génération EDT école={}, département={}, spécialité={}", idEcole, departement, specialite);

        try {
            LocalDateTime debutSemaineProchaine = LocalDateTime.now()
                    .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                    .withHour(8)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            
            int semaineProchaine = debutSemaineProchaine.get(WeekFields.ISO.weekOfYear());
            
            log.info("Génération pour la semaine {} (du {})", semaineProchaine, debutSemaineProchaine.toLocalDate());

            // 2. Récupérer toutes les données nécessaires
            Map<String, Object> classesData = getClassesData(idEcole);
            Map<String, Object> sallesData = getSallesData(idEcole);
            Map<String, Object> uesData = getUEsData();
            Map<String, Object> enseignantsData = getEnseignantsData(idEcole, departement, specialite);

            if (classesData.isEmpty()) {
                throw new IllegalStateException(idEcole == null || idEcole.isBlank()
                        ? "Aucune classe n'est configurée."
                        : "Aucune classe n'est associée à cette école. Associez ses filières à l'école avant de générer.");
            }
            if (sallesData.isEmpty()) {
                throw new IllegalStateException(idEcole == null || idEcole.isBlank()
                        ? "Aucune salle disponible n'est configurée."
                        : "Aucune salle disponible n'est configurée pour cette école.");
            }
            
            // 3. Créer les créneaux disponibles pour la semaine prochaine
            List<FordFulkersonScheduler.TimeSlot> timeSlots = generateTimeSlots(debutSemaineProchaine);
            
            // 4. Préparer les demandes de cours basées sur les UEs et les classes
            List<FordFulkersonScheduler.SchedulingRequest> requests = prepareSchedulingRequestsFromUEs(
                    classesData, uesData, enseignantsData);
                if (requests.isEmpty()) {
                throw new IllegalStateException(
                    "Aucun cours planifiable: vérifiez les UE correspondant au niveau et à la filière des classes, " +
                    "puis affectez au moins un enseignant qualifié à chaque UE.");
                }
            
            // 5. Préparer les salles
            List<FordFulkersonScheduler.Salle> salles = prepareSalles(sallesData);
            
            // 6. Récupérer les disponibilités des enseignants pour la semaine prochaine
            List<FordFulkersonScheduler.Disponibilite> disponibilites = getEnseignantDisponibilites(
                    enseignantsData, idEcole, debutSemaineProchaine);
            
            // 7. Générer l'emploi du temps avec Ford-Fulkerson
            List<FordFulkersonScheduler.SchedulingResult> results = scheduler.generateSchedule(
                    requests, timeSlots, salles, disponibilites);

                long coursPlanifies = results.stream()
                    .filter(result -> !"TPE".equals(result.getTypeCours()))
                    .count();
                if (coursPlanifies == 0) {
                throw new IllegalStateException(
                    "Aucun cours n'a pu être placé: vérifiez les disponibilités des enseignants pour la semaine " +
                    "à venir, les capacités et les types de salles.");
                }
            
            // 8. Compléter avec TPE pour les créneaux vides
            results = completerAvecTPE(results, timeSlots, classesData, semaineProchaine);
            
            // 9. Sauvegarder les résultats
                emploiDuTempsRepository.deleteBySemaineAndClasseIds(semaineProchaine,
                    new ArrayList<>(classesData.keySet()));
            List<EmploiDuTemps> emplois = saveSchedulingResults(results);
            scheduleTeacherNotifications(emplois, enseignantsData);
            
            // 10. Convertir en DTO avec les informations complètes
            return convertToDTO(emplois, classesData, sallesData, uesData, enseignantsData);
            
        } catch (Exception e) {
            log.error("Erreur lors de la génération des emplois du temps", e);
            throw new RuntimeException("Erreur lors de la génération des emplois du temps: " + e.getMessage());
        }
    }

    private List<FordFulkersonScheduler.TimeSlot> generateTimeSlots(LocalDateTime debutSemaine) {
        List<FordFulkersonScheduler.TimeSlot> slots = new ArrayList<>();
        
        // Créneaux de 8h à 18h, par blocs de 2h
        LocalTime[] heuresDebut = {
            LocalTime.of(8, 0), LocalTime.of(10, 0), LocalTime.of(12, 0), 
            LocalTime.of(14, 0), LocalTime.of(16, 0)
        };
        
        String[] creneaux = {"8h-10h", "10h-12h", "12h-14h", "14h-16h", "16h-18h"};
        
        // Du lundi au vendredi
        for (int jour = 1; jour <= 5; jour++) {
            for (int i = 0; i < heuresDebut.length; i++) {
                FordFulkersonScheduler.TimeSlot slot = new FordFulkersonScheduler.TimeSlot();
                
                LocalDateTime debut = debutSemaine.plusDays(jour - 1)
                        .withHour(heuresDebut[i].getHour())
                        .withMinute(heuresDebut[i].getMinute());
                
                slot.setDebut(debut);
                slot.setFin(debut.plusHours(2));
                slot.setJourSemaine(jour);
                slot.setCreneau(creneaux[i]);
                slot.setSemaine(debut.get(WeekFields.ISO.weekOfYear()));
                
                slots.add(slot);
            }
        }
        
        return slots;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getClassesData(String idEcole) {
        try {
            WebClient webClient = authorizedWebClient();
            WebClient.RequestHeadersSpec<?> request = idEcole == null || idEcole.isBlank()
                ? webClient.get().uri("http://classes-service/api/classes")
                : webClient.get().uri("http://classes-service/api/classes/ecole/{idEcole}", idEcole);
            List<Map<String, Object>> classes = request
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            return (classes == null ? Collections.<Map<String, Object>>emptyList() : classes).stream()
                    .filter(classe -> {
                        String classSchoolId = (String) classe.get("idEcole");
                        if (classSchoolId == null || classSchoolId.isBlank()) {
                            return false;
                        }
                        return idEcole == null || idEcole.isBlank() || idEcole.equals(classSchoolId);
                    })
                    .collect(Collectors.toMap(
                            classe -> (String) classe.get("idClasse"),
                            classe -> classe,
                            (first, duplicate) -> first
                    ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des classes", e);
            throw new IllegalStateException("Impossible de récupérer les classes: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getSallesData(String idEcole) {
        try {
            WebClient webClient = authorizedWebClient();
            WebClient.RequestHeadersSpec<?> request = idEcole == null || idEcole.isBlank()
                ? webClient.get().uri("http://salle-service/api/salles")
                : webClient.get().uri("http://salle-service/api/salles/ecole/{idEcole}", idEcole);
            List<Map<String, Object>> salles = request
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            return (salles == null ? Collections.<Map<String, Object>>emptyList() : salles).stream()
                    .filter(salle -> "DISPONIBLE".equals(salle.get("statut")))
                    .collect(Collectors.toMap(
                            salle -> (String) salle.get("idSalle"),
                            salle -> salle,
                            (first, duplicate) -> first
                    ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des salles", e);
                    throw new IllegalStateException("Impossible de récupérer les salles: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getUEsData() {
        try {
            WebClient webClient = authorizedWebClient();
            List<Map<String, Object>> ues = webClient.get()
                    .uri("http://ue-service/api/ues")
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            
            return (ues == null ? Collections.<Map<String, Object>>emptyList() : ues).stream()
                    .collect(Collectors.toMap(
                            ue -> String.valueOf(((Number) ue.get("id")).longValue()),
                            ue -> ue
                    ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des UEs", e);
            throw new IllegalStateException("Impossible de récupérer les unités d'enseignement: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> getEnseignantsData(String idEcole, String departement, String specialite) {
        try {
            WebClient webClient = authorizedWebClient();
            String uri = "http://user-service/api/utilisateurs/enseignants/recherche";
            org.springframework.web.util.UriComponentsBuilder uriBuilder = org.springframework.web.util.UriComponentsBuilder.fromUriString(uri);
            if (idEcole != null && !idEcole.isBlank()) uriBuilder.queryParam("idEcole", idEcole);
            if (departement != null && !departement.isBlank()) uriBuilder.queryParam("departement", departement);
            if (specialite != null && !specialite.isBlank()) uriBuilder.queryParam("specialite", specialite);
            URI targetUri = uriBuilder.build().encode().toUri();
            WebClient.RequestHeadersSpec<?> request = webClient.get().uri(targetUri);
            List<Map<String, Object>> enseignants = request
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            
                return (enseignants == null ? Collections.<Map<String, Object>>emptyList() : enseignants).stream()
                    .collect(Collectors.toMap(
                            enseignant -> (String) enseignant.get("idUser"),
                        enseignant -> enseignant,
                        (first, duplicate) -> first
                    ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des enseignants", e);
            throw new IllegalStateException("Impossible de récupérer les enseignants: " + e.getMessage(), e);
        }
    }

    private List<FordFulkersonScheduler.SchedulingRequest> prepareSchedulingRequestsFromUEs(
            Map<String, Object> classesData,
            Map<String, Object> uesData,
            Map<String, Object> enseignantsData) {

        List<FordFulkersonScheduler.SchedulingRequest> requests = new ArrayList<>();
        
        // Pour chaque classe, trouver les UEs correspondantes
        for (Map.Entry<String, Object> classeEntry : classesData.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> classeInfo = (Map<String, Object>) classeEntry.getValue();
            
            String classeId = classeEntry.getKey();
            String niveau = (String) classeInfo.get("niveau");
            String filiereId = (String) classeInfo.get("idFiliere");
            String ecoleId = (String) classeInfo.get("idEcole");
            Integer effectif = (Integer) classeInfo.get("effectifs");
            if (niveau == null || filiereId == null || effectif == null || ecoleId == null) {
                log.warn("Classe {} ignorée: filière, niveau, effectif ou école manquant", classeId);
                continue;
            }
            
            // Trouver les UEs pour cette classe (même niveau et filière)
            for (Map.Entry<String, Object> ueEntry : uesData.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> ueInfo = (Map<String, Object>) ueEntry.getValue();
                
                String ueNiveau = (String) ueInfo.get("niveau");
                String ueFiliere = (String) ueInfo.get("idFiliere");
                    java.time.LocalTime heureDebut = parseTime(ueInfo.get("heureDebut"), java.time.LocalTime.of(8, 0));
                    java.time.LocalTime heureFin = parseTime(ueInfo.get("heureFin"), java.time.LocalTime.of(18, 0));
                if (ueNiveau != null && ueFiliere != null
                    && niveau.equals(ueNiveau) && filiereId.equals(ueFiliere)) {
                    Long ueId = Long.valueOf(ueEntry.getKey());
                    
                    // Récupérer les enseignants compétents pour cette UE
                    Map<String, Object> enseignantsUE = getEnseignantsForUE(ueId, enseignantsData.keySet(), ecoleId);
                    
                    // Créer les demandes pour chaque type de cours
                    createRequestsForUE(requests, classeId, ecoleId, ueId, ueInfo, enseignantsUE, effectif, heureDebut, heureFin);
                }
            }
        }
        
        return requests;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getEnseignantsForUE(Long ueId, Set<String> schoolTeacherIds, String idEcole) {
        try {
            WebClient webClient = authorizedWebClient();
            List<Map<String, Object>> enseignantsUE = webClient.get()
                    .uri("http://user-service/api/enseignant-ues/ue/{ueId}", ueId)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            
                return (enseignantsUE == null ? Collections.<Map<String, Object>>emptyList() : enseignantsUE).stream()
                    .filter(enseignant -> schoolTeacherIds.contains(String.valueOf(enseignant.get("enseignantId"))))
                    .filter(enseignant -> idEcole == null || idEcole.equals(enseignant.get("idEcole")))
                    .collect(Collectors.toMap(
                            ens -> (String) ens.get("enseignantId"),
                        ens -> ens,
                        (first, duplicate) -> first
                    ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des enseignants pour l'UE {}", ueId, e);
            throw new IllegalStateException("Impossible de récupérer les enseignants affectés à l'UE " + ueId, e);
        }
    }

    private void createRequestsForUE(
            List<FordFulkersonScheduler.SchedulingRequest> requests,
            String classeId,
            String ecoleId,
            Long ueId,
            Map<String, Object> ueInfo,
            Map<String, Object> enseignantsUE,
            Integer effectif,
            java.time.LocalTime heureDebut, java.time.LocalTime heureFin) {

        Integer heuresCM = (Integer) ueInfo.get("heuresCM");
        Integer heuresTD = (Integer) ueInfo.get("heuresTD");
        Integer heuresTP = (Integer) ueInfo.get("heuresTP");

        // CM
        if (heuresCM != null && heuresCM > 0) {
            String enseignantCM = findEnseignantForType(enseignantsUE, "CM");
            if (enseignantCM != null) {
                requests.add(createRequest(classeId, ecoleId, ueId, enseignantCM, "CM", heuresCM, effectif, heureDebut, heureFin));
            }
        }

        // TD
        if (heuresTD != null && heuresTD > 0) {
            String enseignantTD = findEnseignantForType(enseignantsUE, "TD");
            if (enseignantTD != null) {
                requests.add(createRequest(classeId, ecoleId, ueId, enseignantTD, "TD", heuresTD, effectif, heureDebut, heureFin));
            }
        }

        // TP
        if (heuresTP != null && heuresTP > 0) {
            String enseignantTP = findEnseignantForType(enseignantsUE, "TP");
            if (enseignantTP != null) {
                requests.add(createRequest(classeId, ecoleId, ueId, enseignantTP, "TP", heuresTP, effectif, heureDebut, heureFin));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String findEnseignantForType(Map<String, Object> enseignantsUE, String typeCours) {
        return enseignantsUE.entrySet().stream()
                .filter(entry -> {
                    Map<String, Object> competence = (Map<String, Object>) entry.getValue();
                    Boolean peut = (Boolean) competence.get("peut" + typeCours);
                    return Boolean.TRUE.equals(peut);
                })
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private FordFulkersonScheduler.SchedulingRequest createRequest(
            String classeId, String ecoleId, Long ueId, String enseignantId, String typeCours, Integer heures, Integer effectif,
            java.time.LocalTime heureDebut, java.time.LocalTime heureFin) {
        
        FordFulkersonScheduler.SchedulingRequest request = new FordFulkersonScheduler.SchedulingRequest();
        request.setClasseId(classeId);
        request.setIdEcole(ecoleId);
        request.setUeId(ueId);
        request.setEnseignantId(enseignantId);
        request.setTypeCours(typeCours);
        request.setHeuresRequises(heures);
        request.setEffectifClasse(effectif);
        request.setHeureDebut(heureDebut);
        request.setHeureFin(heureFin);
        return request;
    }

    private java.time.LocalTime parseTime(Object value, java.time.LocalTime fallback) {
        if (value == null) return fallback;
        try { return java.time.LocalTime.parse(String.valueOf(value)); }
        catch (RuntimeException ignored) { return fallback; }
    }

    private List<FordFulkersonScheduler.Salle> prepareSalles(Map<String, Object> sallesData) {
        return sallesData.values().stream()
                .map(salleData -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> salle = (Map<String, Object>) salleData;
                    
                    FordFulkersonScheduler.Salle s = new FordFulkersonScheduler.Salle();
                    s.setId((String) salle.get("idSalle"));
                    s.setIdEcole((String) salle.get("idEcole"));
                    s.setNom((String) salle.get("nomSalle"));
                    s.setCapacite((Integer) salle.get("capacite"));
                    s.setType((String) salle.get("type"));
                    return s;
                })
                .collect(Collectors.toList());
    }

    private List<FordFulkersonScheduler.Disponibilite> getEnseignantDisponibilites(
            Map<String, Object> enseignantsData, String idEcole, LocalDateTime debutSemaine) {
        
        List<FordFulkersonScheduler.Disponibilite> disponibilites = new ArrayList<>();
        
        for (String enseignantId : enseignantsData.keySet()) {
            try {
                        disponibiliteService.getDisponibilitesByEnseignantAndSchool(
                            enseignantId,
                            idEcole == null || idEcole.isBlank()
                                ? (String) ((Map<String, Object>) enseignantsData.get(enseignantId)).get("idEcole")
                                : idEcole)
                        .stream()
                        .filter(dispo -> {
                            // Filtrer les disponibilités pour la semaine prochaine
                            LocalDateTime debut = dispo.getDateHeureDebut();
                            return !debut.isBefore(debutSemaine) &&
                                   debut.isBefore(debutSemaine.plusWeeks(1));
                        })
                        .forEach(dispo -> {
                            FordFulkersonScheduler.Disponibilite d = new FordFulkersonScheduler.Disponibilite();
                            d.setEnseignantId(enseignantId);
                            d.setDebut(dispo.getDateHeureDebut());
                            d.setFin(dispo.getDateHeureFin());
                            disponibilites.add(d);
                        });
            } catch (Exception e) {
                log.warn("Erreur lors de la récupération des disponibilités pour l'enseignant {}", enseignantId);
            }
        }
        
        return disponibilites;
    }

    private List<FordFulkersonScheduler.SchedulingResult> completerAvecTPE(
            List<FordFulkersonScheduler.SchedulingResult> results,
            List<FordFulkersonScheduler.TimeSlot> timeSlots,
            Map<String, Object> classesData,
            Integer semaine) {

        List<FordFulkersonScheduler.SchedulingResult> completResults = new ArrayList<>(results);
        
        // Pour chaque classe, vérifier les créneaux vides
        for (String classeId : classesData.keySet()) {
            for (FordFulkersonScheduler.TimeSlot slot : timeSlots) {
                // Vérifier si ce créneau est libre pour cette classe
                boolean creneauOccupe = results.stream()
                        .anyMatch(result -> result.getClasseId().equals(classeId) &&
                                result.getDateHeureDebut().equals(slot.getDebut()));
                
                if (!creneauOccupe) {
                    // Créer un TPE pour ce créneau
                    FordFulkersonScheduler.SchedulingResult tpe = new FordFulkersonScheduler.SchedulingResult();
                    tpe.setClasseId(classeId);
                    tpe.setEnseignantId("TPE");
                    tpe.setUeId(0L); // ID spécial pour TPE
                    tpe.setSalleId("TPE");
                    tpe.setDateHeureDebut(slot.getDebut());
                    tpe.setDateHeureFin(slot.getFin());
                    tpe.setTypeCours("TPE");
                    tpe.setSemaine(semaine);
                    tpe.setJourSemaine(slot.getJourSemaine());
                    tpe.setCreneau(slot.getCreneau());
                    
                    completResults.add(tpe);
                }
            }
        }
        
        return completResults;
    }

    private List<EmploiDuTemps> saveSchedulingResults(List<FordFulkersonScheduler.SchedulingResult> results) {
        List<EmploiDuTemps> emplois = results.stream()
                .map(result -> {
                    EmploiDuTemps emploi = new EmploiDuTemps();
                    emploi.setClasseId(result.getClasseId());
                    emploi.setEnseignantId(result.getEnseignantId());
                    emploi.setUeId(result.getUeId());
                    emploi.setSalleId(result.getSalleId());
                    emploi.setDateHeureDebut(result.getDateHeureDebut());
                    emploi.setDateHeureFin(result.getDateHeureFin());
                    emploi.setTypeCours(result.getTypeCours());
                    emploi.setSemaine(result.getSemaine());
                    emploi.setJourSemaine(result.getJourSemaine());
                    emploi.setCreneau(result.getCreneau());
                    return emploi;
                })
                .collect(Collectors.toList());
        
        return emploiDuTempsRepository.saveAll(emplois);
    }

    private List<EmploiDuTempsDTO> convertToDTO(
            List<EmploiDuTemps> emplois,
            Map<String, Object> classesData,
            Map<String, Object> sallesData,
            Map<String, Object> uesData,
            Map<String, Object> enseignantsData) {

        return emplois.stream()
                .map(emploi -> {
                    EmploiDuTempsDTO dto = new EmploiDuTempsDTO();
                    dto.setId(emploi.getId());
                    dto.setClasseId(emploi.getClasseId());
                    dto.setEnseignantId(emploi.getEnseignantId());
                    dto.setUeId(emploi.getUeId());
                    dto.setSalleId(emploi.getSalleId());
                    dto.setDateHeureDebut(emploi.getDateHeureDebut());
                    dto.setDateHeureFin(emploi.getDateHeureFin());
                    dto.setTypeCours(emploi.getTypeCours());
                    dto.setSemaine(emploi.getSemaine());
                    dto.setJourSemaine(emploi.getJourSemaine());
                    dto.setCreneau(emploi.getCreneau());
                    
                    // Enrichir avec les données des autres services
                    if (!"TPE".equals(emploi.getTypeCours())) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> classeInfo = (Map<String, Object>) classesData.get(emploi.getClasseId());
                        if (classeInfo != null) {
                            dto.setNomClasse((String) classeInfo.get("nomClasse"));
                        }
                        
                        @SuppressWarnings("unchecked")
                        Map<String, Object> salleInfo = (Map<String, Object>) sallesData.get(emploi.getSalleId());
                        if (salleInfo != null) {
                            dto.setNomSalle((String) salleInfo.get("nomSalle"));
                        }
                        
                        @SuppressWarnings("unchecked")
                        Map<String, Object> ueInfo = (Map<String, Object>) uesData.get(String.valueOf(emploi.getUeId()));
                        if (ueInfo != null) {
                            dto.setCodeUE((String) ueInfo.get("codeUE"));
                            dto.setNomUE((String) ueInfo.get("nomUE"));
                        }
                        
                        @SuppressWarnings("unchecked")
                        Map<String, Object> enseignantInfo = (Map<String, Object>) enseignantsData.get(emploi.getEnseignantId());
                        if (enseignantInfo != null) {
                            dto.setNomEnseignant(enseignantInfo.get("nomUser") + " " + enseignantInfo.get("prenomUser"));
                        }
                    } else {
                        // TPE
                        dto.setCodeUE("TPE");
                        dto.setNomUE("Temps Personnel Étudiant");
                        dto.setNomSalle("Libre");
                        dto.setNomEnseignant("Autonomie");
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void scheduleTeacherNotifications(List<EmploiDuTemps> emplois, Map<String, Object> enseignantsData) {
        WebClient client = authorizedWebClient();
        for (EmploiDuTemps emploi : emplois) {
            if ("TPE".equals(emploi.getTypeCours()) || emploi.getEnseignantId() == null) continue;
            Map<String, Object> teacher = enseignantsData.get(emploi.getEnseignantId()) instanceof Map
                    ? (Map<String, Object>) enseignantsData.get(emploi.getEnseignantId()) : Collections.emptyMap();
            String email = String.valueOf(teacher.getOrDefault("email", ""));
            LocalDateTime reminderAt = emploi.getDateHeureDebut().minusMinutes(15);
            String message = String.format("Votre cours %s (%s) commence à %s. Classe: %s, salle: %s.",
                    emploi.getUeId(), emploi.getTypeCours(), emploi.getDateHeureDebut(), emploi.getClasseId(), emploi.getSalleId());
            for (String channel : List.of("IN_APP", "EMAIL")) {
                Map<String, Object> notification = new HashMap<>();
                notification.put("destinataireId", emploi.getEnseignantId());
                notification.put("destinataireEmail", email);
                notification.put("canal", channel);
                notification.put("sujet", "Cours dans 15 minutes");
                notification.put("message", message);
                notification.put("scheduledAt", reminderAt);
                notification.put("deduplicationKey", String.join("|", emploi.getEnseignantId(),
                    String.valueOf(emploi.getUeId()), String.valueOf(emploi.getDateHeureDebut()), channel));
                try {
                    client.post().uri("http://notification-service/api/notifications")
                            .bodyValue(notification).retrieve().toBodilessEntity().block();
                } catch (Exception e) {
                    log.warn("Notification impossible pour {}: {}", emploi.getEnseignantId(), e.getMessage());
                }
            }
        }
    }

    public List<Map<String, Object>> getEnseignantsAvecDisponibilitesDetailles(String idEcole, String departement) {
        try {
            // Récupérer les enseignants qui ont des disponibilités
            List<String> enseignantIds = disponibiliteService.getEnseignantsAvecDisponibilites();
            
            // Récupérer les détails des enseignants
            Map<String, Object> enseignantsData = getEnseignantsData(idEcole, departement, null);
            
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (String enseignantId : enseignantIds) {
                @SuppressWarnings("unchecked")
                Map<String, Object> enseignantInfo = (Map<String, Object>) enseignantsData.get(enseignantId);
                
                if (enseignantInfo != null) {
                    Map<String, Object> enseignantDetail = new HashMap<>();
                    enseignantDetail.put("idUser", enseignantId);
                    enseignantDetail.put("nom", enseignantInfo.get("nomUser"));
                    enseignantDetail.put("prenom", enseignantInfo.get("prenomUser"));
                    enseignantDetail.put("email", enseignantInfo.get("email"));
                    enseignantDetail.put("departement", enseignantInfo.get("departement"));
                    enseignantDetail.put("specialite", enseignantInfo.get("specialite"));
                    enseignantDetail.put("idEcole", enseignantInfo.get("idEcole"));
                    
                    // Compter les disponibilités
                    int nombreDisponibilites = disponibiliteService.getDisponibilitesByEnseignant(enseignantId).size();
                    enseignantDetail.put("nombreDisponibilites", nombreDisponibilites);
                    
                    result.add(enseignantDetail);
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des enseignants avec disponibilités détaillées", e);
            return new ArrayList<>();
        }
    }

    private WebClient authorizedWebClient() {
        WebClient.Builder builder = webClientBuilder.build().mutate();
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null && !authorization.isBlank()) {
                builder.defaultHeader(HttpHeaders.AUTHORIZATION, authorization);
            }
            String userId = request.getHeader("X-User-Id");
            String userRole = request.getHeader("X-User-Role");
            if (userId != null && !userId.isBlank() && userRole != null && !userRole.isBlank()) {
                builder.defaultHeader("X-User-Id", userId);
                builder.defaultHeader("X-User-Role", userRole);
            }
        }
        return builder.build();
    }
}