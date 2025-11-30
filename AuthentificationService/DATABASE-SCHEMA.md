# Schéma de la Base de Données PlannoraDB

## Table: users

Table principale pour stocker les informations des utilisateurs du système.

### Structure

| Colonne | Type | Contraintes | Description |
|---------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Identifiant unique de l'utilisateur |
| email | VARCHAR(255) | NOT NULL, UNIQUE | Adresse email (utilisée pour la connexion) |
| password | VARCHAR(255) | NOT NULL | Mot de passe hashé avec BCrypt |
| nom | VARCHAR(255) | NOT NULL | Nom de famille de l'utilisateur |
| prenom | VARCHAR(255) | NOT NULL | Prénom de l'utilisateur |
| role | VARCHAR(50) | NOT NULL | Rôle de l'utilisateur (ADMIN, ENSEIGNANT, ETUDIANT) |
| created_at | DATETIME | | Date de création du compte |
| updated_at | DATETIME | | Date de dernière modification |

### Index

- PRIMARY KEY sur `id`
- UNIQUE INDEX sur `email`
- INDEX sur `email` pour optimiser les recherches

### Rôles disponibles

```sql
ENUM('ADMIN', 'ENSEIGNANT', 'ETUDIANT')
```

- **ADMIN**: Administrateur système avec tous les droits
- **ENSEIGNANT**: Enseignant pouvant gérer ses cours et réservations
- **ETUDIANT**: Étudiant pouvant consulter et réserver

## Exemple de données

```sql
-- Utilisateur Admin
INSERT INTO users (email, password, nom, prenom, role, created_at, updated_at) 
VALUES (
    'admin@plannora.com',
    '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG',
    'Admin',
    'Système',
    'ADMIN',
    NOW(),
    NOW()
);

-- Utilisateur Enseignant
INSERT INTO users (email, password, nom, prenom, role, created_at, updated_at) 
VALUES (
    'enseignant@plannora.com',
    '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG',
    'Dupont',
    'Jean',
    'ENSEIGNANT',
    NOW(),
    NOW()
);

-- Utilisateur Étudiant
INSERT INTO users (email, password, nom, prenom, role, created_at, updated_at) 
VALUES (
    'etudiant@plannora.com',
    '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG',
    'Martin',
    'Marie',
    'ETUDIANT',
    NOW(),
    NOW()
);
```

## Requêtes SQL utiles

### Rechercher un utilisateur par email
```sql
SELECT * FROM users WHERE email = 'admin@plannora.com';
```

### Lister tous les enseignants
```sql
SELECT * FROM users WHERE role = 'ENSEIGNANT';
```

### Compter les utilisateurs par rôle
```sql
SELECT role, COUNT(*) as total 
FROM users 
GROUP BY role;
```

### Utilisateurs créés récemment
```sql
SELECT * FROM users 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY created_at DESC;
```

### Mettre à jour le mot de passe d'un utilisateur
```sql
UPDATE users 
SET password = '$2a$10$nouveauHash...', updated_at = NOW()
WHERE email = 'user@plannora.com';
```

### Supprimer un utilisateur
```sql
DELETE FROM users WHERE id = 1;
```

## Extensions futures

### Table: refresh_tokens (pour gérer les refresh tokens)

```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_id (user_id)
);
```

### Table: password_reset_tokens (pour la réinitialisation de mot de passe)

```sql
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token)
);
```

### Table: login_history (pour l'audit des connexions)

```sql
CREATE TABLE login_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    success BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time)
);
```

## Diagramme ER (Entity-Relationship)

```
┌─────────────────────────────────────┐
│             users                    │
├─────────────────────────────────────┤
│ PK  id (BIGINT)                     │
│ UQ  email (VARCHAR)                 │
│     password (VARCHAR)              │
│     nom (VARCHAR)                   │
│     prenom (VARCHAR)                │
│     role (ENUM)                     │
│     created_at (DATETIME)           │
│     updated_at (DATETIME)           │
└─────────────────────────────────────┘
```

## Sécurité

### Hashage des mots de passe

Les mots de passe sont hashés avec **BCrypt** (algorithme de hachage adaptatif):
- Coût: 10 rounds (par défaut)
- Format: `$2a$10$...` (60 caractères)
- Résistant aux attaques par force brute
- Chaque mot de passe a un salt unique

### Bonnes pratiques

1. ✅ Ne jamais stocker les mots de passe en clair
2. ✅ Utiliser UNIQUE sur l'email pour éviter les doublons
3. ✅ Indexer les colonnes fréquemment recherchées
4. ✅ Utiliser des timestamps pour l'audit
5. ✅ Valider les emails côté application ET base de données
6. ✅ Limiter la longueur des VARCHAR pour optimiser les performances

## Maintenance

### Backup régulier
```bash
mysqldump -u root -p PlannoraDB > backup_$(date +%Y%m%d).sql
```

### Restauration
```bash
mysql -u root -p PlannoraDB < backup_20251120.sql
```

### Optimisation
```sql
OPTIMIZE TABLE users;
ANALYZE TABLE users;
```
