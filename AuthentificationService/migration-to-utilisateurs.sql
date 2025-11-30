-- Script de migration : Supprimer la table users et utiliser utilisateurs
-- Base de données : PlannoraDB

USE PlannoraDB;

-- 1. Sauvegarder les données de la table users (si elle existe et contient des données)
-- Cette étape est optionnelle si vous voulez conserver les anciennes données

-- Créer une table temporaire pour sauvegarder les données
DROP TABLE IF EXISTS users_backup;
CREATE TABLE users_backup AS SELECT * FROM users WHERE 1=0;

-- Copier les données existantes (si la table users existe)
INSERT INTO users_backup 
SELECT * FROM users 
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'PlannoraDB' AND table_name = 'users');

-- 2. Supprimer la table users
DROP TABLE IF EXISTS users;

-- 3. Vérifier que la table utilisateurs existe
-- (Elle sera créée automatiquement par Hibernate au démarrage des services)

-- 4. Afficher les tables restantes
SHOW TABLES;

-- 5. Afficher la structure de la table utilisateurs
DESCRIBE utilisateurs;

-- 6. Afficher le contenu de la table utilisateurs
SELECT * FROM utilisateurs;

-- Note : Les données de test seront créées automatiquement par DataInitializer
-- au démarrage du service d'authentification si la table est vide.

-- Résultat attendu :
-- - Table 'users' supprimée
-- - Table 'utilisateurs' utilisée par les deux services
-- - Données de test créées automatiquement
