-- Script de création de la base de données PlannoraDB
-- Exécutez ce script manuellement si vous souhaitez créer la base de données avant le démarrage

-- Création de la base de données
CREATE DATABASE IF NOT EXISTS PlannoraDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Utilisation de la base de données
USE PlannoraDB;

-- La table users sera créée automatiquement par Hibernate
-- Voici la structure pour référence:

/*
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
*/

-- Les utilisateurs de test seront insérés automatiquement via data.sql
-- Mot de passe pour tous: "password123"

SELECT 'Base de données PlannoraDB créée avec succès!' as message;
