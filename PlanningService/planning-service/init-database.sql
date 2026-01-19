CREATE DATABASE IF NOT EXISTS PlannoraPlanningDB;
USE PlannoraPlanningDB;

CREATE TABLE IF NOT EXISTS disponibilites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enseignant_id VARCHAR(255) NOT NULL,
    date_heure_debut DATETIME NOT NULL,
    date_heure_fin DATETIME NOT NULL,
    commentaire VARCHAR(255),
    INDEX idx_enseignant (enseignant_id)
);
