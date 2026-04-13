package com.isi4.planningservice.algorithm;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class FordFulkersonScheduler {

    @Data
    public static class SchedulingRequest {
        private String classeId;
        private Long ueId;
        private String enseignantId;
        private String typeCours; // CM, TD, TP
        private Integer heuresRequises;
        private Integer effectifClasse;
    }

    @Data
    public static class TimeSlot {
        private LocalDateTime debut;
        private LocalDateTime fin;
        private Integer jourSemaine;
        private String creneau;
        private Integer semaine;
    }

    @Data
    public static class Salle {
        private String id;
        private String nom;
        private Integer capacite;
        private String type;
    }

    @Data
    public static class Disponibilite {
        private String enseignantId;
        private LocalDateTime debut;
        private LocalDateTime fin;
    }

    @Data
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
                .filter(slot -> isEnseignantAvailable(request.getEnseignantId(), slot, disponibilites))
                .filter(slot -> !isClasseOccupied(request.getClasseId(), slot, existingResults, currentResults))
                .filter(slot -> !isEnseignantOccupied(request.getEnseignantId(), slot, existingResults, currentResults))
                .collect(Collectors.toList());
    }

    private List<Salle> filterValidSalles(SchedulingRequest request, List<Salle> salles) {
        return salles.stream()
                .filter(salle -> salle.getCapacite() >= request.getEffectifClasse())
                .filter(salle -> isTypeCompatible(salle.getType(), request.getTypeCours()))
                .collect(Collectors.toList());
    }

    private boolean isEnseignantAvailable(String enseignantId, TimeSlot slot, List<Disponibilite> disponibilites) {
        return disponibilites.stream()
                .anyMatch(dispo -> dispo.getEnseignantId().equals(enseignantId) &&
                        !dispo.getDebut().isAfter(slot.getDebut()) &&
                        !dispo.getFin().isBefore(slot.getFin()));
    }

    private boolean isClasseOccupied(String classeId, TimeSlot slot, List<SchedulingResult> existingResults, List<SchedulingResult> currentResults) {
        return Stream.concat(existingResults.stream(), currentResults.stream())
                .anyMatch(result -> result.getClasseId().equals(classeId) &&
                        isTimeOverlap(result.getDateHeureDebut(), result.getDateHeureFin(), slot.getDebut(), slot.getFin()));
    }

    private boolean isEnseignantOccupied(String enseignantId, TimeSlot slot, List<SchedulingResult> existingResults, List<SchedulingResult> currentResults) {
        return Stream.concat(existingResults.stream(), currentResults.stream())
                .anyMatch(result -> result.getEnseignantId().equals(enseignantId) &&
                        isTimeOverlap(result.getDateHeureDebut(), result.getDateHeureFin(), slot.getDebut(), slot.getFin()));
    }

    private boolean isSalleAvailable(Salle salle, TimeSlot slot, List<SchedulingResult> existingResults, List<SchedulingResult> currentResults) {
        return Stream.concat(existingResults.stream(), currentResults.stream())
                .noneMatch(result -> result.getSalleId().equals(salle.getId()) &&
                        isTimeOverlap(result.getDateHeureDebut(), result.getDateHeureFin(), slot.getDebut(), slot.getFin()));
    }

    private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private boolean isTypeCompatible(String typeSalle, String typeCours) {
        return switch (typeCours) {
            case "CM" -> List.of("AMPHITHEATRE", "SALLE_COURS", "SALLE_CONFERENCE").contains(typeSalle);
            case "TD" -> List.of("SALLE_COURS", "SALLE_CONFERENCE").contains(typeSalle);
            case "TP" -> List.of("LABORATOIRE", "SALLE_INFORMATIQUE").contains(typeSalle);
            default -> true;
        };
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
}