package com.isi4.planningservice.service;

import com.isi4.planningservice.algorithm.FordFulkersonScheduler;
import com.isi4.planningservice.dto.DisponibiliteDTO;
import com.isi4.planningservice.dto.EmploiDuTempsDTO;
import com.isi4.planningservice.entity.EmploiDuTemps;
import com.isi4.planningservice.repository.EmploiDuTempsRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmploiDuTempsServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(EmploiDuTempsServiceV2.class);

    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final DisponibiliteService disponibiliteService;
    private final FordFulkersonScheduler scheduler;
    private final WebClient.Builder webClientBuilder;

    public EmploiDuTempsServiceV2(
            EmploiDuTempsRepository emploiDuTempsRepository,
            DisponibiliteService disponibiliteService,
            FordFulkersonScheduler scheduler,
            WebClient.Builder webClientBuilder) {
        this.emploiDuTempsRepository = emploiDuTempsRepository;
        this.disponibiliteService = disponibiliteService;
        this.scheduler = scheduler;
        this.webClientBuilder = webClientBuilder;
    }

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

            Map<String, Object> classesData = getClassesData(idEcole, departement, specialite);
            Map<String, Object> sallesData = getSallesData(idEcole);
            Map<String, Object> uesData = getUEsData();
            Map<String, Object> enseignantsData = getEnseignantsData(idEcole, departement, specialite);

            if (classesData.isEmpty()) {
                throw new IllegalStateException(idEcole == null || idEcole.isBlank()
                        ? "Aucune classe n'est configurée."
                        : "Aucune classe ne correspond à cette école, ce département et cette spécialité.");
            }
            if (sallesData.isEmpty()) {
                throw new IllegalStateException(idEcole == null || idEcole.isBlank()
                        ? "Aucune salle disponible n'est configurée."
                        : "Aucune salle disponible n'est configurée pour cette école.");
            }

            List<FordFulkersonScheduler.TimeSlot> timeSlots = generateTimeSlots(debutSemaineProchaine);

            List<FordFulkersonScheduler.SchedulingRequest> requests = prepareSchedulingRequestsFromUEs(
                    classesData, uesData, enseignantsData);
            if (requests.isEmpty()) {
                throw new IllegalStateException(
                        "Aucun cours planifiable: vérifiez les UE correspondant au niveau, au département et à la spécialité des classes, " +
                                "puis affectez au moins un enseignant qualifié à chaque UE.");
            }

            List<FordFulkersonScheduler.Salle> salles = prepareSalles(sallesData);
            List<FordFulkersonScheduler.Disponibilite> disponibilites = getEnseignantDisponibilites(
                    enseignantsData, idEcole, debutSemaineProchaine);
            log.info("Données de planification: {} classes, {} demandes, {} salles, {} disponibilités, {} enseignants",
                    classesData.size(), requests.size(), salles.size(), disponibilites.size(), enseignantsData.size());
            if (disponibilites.isEmpty()) {
                throw new IllegalStateException("Aucune disponibilité n'est enregistrée pour la semaine du "
                        + debutSemaineProchaine.toLocalDate() + ". Les enseignants doivent renseigner leurs créneaux pour cette semaine.");
            }

            List<FordFulkersonScheduler.SchedulingResult> results = scheduler.generateSchedule(
                    requests, timeSlots, salles, disponibilites);

            long coursPlanifies = results.stream()
                    .filter(result -> !"TPE".equals(result.getTypeCours()))
                    .count();
            if (coursPlanifies == 0) {
                throw new IllegalStateException(
                        "Aucun cours n'a pu être placé: vérifiez les disponibilités des enseignants, les capacités et les types des salles, " +
                                "ainsi que leur attribution aux classes sélectionnées.");
            }

            results = completerAvecTPE(results, timeSlots, classesData, semaineProchaine);

            emploiDuTempsRepository.deleteBySemaineAndClasseIds(semaineProchaine,
                    new ArrayList<>(classesData.keySet()));
            List<EmploiDuTemps> emplois = saveSchedulingResults(results);
            scheduleTeacherNotifications(emplois, enseignantsData);

            return convertToDTO(emplois, classesData, sallesData, uesData, enseignantsData);

        } catch (Exception e) {
            log.error("Erreur lors de la génération des emplois du temps", e);
            throw new RuntimeException("Erreur lors de la génération des emplois du temps: " + e.getMessage());
        }
    }

    private List<FordFulkersonScheduler.TimeSlot> generateTimeSlots(LocalDateTime debutSemaine) {
        List<FordFulkersonScheduler.TimeSlot> slots = new ArrayList<>();

        LocalTime[] heuresDebut = {
                LocalTime.of(8, 0), LocalTime.of(10, 0), LocalTime.of(12, 0),
                LocalTime.of(14, 0), LocalTime.of(16, 0)
        };

        String[] creneaux = {"8h-10h", "10h-12h", "12h-14h", "14h-16h", "16h-18h"};

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
    private Map<String, Object> getClassesData(String idEcole, String departement, String specialite) {
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
                    .filter(classe -> matchesGenerationScope(classe, idEcole, departement, specialite))
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

    static boolean matchesGenerationScope(
            Map<String, Object> classe,
            String idEcole,
            String departement,
            String specialite) {
        if (classe == null) {
            return false;
        }

        String classSchoolId = safeString(classe.get("idEcole"));
        String classDepartmentId = safeString(classe.get("idDepartement"));
        String classSpecialityId = safeString(classe.get("idSpecialite"));
        String classLevelId = safeString(classe.get("idNiveau"));

        String requestedDepartmentId = "";
        String requestedSpecialityId = "";
        String requestedLevelId = "";

        if (departement != null && !departement.isBlank() && isUuidLike(departement)) {
            requestedDepartmentId = departement;
        }
        if (specialite != null && !specialite.isBlank() && isUuidLike(specialite)) {
            requestedSpecialityId = specialite;
        }
        if (idEcole != null && isUuidLike(idEcole)) {
            classSchoolId = !classSchoolId.isBlank() ? classSchoolId : null;
        }

        if (!requestedDepartmentId.isBlank() && !classDepartmentId.isBlank() && !requestedDepartmentId.equals(classDepartmentId)) {
            return false;
        }
        if (!requestedSpecialityId.isBlank() && !classSpecialityId.isBlank() && !requestedSpecialityId.equals(classSpecialityId)) {
            return false;
        }
        if (!requestedLevelId.isBlank() && !classLevelId.isBlank() && !requestedLevelId.equals(classLevelId)) {
            return false;
        }

        if (idEcole != null && !idEcole.isBlank() && !idEcole.equals(classSchoolId)) {
            return false;
        }

        String classDepartment = normalizeScopeValue((String) classe.get("nomFiliere"));
        String classSpecialite = normalizeScopeValue((String) classe.get("specialite"));
        String requestedDepartment = normalizeScopeValue(departement);
        String requestedSpecialite = normalizeScopeValue(specialite);

        boolean matchesDepartment = requestedDepartment.isBlank()
                || sameScope(requestedDepartment, classDepartment)
                || sameScope(requestedDepartment, classSpecialite)
                || (!classDepartment.isBlank() && sameScope(classDepartment, requestedSpecialite));

        boolean matchesSpecialite = requestedSpecialite.isBlank()
                || sameScope(requestedSpecialite, classSpecialite)
                || sameScope(requestedSpecialite, classDepartment)
                || (!classSpecialite.isBlank() && sameScope(requestedDepartment, classDepartment))
                || (!classSpecialite.isBlank() && sameScope(requestedDepartment, classSpecialite));

        return matchesDepartment && matchesSpecialite;
    }

    private static String safeString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static boolean isUuidLike(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.matches("^[0-9a-fA-F-]{36}$") || value.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    private static boolean sameScope(String left, String right) {
        if (left == null || left.isBlank() || right == null || right.isBlank()) {
            return false;
        }
        Set<String> leftAliases = scopeAliases(left);
        Set<String> rightAliases = scopeAliases(right);
        return !Collections.disjoint(leftAliases, rightAliases);
    }

    private static Set<String> scopeAliases(String value) {
        String normalized = normalizeScopeValue(value);
        if (normalized.isBlank()) {
            return Collections.emptySet();
        }

        Set<String> aliases = new HashSet<>();
        aliases.add(normalized);

        switch (normalized) {
            case "software engineering", "genie logiciel", "génie logiciel", "engineering software" -> {
                aliases.add("engineering");
                aliases.add("ingenierie");
                aliases.add("ingénierie");
                aliases.add("genie logiciel");
                aliases.add("génie logiciel");
                aliases.add("software engineering");
            }
            case "engineering", "ingenierie", "ingénierie" -> {
                aliases.add("software engineering");
                aliases.add("genie logiciel");
                aliases.add("génie logiciel");
                aliases.add("engineering");
                aliases.add("ingenierie");
                aliases.add("ingénierie");
            }
            case "informatique", "informatics" -> {
                aliases.add("informatique");
                aliases.add("informatics");
            }
            case "civil engineering", "genie civil", "génie civil" -> {
                aliases.add("civil engineering");
                aliases.add("genie civil");
                aliases.add("génie civil");
            }
            default -> {
            }
        }

        return aliases;
    }

    private static String normalizeScopeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
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

            for (Map.Entry<String, Object> ueEntry : uesData.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> ueInfo = (Map<String, Object>) ueEntry.getValue();

                String ueNiveau = (String) ueInfo.get("niveau");
                String ueFiliere = (String) ueInfo.get("idFiliere");
                String ueEcole = (String) ueInfo.get("idEcole");
                String ueSpecialite = (String) ueInfo.get("specialite");
                String ueDepartment = normalizeScopeValue((String) ueInfo.get("nomFiliere"));
                String classeDepartment = normalizeScopeValue((String) classeInfo.get("nomFiliere"));
                String classeSpecialite = normalizeScopeValue((String) classeInfo.get("specialite"));
                LocalTime heureDebut = parseTime(ueInfo.get("heureDebut"), LocalTime.of(8, 0));
                LocalTime heureFin = parseTime(ueInfo.get("heureFin"), LocalTime.of(18, 0));
                boolean matchingSpecialite = ueSpecialite == null || ueSpecialite.isBlank()
                        || sameScope(normalizeScopeValue(ueSpecialite), classeSpecialite)
                        || sameScope(normalizeScopeValue(ueSpecialite), classeDepartment)
                        || (classeSpecialite.isBlank() && !ueDepartment.isBlank() && sameScope(ueDepartment, classeDepartment));
                if (ueNiveau != null && ueFiliere != null
                        && niveauxCompatibles(niveau, ueNiveau) && filiereId.equals(ueFiliere)
                        && (ueEcole == null || ueEcole.isBlank() || ueEcole.equals(ecoleId))
                        && matchingSpecialite) {
                    Long ueId = Long.valueOf(ueEntry.getKey());
                    Map<String, Object> enseignantsUE = getEnseignantsForUE(ueId, enseignantsData.keySet(), ecoleId);
                    createRequestsForUE(requests, classeId, ecoleId, ueId, ueInfo, enseignantsUE, effectif, heureDebut, heureFin);
                }
            }
        }

        return requests;
    }

    static boolean niveauxCompatibles(String niveauClasse, String niveauUE) {
        return niveauClasse != null && niveauUE != null
                && normaliserNiveau(niveauClasse).equals(normaliserNiveau(niveauUE));
    }

    private static String normaliserNiveau(String niveau) {
        String normalized = niveau.trim().toUpperCase(Locale.ROOT);
        if (normalized.matches("L[1-5]")) {
            return "ING" + normalized.substring(1);
        }
        if (normalized.matches("ING[1-5]")) {
            return normalized;
        }
        if (normalized.matches("MASTER[12]")) {
            return "M" + normalized.substring("MASTER".length());
        }
        return normalized;
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
                            ens -> String.valueOf(ens.get("enseignantId")),
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
            LocalTime heureDebut,
            LocalTime heureFin) {

        Integer heuresCM = (Integer) ueInfo.get("heuresCM");
        Integer heuresTD = (Integer) ueInfo.get("heuresTD");
        Integer heuresTP = (Integer) ueInfo.get("heuresTP");

        if (heuresCM != null && heuresCM > 0) {
            String enseignantCM = findEnseignantForType(enseignantsUE, "CM");
            if (enseignantCM != null) {
                requests.add(createRequest(classeId, ecoleId, ueId, enseignantCM, "CM", heuresCM, effectif, heureDebut, heureFin));
            }
        }

        if (heuresTD != null && heuresTD > 0) {
            String enseignantTD = findEnseignantForType(enseignantsUE, "TD");
            if (enseignantTD != null) {
                requests.add(createRequest(classeId, ecoleId, ueId, enseignantTD, "TD", heuresTD, effectif, heureDebut, heureFin));
            }
        }

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
            LocalTime heureDebut, LocalTime heureFin) {

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

    private LocalTime parseTime(Object value, LocalTime fallback) {
        if (value == null) return fallback;
        try { return LocalTime.parse(String.valueOf(value)); }
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
                    s.setIdClasse((String) salle.get("idClasse"));
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
                            LocalDateTime debut = dispo.getDateHeureDebut();
                            return !debut.isBefore(debutSemaine) && debut.isBefore(debutSemaine.plusWeeks(1));
                        })
                        .forEach(dispo -> {
                            FordFulkersonScheduler.Disponibilite d = new FordFulkersonScheduler.Disponibilite();
                            d.setEnseignantId(enseignantId);
                            d.setDebut(dispo.getDateHeureDebut());
                            d.setFin(dispo.getDateHeureFin());
                            disponibilites.add(d);
                        });
            } catch (Exception e) {
                log.error("Impossible de récupérer les disponibilités de l'enseignant {} pour l'école {}",
                        enseignantId, idEcole, e);
                throw new IllegalStateException(
                        "Impossible de récupérer les disponibilités de l'enseignant " + enseignantId + ": " + e.getMessage(), e);
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

        for (String classeId : classesData.keySet()) {
            for (FordFulkersonScheduler.TimeSlot slot : timeSlots) {
                boolean creneauOccupe = results.stream()
                        .anyMatch(result -> result.getClasseId().equals(classeId)
                                && result.getDateHeureDebut().equals(slot.getDebut()));

                if (!creneauOccupe) {
                    FordFulkersonScheduler.SchedulingResult tpe = new FordFulkersonScheduler.SchedulingResult();
                    tpe.setClasseId(classeId);
                    tpe.setEnseignantId(null);
                    tpe.setUeId(null);
                    tpe.setSalleId(null);
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

    List<EmploiDuTemps> saveSchedulingResults(List<FordFulkersonScheduler.SchedulingResult> results) {
        List<EmploiDuTemps> emplois = results.stream()
                .map(result -> {
                    EmploiDuTemps emploi = new EmploiDuTemps();
                    emploi.setClasseId(result.getClasseId());
                    emploi.setClasse(result.getClasseId());
                    emploi.setEnseignantId(result.getEnseignantId());
                    emploi.setUeId(result.getUeId());
                    emploi.setUe(result.getUeId() == null ? result.getTypeCours() : result.getUeId().toString());
                    emploi.setSalleId(result.getSalleId());
                    emploi.setSalle(result.getSalleId() == null ? result.getTypeCours() : result.getSalleId());
                    emploi.setDateHeureDebut(result.getDateHeureDebut());
                    emploi.setDateHeureFin(result.getDateHeureFin());
                    emploi.setDateJour(result.getDateHeureDebut().toLocalDate());
                    emploi.setJour(result.getDateHeureDebut().getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, Locale.FRENCH));
                    emploi.setHeureDebut(result.getDateHeureDebut().getHour());
                    emploi.setHeureFin(result.getDateHeureFin().getHour());
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

                    if (!"TPE".equals(emploi.getTypeCours())) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> classeInfo = (Map<String, Object>) classesData.get(emploi.getClasseId());
                        if (classeInfo != null) {
                            dto.setNomClasse((String) classeInfo.get("nomClasse"));
                            dto.setIdEcole((String) classeInfo.get("idEcole"));
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
                            Object nom = enseignantInfo.get("nomUser");
                            Object prenom = enseignantInfo.get("prenomUser");
                            dto.setNomEnseignant((nom == null ? "" : nom) + " " + (prenom == null ? "" : prenom));
                        }
                    } else {
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
            @SuppressWarnings("unchecked")
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
        return getEnseignantsAvecDisponibilitesDetailles(idEcole, departement, null);
    }

    public List<Map<String, Object>> getEnseignantsAvecDisponibilitesDetailles(String idEcole, String departement, String specialite) {
        try {
            Map<String, Object> enseignantsData = getEnseignantsData(idEcole, departement, specialite);
            if (enseignantsData == null || enseignantsData.isEmpty()) {
                return new ArrayList<>();
            }

            List<Map<String, Object>> result = new ArrayList<>();
            Set<String> enseignantIds = new HashSet<>(enseignantsData.keySet());

            for (String enseignantId : enseignantIds) {
                Map<String, Object> enseignantInfo = null;
                Object raw = enseignantsData.get(enseignantId);
                if (raw instanceof Map<?, ?> casted) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> candidate = (Map<String, Object>) casted;
                    enseignantInfo = candidate;
                }

                if (enseignantInfo == null) {
                    continue;
                }

                Map<String, Object> enseignantDetail = new HashMap<>();
                String nom = safeString(enseignantInfo.get("nomUser"));
                String prenom = safeString(enseignantInfo.get("prenomUser"));
                String email = safeString(enseignantInfo.get("email"));
                String idEcoleUser = safeString(enseignantInfo.get("idEcole"));
                String departementUser = safeString(enseignantInfo.get("departement"));
                String specialiteUser = safeString(enseignantInfo.get("specialite"));

                enseignantDetail.put("enseignantId", enseignantId);
                enseignantDetail.put("idUser", enseignantId);
                enseignantDetail.put("nom", nom);
                enseignantDetail.put("nomUser", nom);
                enseignantDetail.put("prenom", prenom);
                enseignantDetail.put("prenomUser", prenom);
                enseignantDetail.put("email", email);
                enseignantDetail.put("departement", departementUser);
                enseignantDetail.put("specialite", specialiteUser);
                enseignantDetail.put("idEcole", idEcoleUser);

                List<DisponibiliteDTO> disponibilites = disponibiliteService.getDisponibilitesByEnseignant(enseignantId);
                int nombreDisponibilites = disponibilites == null ? 0 : disponibilites.size();
                enseignantDetail.put("nombreDisponibilites", nombreDisponibilites);

                result.add(enseignantDetail);
            }

            result.sort((a, b) -> {
                String aName = String.valueOf(a.getOrDefault("prenom", "")) + " " + String.valueOf(a.getOrDefault("nom", ""));
                String bName = String.valueOf(b.getOrDefault("prenom", "")) + " " + String.valueOf(b.getOrDefault("nom", ""));
                return aName.compareToIgnoreCase(bName);
            });

            return result;
        } catch (Exception e) {
            log.warn("Impossible de récupérer la liste des enseignants disponibles. Retour vide pour l'interface.", e);
            return new ArrayList<>();
        }
    }

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

