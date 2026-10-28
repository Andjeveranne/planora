package com.isi4.planningservice.service;

import com.isi4.planningservice.algorithm.FordFulkersonScheduler;
import com.isi4.planningservice.dto.EmploiDuTempsDTO;
import com.isi4.planningservice.entity.EmploiDuTemps;
import com.isi4.planningservice.entity.UEEnseignant;
import com.isi4.planningservice.repository.DisponibiliteRepository;
import com.isi4.planningservice.repository.EmploiDuTempsRepository;
import com.isi4.planningservice.repository.UEClasseRepository;
import com.isi4.planningservice.repository.UEEnseignantRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmploiDuTempsService {

    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final UEClasseRepository ueClasseRepository;
    private final UEEnseignantRepository ueEnseignantRepository;
    private final DisponibiliteRepository disponibiliteRepository;
    private final DisponibiliteService disponibiliteService;
    private final FordFulkersonScheduler scheduler;
    private final WebClient.Builder webClientBuilder;

    // Créneaux de 2h de 8h à 18h
    private static final LocalTime[] HEURES_DEBUT = {
        LocalTime.of(8,  0), LocalTime.of(10, 0), LocalTime.of(12, 0),
        LocalTime.of(14, 0), LocalTime.of(16, 0)
    };
    private static final String[] LABELS_CRENEAU = {
        "8h-10h", "10h-12h", "12h-14h", "14h-16h", "16h-18h"
    };
    private static final int NB_JOURS = 5; // Lundi → Vendredi

    // -----------------------------------------------------------------------
    //  GÉNÉRATION — semaine à venir (calculée automatiquement)
    // -----------------------------------------------------------------------

    @Transactional
    public List<EmploiDuTempsDTO> genererEmploisDuTemps(String idEcole) {
        LocalDate lundi = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(1);
        int semaine = (int) lundi.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        log.info("Génération emplois du temps — semaine {}, école {}", semaine, idEcole);

        try {
            List<Map<String, Object>> classesList = getClassesData();
            List<Map<String, Object>> uesList      = getUEsData();
            Map<String, Map<String, Object>> sallesMap = getSallesMap(idEcole);

            Map<String, Map<String, Object>> classesMap = classesList.stream()
                    .collect(Collectors.toMap(c -> (String) c.get("idClasse"), c -> c));

            List<UEEnseignant> ueEnseignants = ueEnseignantRepository.findAll();

            List<FordFulkersonScheduler.TimeSlot> timeSlots = buildTimeSlots(semaine, lundi);
            List<FordFulkersonScheduler.SchedulingRequest> requests =
                    buildRequests(uesList, ueEnseignants, classesMap);
            List<FordFulkersonScheduler.Salle> salles = buildSalles(sallesMap);
            List<FordFulkersonScheduler.Disponibilite> disponibilites =
                    buildDisponibilites(ueEnseignants, lundi);

            List<FordFulkersonScheduler.SchedulingResult> results =
                    scheduler.generateSchedule(requests, timeSlots, salles, disponibilites);

            emploiDuTempsRepository.deleteBySemaine(semaine);
            List<EmploiDuTemps> saved = new ArrayList<>(saveResults(results, semaine));
            saved.addAll(fillWithTPE(saved, classesMap, semaine, lundi));

            Map<Long, Map<String, Object>> uesMap = uesList.stream()
                    .collect(Collectors.toMap(u -> ((Number) u.get("id")).longValue(), u -> u));
            return toDTO(saved, classesMap, sallesMap, uesMap);

        } catch (Exception e) {
            log.error("Erreur génération emplois du temps", e);
            throw new RuntimeException("Erreur de génération: " + e.getMessage(), e);
        }
    }

    // Pour compatibilité avec l'ancienne signature (semaine + idEcole)
    @Transactional
    public List<EmploiDuTempsDTO> genererEmploisDuTemps(Integer semaine, String idEcole) {
        return genererEmploisDuTemps(idEcole);
    }

    // -----------------------------------------------------------------------
    //  LECTURE
    // -----------------------------------------------------------------------

    public List<EmploiDuTempsDTO> getEmploiDuTempsClasse(String classeId, Integer semaine) {
        List<EmploiDuTemps> emplois = semaine != null
                ? emploiDuTempsRepository.findByClasseIdAndSemaine(classeId, semaine)
                : emploiDuTempsRepository.findByClasseIdOrderByDateHeureDebutAsc(classeId);
        return enrichAndConvert(emplois);
    }

    public List<EmploiDuTempsDTO> getEmploiDuTempsEnseignant(String enseignantId, Integer semaine) {
        List<EmploiDuTemps> emplois = semaine != null
                ? emploiDuTempsRepository.findByEnseignantIdAndSemaine(enseignantId, semaine)
                : emploiDuTempsRepository.findByEnseignantIdOrderByDateHeureDebutAsc(enseignantId);
        return enrichAndConvert(emplois);
    }

    @Transactional
    public void supprimerEmploisDuTemps(Integer semaine, String idEcole) {
        int n = emploiDuTempsRepository.deleteBySemaine(semaine);
        log.info("Supprimé {} emplois du temps pour la semaine {}", n, semaine);
    }

    /**
     * Retourne la liste des enseignants qui ont soumis des disponibilités,
     * avec leurs UEs déclarées.
     */
    public List<Map<String, Object>> getEnseignantsAvecDisponibilites() {
        List<String> enseignantIds = disponibiliteRepository.findDistinctEnseignantIds();
        List<UEEnseignant> allUE = ueEnseignantRepository.findAll();
        return enseignantIds.stream().map(id -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("enseignantId", id);
            entry.put("nombreDisponibilites", disponibiliteRepository.countByEnseignantId(id));
            List<Map<String, Object>> ues = allUE.stream()
                    .filter(ue -> ue.getEnseignantId().equals(id))
                    .map(ue -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("ueId", ue.getUeId());
                        m.put("peutCM", ue.getPeutCM());
                        m.put("peutTD", ue.getPeutTD());
                        m.put("peutTP", ue.getPeutTP());
                        return m;
                    }).collect(Collectors.toList());
            entry.put("ues", ues);
            return entry;
        }).collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    //  HELPERS INTERNES
    // -----------------------------------------------------------------------

    private List<FordFulkersonScheduler.TimeSlot> buildTimeSlots(int semaine, LocalDate lundi) {
        List<FordFulkersonScheduler.TimeSlot> slots = new ArrayList<>();
        for (int jour = 0; jour < NB_JOURS; jour++) {
            LocalDate day = lundi.plusDays(jour);
            for (int i = 0; i < HEURES_DEBUT.length; i++) {
                FordFulkersonScheduler.TimeSlot slot = new FordFulkersonScheduler.TimeSlot();
                slot.setDebut(LocalDateTime.of(day, HEURES_DEBUT[i]));
                slot.setFin(slot.getDebut().plusHours(2));
                slot.setJourSemaine(jour + 1);
                slot.setCreneau(LABELS_CRENEAU[i]);
                slot.setSemaine(semaine);
                slots.add(slot);
            }
        }
        return slots;
    }

    private List<FordFulkersonScheduler.SchedulingRequest> buildRequests(
            List<Map<String, Object>> uesList,
            List<UEEnseignant> ueEnseignants,
            Map<String, Map<String, Object>> classesMap) {

        List<FordFulkersonScheduler.SchedulingRequest> requests = new ArrayList<>();

        for (Map<String, Object> ue : uesList) {
            Long ueId      = ((Number) ue.get("id")).longValue();
            String filiere = (String) ue.get("idFiliere");
            String niveau  = (String) ue.get("niveau");
            if (filiere == null || niveau == null) continue;

            // Classes qui correspondent à cette filière + niveau
            List<Map<String, Object>> classes = classesMap.values().stream()
                    .filter(c -> filiere.equals(c.get("idFiliere")) && niveau.equals(c.get("niveau")))
                    .collect(Collectors.toList());
            if (classes.isEmpty()) continue;

            List<UEEnseignant> qualifies = ueEnseignants.stream()
                    .filter(e -> e.getUeId().equals(ueId))
                    .collect(Collectors.toList());

            int hCM = toInt(ue.get("heuresCM"));
            int hTD = toInt(ue.get("heuresTD"));
            int hTP = toInt(ue.get("heuresTP"));

            for (Map<String, Object> classe : classes) {
                String classeId = (String) classe.get("idClasse");
                int effectif    = toInt(classe.get("effectifs"));

                if (hCM > 0) buildRequest(requests, qualifies, classeId, ueId, "CM", hCM, effectif, UEEnseignant::getPeutCM);
                if (hTD > 0) buildRequest(requests, qualifies, classeId, ueId, "TD", hTD, effectif, UEEnseignant::getPeutTD);
                if (hTP > 0) buildRequest(requests, qualifies, classeId, ueId, "TP", hTP, effectif, UEEnseignant::getPeutTP);
            }
        }
        return requests;
    }

    @FunctionalInterface
    interface BooleanGetter { Boolean get(UEEnseignant e); }

    private void buildRequest(
            List<FordFulkersonScheduler.SchedulingRequest> requests,
            List<UEEnseignant> qualifies,
            String classeId, Long ueId, String type, int heures, int effectif,
            BooleanGetter canTeach) {
        qualifies.stream()
                .filter(e -> Boolean.TRUE.equals(canTeach.get(e)))
                .findFirst()
                .ifPresent(e -> {
                    FordFulkersonScheduler.SchedulingRequest req = new FordFulkersonScheduler.SchedulingRequest();
                    req.setClasseId(classeId);
                    req.setUeId(ueId);
                    req.setEnseignantId(e.getEnseignantId());
                    req.setTypeCours(type);
                    req.setHeuresRequises((heures + 1) / 2); // arrondi en blocs 2h
                    req.setEffectifClasse(effectif);
                    requests.add(req);
                });
    }

    private List<FordFulkersonScheduler.Salle> buildSalles(Map<String, Map<String, Object>> sallesMap) {
        return sallesMap.values().stream().map(s -> {
            FordFulkersonScheduler.Salle salle = new FordFulkersonScheduler.Salle();
            salle.setId((String) s.get("idSalle"));
            salle.setNom((String) s.get("nomSalle"));
            salle.setCapacite(toInt(s.get("capacite")));
            salle.setType((String) s.get("type"));
            return salle;
        }).collect(Collectors.toList());
    }

    private List<FordFulkersonScheduler.Disponibilite> buildDisponibilites(
            List<UEEnseignant> ueEnseignants, LocalDate lundi) {
        LocalDateTime debut = lundi.atStartOfDay();
        LocalDateTime fin   = lundi.plusDays(5).atStartOfDay();
        Set<String> ids = ueEnseignants.stream().map(UEEnseignant::getEnseignantId).collect(Collectors.toSet());
        List<FordFulkersonScheduler.Disponibilite> result = new ArrayList<>();
        for (String id : ids) {
            try {
                disponibiliteService.getDisponibilitesByEnseignant(id).stream()
                        .filter(d -> !d.getDateHeureDebut().isBefore(debut) && d.getDateHeureDebut().isBefore(fin))
                        .forEach(d -> {
                            FordFulkersonScheduler.Disponibilite disp = new FordFulkersonScheduler.Disponibilite();
                            disp.setEnseignantId(id);
                            disp.setDebut(d.getDateHeureDebut());
                            disp.setFin(d.getDateHeureFin());
                            result.add(disp);
                        });
            } catch (Exception ex) {
                log.warn("Dispo non trouvée pour enseignant {}: {}", id, ex.getMessage());
            }
        }
        return result;
    }

    private List<EmploiDuTemps> saveResults(List<FordFulkersonScheduler.SchedulingResult> results, int semaine) {
        return emploiDuTempsRepository.saveAll(results.stream().map(r -> {
            EmploiDuTemps e = new EmploiDuTemps();
            e.setClasseId(r.getClasseId());
            e.setEnseignantId(r.getEnseignantId());
            e.setUeId(r.getUeId());
            e.setSalleId(r.getSalleId());
            e.setDateHeureDebut(r.getDateHeureDebut());
            e.setDateHeureFin(r.getDateHeureFin());
            e.setTypeCours(r.getTypeCours());
            e.setSemaine(semaine);
            e.setJourSemaine(r.getJourSemaine());
            e.setCreneau(r.getCreneau());
            return e;
        }).collect(Collectors.toList()));
    }

    /** Remplit les créneaux libres de chaque classe avec "TPE" */
    private List<EmploiDuTemps> fillWithTPE(
            List<EmploiDuTemps> scheduled,
            Map<String, Map<String, Object>> classesMap,
            int semaine, LocalDate lundi) {

        // Créneaux déjà occupés par classe
        Map<String, Set<String>> occupes = new HashMap<>();
        for (EmploiDuTemps e : scheduled) {
            occupes.computeIfAbsent(e.getClasseId(), k -> new HashSet<>())
                    .add(e.getJourSemaine() + "_" + e.getCreneau());
        }

        List<EmploiDuTemps> tpe = new ArrayList<>();
        for (String classeId : classesMap.keySet()) {
            Set<String> pris = occupes.getOrDefault(classeId, Collections.emptySet());
            for (int jour = 1; jour <= NB_JOURS; jour++) {
                LocalDate day = lundi.plusDays(jour - 1);
                for (int i = 0; i < HEURES_DEBUT.length; i++) {
                    if (!pris.contains(jour + "_" + LABELS_CRENEAU[i])) {
                        EmploiDuTemps t = new EmploiDuTemps();
                        t.setClasseId(classeId);
                        t.setTypeCours("TPE");
                        t.setDateHeureDebut(LocalDateTime.of(day, HEURES_DEBUT[i]));
                        t.setDateHeureFin(LocalDateTime.of(day, HEURES_DEBUT[i].plusHours(2)));
                        t.setSemaine(semaine);
                        t.setJourSemaine(jour);
                        t.setCreneau(LABELS_CRENEAU[i]);
                        tpe.add(t);
                    }
                }
            }
        }
        return tpe.isEmpty() ? Collections.emptyList() : emploiDuTempsRepository.saveAll(tpe);
    }

    // -----------------------------------------------------------------------
    //  CONVERSION DTO
    // -----------------------------------------------------------------------

    private List<EmploiDuTempsDTO> enrichAndConvert(List<EmploiDuTemps> emplois) {
        Map<String, Map<String, Object>> classesMap = getClassesData().stream()
                .collect(Collectors.toMap(c -> (String) c.get("idClasse"), c -> c));
        Map<String, Map<String, Object>> sallesMap = getSallesMap(null);
        Map<Long, Map<String, Object>> uesMap = getUEsData().stream()
                .collect(Collectors.toMap(u -> ((Number) u.get("id")).longValue(), u -> u));
        return toDTO(emplois, classesMap, sallesMap, uesMap);
    }

    private List<EmploiDuTempsDTO> toDTO(
            List<EmploiDuTemps> emplois,
            Map<String, Map<String, Object>> classesMap,
            Map<String, Map<String, Object>> sallesMap,
            Map<Long, Map<String, Object>> uesMap) {

        return emplois.stream().map(e -> {
            EmploiDuTempsDTO dto = new EmploiDuTempsDTO();
            dto.setId(e.getId());
            dto.setClasseId(e.getClasseId());
            dto.setEnseignantId(e.getEnseignantId());
            dto.setUeId(e.getUeId());
            dto.setSalleId(e.getSalleId());
            dto.setDateHeureDebut(e.getDateHeureDebut());
            dto.setDateHeureFin(e.getDateHeureFin());
            dto.setTypeCours(e.getTypeCours());
            dto.setSemaine(e.getSemaine());
            dto.setJourSemaine(e.getJourSemaine());
            dto.setCreneau(e.getCreneau());

            Optional.ofNullable(classesMap.get(e.getClasseId()))
                    .ifPresent(c -> {
                        dto.setNomClasse((String) c.get("nomClasse"));
                        dto.setIdEcole((String) c.get("idEcole"));
                    });
            if (e.getSalleId() != null)
                Optional.ofNullable(sallesMap.get(e.getSalleId()))
                        .ifPresent(s -> dto.setNomSalle((String) s.get("nomSalle")));
            if (e.getUeId() != null)
                Optional.ofNullable(uesMap.get(e.getUeId()))
                        .ifPresent(u -> {
                            dto.setCodeUE((String) u.get("codeUE"));
                            dto.setNomUE((String) u.get("nomUE"));
                        });
            return dto;
        }).collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    //  APPELS SERVICES EXTERNES
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getClassesData() {
        try {
            List<Map<String, Object>> data = authorizedWebClient().get()
                    .uri("http://classes-service/api/classes")
                    .retrieve().bodyToMono(List.class).block();
            return data != null ? data : Collections.emptyList();
        } catch (Exception e) {
            log.error("Erreur classes: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getUEsData() {
        try {
            List<Map<String, Object>> data = authorizedWebClient().get()
                    .uri("http://ue-service/api/ues")
                    .retrieve().bodyToMono(List.class).block();
            return data != null ? data : Collections.emptyList();
        } catch (Exception e) {
            log.error("Erreur UEs: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> getSallesMap(String idEcole) {
        try {
            String uri = (idEcole != null && !idEcole.isBlank())
                    ? "http://salle-service/api/salles/ecole/" + idEcole
                    : "http://salle-service/api/salles";
            List<Map<String, Object>> data = authorizedWebClient().get()
                    .uri(uri).retrieve().bodyToMono(List.class).block();
            if (data == null) return Collections.emptyMap();
            return data.stream()
                    .filter(s -> !"INDISPONIBLE".equals(s.get("statut")))
                    .collect(Collectors.toMap(s -> (String) s.get("idSalle"), s -> s));
        } catch (Exception e) {
            log.error("Erreur salles: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private int toInt(Object val) {
        return val instanceof Number n ? n.intValue() : 0;
    }

    // -----------------------------------------------------------------------
    //  WEBCLIENT WITH JWT FORWARDING
    // -----------------------------------------------------------------------

    private WebClient authorizedWebClient() {
        WebClient.Builder builder = webClientBuilder.build().mutate();

        String requestAuth = null;
        String requestUserId = null;
        String requestUserRole = null;

        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            requestAuth = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (requestAuth != null && !requestAuth.isBlank()) {
                builder.defaultHeader(HttpHeaders.AUTHORIZATION, requestAuth);
            }
            requestUserId = request.getHeader("X-User-Id");
            requestUserRole = request.getHeader("X-User-Role");
        }

        if (requestUserId == null || requestUserId.isBlank()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                requestUserId = authentication.getName();
            }
        }

        if (requestUserRole == null || requestUserRole.isBlank()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getAuthorities() != null) {
                requestUserRole = authentication.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .filter(authority -> authority != null && !authority.isBlank())
                        .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                        .findFirst()
                        .orElse(null);
            }
        }

        if (requestUserId != null && !requestUserId.isBlank()) {
            builder.defaultHeader("X-User-Id", requestUserId);
        }
        if (requestUserRole != null && !requestUserRole.isBlank()) {
            builder.defaultHeader("X-User-Role", requestUserRole);
        }

        return builder.build();
    }
}