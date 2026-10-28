package com.isi4.planningservice.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class FordFulkersonScheduler {

    public static class SchedulingRequest {
        private String classeId;
        private String idEcole;
        private Long ueId;
        private String enseignantId;
        private String typeCours;
        private Integer heuresRequises;
        private Integer effectifClasse;
        private LocalTime heureDebut;
        private LocalTime heureFin;

        public String getClasseId() { return classeId; }
        public void setClasseId(String classeId) { this.classeId = classeId; }
        public String getIdEcole() { return idEcole; }
        public void setIdEcole(String idEcole) { this.idEcole = idEcole; }
        public Long getUeId() { return ueId; }
        public void setUeId(Long ueId) { this.ueId = ueId; }
        public String getEnseignantId() { return enseignantId; }
        public void setEnseignantId(String enseignantId) { this.enseignantId = enseignantId; }
        public String getTypeCours() { return typeCours; }
        public void setTypeCours(String typeCours) { this.typeCours = typeCours; }
        public Integer getHeuresRequises() { return heuresRequises; }
        public void setHeuresRequises(Integer heuresRequises) { this.heuresRequises = heuresRequises; }
        public Integer getEffectifClasse() { return effectifClasse; }
        public void setEffectifClasse(Integer effectifClasse) { this.effectifClasse = effectifClasse; }
        public LocalTime getHeureDebut() { return heureDebut; }
        public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
        public LocalTime getHeureFin() { return heureFin; }
        public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
    }

    public static class TimeSlot {
        private LocalDateTime debut;
        private LocalDateTime fin;
        private Integer jourSemaine;
        private String creneau;
        private Integer semaine;

        public LocalDateTime getDebut() { return debut; }
        public void setDebut(LocalDateTime debut) { this.debut = debut; }
        public LocalDateTime getFin() { return fin; }
        public void setFin(LocalDateTime fin) { this.fin = fin; }
        public Integer getJourSemaine() { return jourSemaine; }
        public void setJourSemaine(Integer jourSemaine) { this.jourSemaine = jourSemaine; }
        public String getCreneau() { return creneau; }
        public void setCreneau(String creneau) { this.creneau = creneau; }
        public Integer getSemaine() { return semaine; }
        public void setSemaine(Integer semaine) { this.semaine = semaine; }
    }

    public static class Salle {
        private String id;
        private String idEcole;
        private String idClasse;
        private String nom;
        private Integer capacite;
        private String type;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getIdEcole() { return idEcole; }
        public void setIdEcole(String idEcole) { this.idEcole = idEcole; }
        public String getIdClasse() { return idClasse; }
        public void setIdClasse(String idClasse) { this.idClasse = idClasse; }
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public Integer getCapacite() { return capacite; }
        public void setCapacite(Integer capacite) { this.capacite = capacite; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class Disponibilite {
        private String enseignantId;
        private LocalDateTime debut;
        private LocalDateTime fin;

        public String getEnseignantId() { return enseignantId; }
        public void setEnseignantId(String enseignantId) { this.enseignantId = enseignantId; }
        public LocalDateTime getDebut() { return debut; }
        public void setDebut(LocalDateTime debut) { this.debut = debut; }
        public LocalDateTime getFin() { return fin; }
        public void setFin(LocalDateTime fin) { this.fin = fin; }
    }

    public static class SchedulingResult {
        private String classeId;
        private String enseignantId;
        private Long ueId;
        private String salleId;
        private LocalDateTime dateHeureDebut;
        private LocalDateTime dateHeureFin;
        private String typeCours;
        private Integer semaine;
        private Integer jourSemaine;
        private String creneau;

        public String getClasseId() { return classeId; }
        public void setClasseId(String classeId) { this.classeId = classeId; }
        public String getEnseignantId() { return enseignantId; }
        public void setEnseignantId(String enseignantId) { this.enseignantId = enseignantId; }
        public Long getUeId() { return ueId; }
        public void setUeId(Long ueId) { this.ueId = ueId; }
        public String getSalleId() { return salleId; }
        public void setSalleId(String salleId) { this.salleId = salleId; }
        public LocalDateTime getDateHeureDebut() { return dateHeureDebut; }
        public void setDateHeureDebut(LocalDateTime dateHeureDebut) { this.dateHeureDebut = dateHeureDebut; }
        public LocalDateTime getDateHeureFin() { return dateHeureFin; }
        public void setDateHeureFin(LocalDateTime dateHeureFin) { this.dateHeureFin = dateHeureFin; }
        public String getTypeCours() { return typeCours; }
        public void setTypeCours(String typeCours) { this.typeCours = typeCours; }
        public Integer getSemaine() { return semaine; }
        public void setSemaine(Integer semaine) { this.semaine = semaine; }
        public Integer getJourSemaine() { return jourSemaine; }
        public void setJourSemaine(Integer jourSemaine) { this.jourSemaine = jourSemaine; }
        public String getCreneau() { return creneau; }
        public void setCreneau(String creneau) { this.creneau = creneau; }
    }

    // Graphe pour l'algorithme Ford-Fulkerson
    private static class FlowNetwork {
        private final Map<String, Map<String, Integer>> capacity = new HashMap<>();
        private final Map<String, Map<String, Integer>> flow = new HashMap<>();
        private final Set<String> nodes = new HashSet<>();

        public void addEdge(String from, String to, int cap) {
            nodes.add(from);
            nodes.add(to);
            capacity.computeIfAbsent(from, k -> new HashMap<>()).put(to, cap);
            capacity.computeIfAbsent(to, k -> new HashMap<>()).putIfAbsent(from, 0);
            flow.computeIfAbsent(from, k -> new HashMap<>()).putIfAbsent(to, 0);
            flow.computeIfAbsent(to, k -> new HashMap<>()).putIfAbsent(from, 0);
        }

        public int getResidualCapacity(String from, String to) {
            return capacity.getOrDefault(from, new HashMap<>()).getOrDefault(to, 0) -
                   flow.getOrDefault(from, new HashMap<>()).getOrDefault(to, 0);
        }

        public void addFlow(String from, String to, int flowValue) {
            flow.computeIfAbsent(from, k -> new HashMap<>()).merge(to, flowValue, (a, b) -> a + b);
            flow.computeIfAbsent(to, k -> new HashMap<>()).merge(from, -flowValue, (a, b) -> a + b);
        }

        public Set<String> getNeighbors(String node) {
            return capacity.getOrDefault(node, new HashMap<>()).keySet();
        }
    }

    public List<SchedulingResult> generateSchedule(
            List<SchedulingRequest> requests,
            List<TimeSlot> availableSlots,
            List<Salle> salles,
            List<Disponibilite> disponibilites) {

        log.info("Génération d'emploi du temps avec {} demandes, {} créneaux, {} salles", 
                requests.size(), availableSlots.size(), salles.size());

        List<SchedulingResult> results = new ArrayList<>();
        
        // Pour chaque demande de cours
        for (SchedulingRequest request : requests) {
            log.info("Traitement de la demande: classe={}, UE={}, enseignant={}, type={}, heures={}", 
                    request.getClasseId(), request.getUeId(), request.getEnseignantId(), 
                    request.getTypeCours(), request.getHeuresRequises());

            // Calculer le nombre de créneaux nécessaires (2h par créneau)
            int creneauxNecessaires = (int) Math.ceil(request.getHeuresRequises() / 2.0);
            
            // Trouver les créneaux pour cette demande
            List<SchedulingResult> coursResults = scheduleCoursWithFordFulkerson(
                    request, creneauxNecessaires, availableSlots, salles, disponibilites, results);
            
            results.addAll(coursResults);
        }

        log.info("Génération terminée avec {} cours planifiés", results.size());
        return results;
    }

    private List<SchedulingResult> scheduleCoursWithFordFulkerson(
            SchedulingRequest request,
            int creneauxNecessaires,
            List<TimeSlot> availableSlots,
            List<Salle> salles,
            List<Disponibilite> disponibilites,
            List<SchedulingResult> existingResults) {

        List<SchedulingResult> results = new ArrayList<>();
        
        for (int i = 0; i < creneauxNecessaires; i++) {
            FlowNetwork network = buildFlowNetwork(request, availableSlots, salles, disponibilites, existingResults, results);
            
            // Appliquer Ford-Fulkerson
            int maxFlow = fordFulkerson(network, "source", "sink");
            
            if (maxFlow > 0) {
                // Extraire la solution du flot
                SchedulingResult result = extractSolutionFromFlow(network, request, availableSlots, salles);
                if (result != null) {
                    results.add(result);
                    log.info("Créneau {} planifié: {}-{} en salle {}", 
                            i+1, result.getDateHeureDebut(), result.getDateHeureFin(), result.getSalleId());
                } else {
                    log.warn("Impossible de planifier le créneau {} pour la demande", i+1);
                    break;
                }
            } else {
                log.warn("Aucun flot trouvé pour le créneau {} de la demande", i+1);
                break;
            }
        }
        
        return results;
    }

    private FlowNetwork buildFlowNetwork(
            SchedulingRequest request,
            List<TimeSlot> availableSlots,
            List<Salle> salles,
            List<Disponibilite> disponibilites,
            List<SchedulingResult> existingResults,
            List<SchedulingResult> currentResults) {

        FlowNetwork network = new FlowNetwork();
        
        // Filtrer les créneaux disponibles pour l'enseignant
        List<TimeSlot> validSlots = filterValidSlots(request, availableSlots, disponibilites, existingResults, currentResults);
        
        // Filtrer les salles compatibles
        List<Salle> validSalles = filterValidSalles(request, salles);
        
        // Construire le graphe: source -> créneaux -> salles -> sink
        for (TimeSlot slot : validSlots) {
            String slotId = "slot_" + slot.getSemaine() + "_" + slot.getJourSemaine() + "_" + slot.getCreneau();
            network.addEdge("source", slotId, 1);
            
            for (Salle salle : validSalles) {
                if (isSalleAvailable(salle, slot, existingResults, currentResults)) {
                    String salleId = "salle_" + salle.getId();
                    network.addEdge(slotId, salleId, 1);
                    network.addEdge(salleId, "sink", 1);
                }
            }
        }
        
        return network;
    }

    private List<TimeSlot> filterValidSlots(
            SchedulingRequest request,
            List<TimeSlot> availableSlots,
            List<Disponibilite> disponibilites,
            List<SchedulingResult> existingResults,
            List<SchedulingResult> currentResults) {

        return availableSlots.stream()
            .filter(slot -> {
                if (request.getHeureDebut() == null && request.getHeureFin() == null) {
                    return true;
                }
                LocalTime slotStart = slot.getDebut().toLocalTime();
                LocalTime slotEnd = slot.getFin().toLocalTime();
                LocalTime ueStart = request.getHeureDebut() != null ? request.getHeureDebut() : slotStart;
                LocalTime ueEnd = request.getHeureFin() != null ? request.getHeureFin() : slotEnd;
                return slotEnd.isAfter(ueStart) && slotStart.isBefore(ueEnd);
            })
            .filter(slot -> isEnseignantAvailable(request.getEnseignantId(), slot, disponibilites))
            .filter(slot -> !isClasseOccupied(request.getClasseId(), slot, existingResults, currentResults))
            .filter(slot -> !isEnseignantOccupied(request.getEnseignantId(), slot, existingResults, currentResults))
            .collect(Collectors.toList());
    }

    private List<Salle> filterValidSalles(SchedulingRequest request, List<Salle> salles) {
        return salles.stream()
            .filter(salle -> request.getIdEcole() == null || request.getIdEcole().isBlank()
                || request.getIdEcole().equals(salle.getIdEcole()))
            .filter(salle -> isSalleCompatibleWithClasse(request, salle))
            .filter(salle -> salle.getCapacite() >= request.getEffectifClasse())
            .filter(salle -> isTypeCompatible(salle.getType(), request.getTypeCours()))
            .collect(Collectors.toList());
    }

    private boolean isSalleCompatibleWithClasse(SchedulingRequest request, Salle salle) {
        if (isCourseRoomType(salle.getType())) {
            if (salle.getIdClasse() == null || salle.getIdClasse().isBlank()) {
                return true;
            }
            return request.getClasseId().equals(salle.getIdClasse());
        }
        if (isTpRoomType(salle.getType())) {
            return salle.getIdClasse() == null || salle.getIdClasse().isBlank()
                    || request.getClasseId().equals(salle.getIdClasse());
        }
        return true;
    }

    private boolean isCourseRoomType(String typeSalle) {
        String normalized = normalizeRoomType(typeSalle);
        return normalized.contains("COURS") || normalized.contains("AMPH") || normalized.contains("THEATRE")
                || normalized.contains("CONFERENCE") || normalized.contains("COURS_AMPHITHEATRE")
                || normalized.contains("AMPHITHEATRE_SALLE_DE_COURS") || normalized.contains("SALLE_DE_COURS_AND_AMPHITHEATRE");
    }

    private boolean isTpRoomType(String typeSalle) {
        String normalized = normalizeRoomType(typeSalle);
        return normalized.contains("LABO") || normalized.contains("INFO") || normalized.contains("INFORMATIQUE")
                || normalized.contains("LABORATOIRE") || normalized.contains("SALLE_INFORMATIQUE_ET_LABORATOIRE");
    }

    private boolean isEnseignantAvailable(String enseignantId, TimeSlot slot, List<Disponibilite> disponibilites) {
        return disponibilites.stream()
                .filter(dispo -> dispo.getEnseignantId().equals(enseignantId))
                .anyMatch(dispo -> {
                    LocalDateTime slotStart = slot.getDebut();
                    LocalDateTime slotEnd = slot.getFin();
                    LocalDateTime dispoStart = dispo.getDebut();
                    LocalDateTime dispoEnd = dispo.getFin();
                    return slotStart.isBefore(dispoEnd) && slotEnd.isAfter(dispoStart);
                });
    }

    private boolean isTypeCompatible(String typeSalle, String typeCours) {
        String normalizedType = normalizeRoomType(typeSalle);
        String normalizedCours = normalizeCourseType(typeCours);

        Set<String> allowedTypes = switch (normalizedCours) {
            case "CM" -> Set.of("AMPHITHEATRE", "SALLE_COURS", "SALLE_CONFERENCE", "AMPHI", "SALLEDECOURS", "AMPHI_THEATRE",
                    "SALLE_DE_COURS", "SALLE_DE_COURS_AMPHITHEATRE", "AMPHITHEATRE_SALLE_DE_COURS",
                    "SALLE_DE_COURS_AND_AMPHITHEATRE", "SALLE_COURS_AMPHITHEATRE");
            case "TD" -> Set.of("SALLE_COURS", "SALLE_CONFERENCE", "SALLEDECOURS", "AMPHITHEATRE", "AMPHI", "AMPHI_THEATRE",
                    "SALLE_DE_COURS", "SALLE_DE_COURS_AMPHITHEATRE", "AMPHITHEATRE_SALLE_DE_COURS",
                    "SALLE_DE_COURS_AND_AMPHITHEATRE", "SALLE_COURS_AMPHITHEATRE");
            case "TP" -> Set.of("LABORATOIRE", "SALLE_INFORMATIQUE", "INFO", "LABO", "LABORATOIRE_INFORMATIQUE",
                    "SALLE_INFORMATIQUE_AMPHITHEATRE", "SALLE_INFORMATIQUE_ET_LABORATOIRE", "LABORATOIRE_ET_SALLE_INFORMATIQUE");
            default -> Set.of();
        };

        if (normalizedType.isBlank()) {
            return true;
        }

        if (allowedTypes.contains(normalizedType) || allowedTypes.contains(normalizedType.replace(" ", "_"))) {
            return true;
        }

        if (normalizedCours.equals("CM") || normalizedCours.equals("TD")) {
            return normalizedType.contains("COURS") || normalizedType.contains("AMPH") || normalizedType.contains("THEATRE")
                    || normalizedType.contains("CONFERENCE") || normalizedType.contains("COURS_AMPHITHEATRE");
        }
        if (normalizedCours.equals("TP")) {
            return normalizedType.contains("LABO") || normalizedType.contains("INFO") || normalizedType.contains("INFORMATIQUE")
                    || normalizedType.contains("LABORATOIRE");
        }

        return false;
    }

    private String normalizeRoomType(String typeSalle) {
        if (typeSalle == null) {
            return "";
        }
        String normalized = typeSalle.trim()
                .replace('&', '_')
                .replace(',', '_')
                .replace('+', '_')
                .replace('-', '_')
                .replace(' ', '_')
                .replace('.', '_')
                .replace('/', '_')
                .replaceAll("[^A-Za-z0-9]+", "_")
                .toUpperCase(Locale.ROOT);
        normalized = normalized.replace("__", "_").replace("_AND_", "_").replace("_ET_", "_");
        normalized = normalized.replace("SALLE_DE_COURS_AND_AMPHITHEATRE", "SALLE_COURS_AMPHITHEATRE");
        return normalized.replaceAll("^_+|_+$", "");
    }

    private String normalizeCourseType(String typeCours) {
        if (typeCours == null) {
            return "";
        }
        return typeCours.trim().toUpperCase(Locale.ROOT);
    }

    private int fordFulkerson(FlowNetwork network, String source, String sink) {
        int maxFlow = 0;
        
        while (true) {
            List<String> path = bfs(network, source, sink);
            if (path.isEmpty()) break;
            
            // Trouver la capacité minimale sur le chemin
            int pathFlow = Integer.MAX_VALUE;
            for (int i = 0; i < path.size() - 1; i++) {
                pathFlow = Math.min(pathFlow, network.getResidualCapacity(path.get(i), path.get(i + 1)));
            }
            
            // Mettre à jour le flot
            for (int i = 0; i < path.size() - 1; i++) {
                network.addFlow(path.get(i), path.get(i + 1), pathFlow);
            }
            
            maxFlow += pathFlow;
        }
        
        return maxFlow;
    }

    private List<String> bfs(FlowNetwork network, String source, String sink) {
        Queue<String> queue = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();
        
        queue.offer(source);
        visited.add(source);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            if (current.equals(sink)) {
                // Reconstruire le chemin
                List<String> path = new ArrayList<>();
                String node = sink;
                while (node != null) {
                    path.add(0, node);
                    node = parent.get(node);
                }
                return path;
            }
            
            for (String neighbor : network.getNeighbors(current)) {
                if (!visited.contains(neighbor) && network.getResidualCapacity(current, neighbor) > 0) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }
        
        return new ArrayList<>();
    }

    private SchedulingResult extractSolutionFromFlow(
            FlowNetwork network,
            SchedulingRequest request,
            List<TimeSlot> availableSlots,
            List<Salle> salles) {

        // Trouver le créneau et la salle utilisés dans le flot
        for (TimeSlot slot : availableSlots) {
            String slotId = "slot_" + slot.getSemaine() + "_" + slot.getJourSemaine() + "_" + slot.getCreneau();
            
            for (Salle salle : salles) {
                String salleId = "salle_" + salle.getId();
                
                if (network.flow.getOrDefault(slotId, new HashMap<>()).getOrDefault(salleId, 0) > 0) {
                    SchedulingResult result = new SchedulingResult();
                    result.setClasseId(request.getClasseId());
                    result.setEnseignantId(request.getEnseignantId());
                    result.setUeId(request.getUeId());
                    result.setSalleId(salle.getId());
                    result.setDateHeureDebut(slot.getDebut());
                    result.setDateHeureFin(slot.getFin());
                    result.setTypeCours(request.getTypeCours());
                    result.setSemaine(slot.getSemaine());
                    result.setJourSemaine(slot.getJourSemaine());
                    result.setCreneau(slot.getCreneau());
                    return result;
                }
            }
        }
        
        return null;
    }

    private boolean isSalleAvailable(Salle salle, TimeSlot slot, List<SchedulingResult> existingResults, List<SchedulingResult> currentResults) {
        return Stream.concat(existingResults.stream(), currentResults.stream())
                .filter(result -> result != null && result.getSalleId() != null && result.getSalleId().equals(salle.getId()))
                .noneMatch(result -> result.getDateHeureDebut() != null && result.getDateHeureFin() != null
                        && result.getDateHeureDebut().isBefore(slot.getFin())
                        && slot.getDebut().isBefore(result.getDateHeureFin()));
    }

    private boolean isClasseOccupied(String classeId, TimeSlot slot, List<SchedulingResult> existingResults, List<SchedulingResult> currentResults) {
        return Stream.concat(existingResults.stream(), currentResults.stream())
                .filter(result -> result != null && result.getClasseId() != null && result.getClasseId().equals(classeId))
                .anyMatch(result -> result.getDateHeureDebut() != null && result.getDateHeureFin() != null
                        && result.getDateHeureDebut().isBefore(slot.getFin())
                        && slot.getDebut().isBefore(result.getDateHeureFin()));
    }

    private boolean isEnseignantOccupied(String enseignantId, TimeSlot slot, List<SchedulingResult> existingResults, List<SchedulingResult> currentResults) {
        return Stream.concat(existingResults.stream(), currentResults.stream())
                .filter(result -> result != null && result.getEnseignantId() != null && result.getEnseignantId().equals(enseignantId))
                .anyMatch(result -> result.getDateHeureDebut() != null && result.getDateHeureFin() != null
                        && result.getDateHeureDebut().isBefore(slot.getFin())
                        && slot.getDebut().isBefore(result.getDateHeureFin()));
    }
}