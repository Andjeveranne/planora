-- Données de test pour les relations UE-Classe
INSERT IGNORE INTO ue_classes (ue_id, classe_id, heures_cm, heures_td, heures_tp) VALUES
(1, 'classe-1', 20, 10, 10),
(2, 'classe-1', 15, 15, 0),
(3, 'classe-2', 18, 12, 6),
(1, 'classe-2', 20, 10, 10),
(4, 'classe-1', 12, 8, 16);

-- Données de test pour les relations UE-Enseignant
INSERT IGNORE INTO ue_enseignants (ue_id, enseignant_id, peut_cm, peut_td, peut_tp) VALUES
(1, 'enseignant-1', true, true, false),
(2, 'enseignant-2', true, true, true),
(3, 'enseignant-1', false, true, true),
(4, 'enseignant-3', true, false, true),
(1, 'enseignant-2', false, true, false);