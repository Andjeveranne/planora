-- Script d'initialisation de la base de données pour UserService
-- Base de données partagée : plannoradb

-- Créer la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS plannoradb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE plannoradb;

-- Les tables seront créées automatiquement par Hibernate avec spring.jpa.hibernate.ddl-auto=update
-- Ce script est fourni à titre de référence pour comprendre la structure

-- Table parent : utilisateurs
-- CREATE TABLE IF NOT EXISTS utilisateurs (
--     id_user VARCHAR(255) PRIMARY KEY,
--     mdp VARCHAR(255) NOT NULL,
--     email VARCHAR(255) NOT NULL UNIQUE,
--     nom_user VARCHAR(255) NOT NULL,
--     prenom_user VARCHAR(255) NOT NULL,
--     telephone VARCHAR(20) NOT NULL,
--     role VARCHAR(50) NOT NULL
-- );

-- Table enfant : administrateurs (héritage JOINED)
-- CREATE TABLE IF NOT EXISTS administrateurs (
--     id_user VARCHAR(255) PRIMARY KEY,
--     FOREIGN KEY (id_user) REFERENCES utilisateurs(id_user) ON DELETE CASCADE
-- );

-- Table enfant : enseignants (héritage JOINED)
-- CREATE TABLE IF NOT EXISTS enseignants (
--     id_user VARCHAR(255) PRIMARY KEY,
--     specialite VARCHAR(255),
--     departement VARCHAR(255),
--     FOREIGN KEY (id_user) REFERENCES utilisateurs(id_user) ON DELETE CASCADE
-- );

-- Données de test (optionnel - l'admin par défaut est créé par DataInitializer)
-- INSERT INTO utilisateurs (id_user, mdp, email, nom_user, prenom_user, telephone, role) 
-- VALUES ('admin001', '$2a$10$...', 'admin@plannora.com', 'Admin', 'Système', '0000000000', 'ADMIN');

-- INSERT INTO administrateurs (id_user) VALUES ('admin001');
