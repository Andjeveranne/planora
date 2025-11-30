# Architecture du UserService

## Vue d'Ensemble

Le UserService est un microservice Spring Boot qui gère les utilisateurs de l'application Plannora. Il utilise une architecture en couches avec héritage JPA pour modéliser les différents types d'utilisateurs.

## Diagramme de Classes

```
                    Utilisateur
                    -----------
                    - idUser: String
                    - mdp: String
                    - email: String
                    - nomUser: String
                    - prenomUser: String
                    - telephone: String
                    - role: String
                         △
                         |
            ┌────────────┴────────────┐
            |                         |
      Administrateur            Enseignant
      --------------            ----------
                                - specialite: String
                                - departement: String
```

## Architecture en Couches

```
┌─────────────────────────────────────────┐
│         Controller Layer                │
│  (UtilisateurController)                │
│  - Endpoints REST                       │
│  - Validation des requêtes              │
│  - Gestion des réponses HTTP            │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Service Layer                   │
│  (UtilisateurService)                   │
│  - Logique métier                       │
│  - Validation des règles                │
│  - Transformation DTO ↔ Entity          │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Repository Layer                │
│  (UtilisateurRepository,                │
│   EnseignantRepository,                 │
│   AdministrateurRepository)             │
│  - Accès aux données                    │
│  - Requêtes JPA                         │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Database Layer                  │
│  MySQL - plannoradb                     │
│  - utilisateurs (table parent)          │
│  - enseignants (table enfant)           │
│  - administrateurs (table enfant)       │
└─────────────────────────────────────────┘
```

## Composants Principaux

### 1. Modèle de Données (model/)

#### Utilisateur.java
- Classe parent abstraite
- Contient les attributs communs à tous les utilisateurs
- Utilise `@Inheritance(strategy = InheritanceType.JOINED)`
- Stratégie JOINED : chaque classe a sa propre table

#### Administrateur.java
- Hérite de Utilisateur
- Pas d'attributs supplémentaires
- Rôle : Gestion complète des utilisateurs

#### Enseignant.java
- Hérite de Utilisateur
- Attributs supplémentaires : specialite, departement
- Rôle : Consultation des enseignants

### 2. DTOs (dto/)

#### UtilisateurDTO
- Utilisé pour les requêtes de création/modification
- Contient les validations (@NotBlank, @Email)
- Inclut le mot de passe (qui sera hashé)

#### EnseignantDTO
- Hérite de UtilisateurDTO
- Ajoute specialite et departement

#### UtilisateurResponseDTO
- Utilisé pour les réponses
- N'inclut PAS le mot de passe
- Données sûres à exposer

### 3. Repositories (repository/)

#### UtilisateurRepository
- Interface JpaRepository<Utilisateur, String>
- Méthodes : findByEmail, existsByEmail

#### EnseignantRepository
- Interface JpaRepository<Enseignant, String>
- Méthodes : findByDepartement

#### AdministrateurRepository
- Interface JpaRepository<Administrateur, String>
- Méthodes CRUD de base

### 4. Service (service/)

#### UtilisateurService
- Logique métier principale
- Gestion du CRUD
- Hashage des mots de passe avec BCrypt
- Transformation DTO ↔ Entity
- Validation des règles métier

### 5. Controller (controller/)

#### UtilisateurController
- Endpoints REST
- Annotations de sécurité (@PreAuthorize)
- Validation des entrées (@Valid)
- Gestion des codes HTTP

### 6. Sécurité (security/)

#### JwtAuthenticationFilter
- Filtre Spring Security
- Extraction et validation du token JWT
- Injection de l'authentification dans le contexte

#### JwtTokenProvider
- Validation des tokens JWT
- Extraction des claims (userId, role)
- Utilise la même clé secrète que le service d'authentification

#### SecurityConfig
- Configuration Spring Security
- Désactivation CSRF (API REST)
- Session STATELESS
- Configuration des endpoints publics/protégés

### 7. Configuration (config/)

#### DataInitializer
- CommandLineRunner
- Crée un administrateur par défaut au démarrage
- Email : admin@plannora.com
- Mot de passe : admin123

### 8. Gestion des Erreurs (exception/)

#### GlobalExceptionHandler
- @RestControllerAdvice
- Gestion centralisée des exceptions
- Transformation des erreurs en réponses JSON
- Gestion des erreurs de validation

## Flux de Requête

### Exemple : Créer un Enseignant

```
1. Client → POST /api/utilisateurs/enseignant
   Headers: Authorization: Bearer TOKEN
   Body: EnseignantDTO

2. JwtAuthenticationFilter
   - Extrait le token
   - Valide le token
   - Injecte l'authentification (userId, role=ADMIN)

3. UtilisateurController.creerEnseignant()
   - @PreAuthorize("hasRole('ADMIN')") vérifie le rôle
   - @Valid valide le DTO
   - Appelle le service

4. UtilisateurService.creerUtilisateur()
   - Vérifie que l'email n'existe pas
   - Crée une instance Enseignant
   - Hash le mot de passe avec BCrypt
   - Génère un UUID si nécessaire
   - Sauvegarde via EnseignantRepository

5. EnseignantRepository.save()
   - JPA persiste dans la base
   - Table utilisateurs + table enseignants

6. Response → 201 Created
   Body: UtilisateurResponseDTO (sans mot de passe)
```

## Stratégie d'Héritage JPA

### JOINED Strategy

```sql
-- Table parent
CREATE TABLE utilisateurs (
    id_user VARCHAR(255) PRIMARY KEY,
    mdp VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    nom_user VARCHAR(255),
    prenom_user VARCHAR(255),
    telephone VARCHAR(20),
    role VARCHAR(50)
);

-- Table enfant 1
CREATE TABLE administrateurs (
    id_user VARCHAR(255) PRIMARY KEY,
    FOREIGN KEY (id_user) REFERENCES utilisateurs(id_user)
);

-- Table enfant 2
CREATE TABLE enseignants (
    id_user VARCHAR(255) PRIMARY KEY,
    specialite VARCHAR(255),
    departement VARCHAR(255),
    FOREIGN KEY (id_user) REFERENCES utilisateurs(id_user)
);
```

### Avantages de JOINED
- ✅ Normalisation des données
- ✅ Pas de colonnes NULL inutiles
- ✅ Intégrité référentielle
- ✅ Requêtes optimisées par type

### Requête JPA
```java
// Récupérer tous les enseignants
List<Enseignant> enseignants = enseignantRepository.findAll();
// SQL: SELECT * FROM utilisateurs u JOIN enseignants e ON u.id_user = e.id_user
```

## Sécurité

### Contrôle d'Accès

| Endpoint | Méthode | Rôle Requis |
|----------|---------|-------------|
| `/api/utilisateurs` | POST | ADMIN |
| `/api/utilisateurs/enseignant` | POST | ADMIN |
| `/api/utilisateurs` | GET | ADMIN |
| `/api/utilisateurs/enseignants` | GET | ADMIN, ENSEIGNANT |
| `/api/utilisateurs/{id}` | GET | ADMIN |
| `/api/utilisateurs/{id}` | PUT | ADMIN |
| `/api/utilisateurs/{id}` | DELETE | ADMIN |

### Authentification JWT

1. L'utilisateur se connecte via le service d'authentification
2. Reçoit un token JWT contenant userId et role
3. Envoie le token dans le header : `Authorization: Bearer TOKEN`
4. Le filtre JWT valide le token et injecte l'authentification
5. Spring Security vérifie les autorisations (@PreAuthorize)

## Intégration avec les Autres Services

### Service d'Authentification
- Émet les tokens JWT
- Valide les credentials
- UserService utilise les mêmes tokens

### Eureka Server
- UserService s'enregistre automatiquement
- Permet la découverte de service
- Health checks automatiques

### Gateway
- Route les requêtes vers UserService
- Peut ajouter des filtres globaux
- Load balancing si plusieurs instances

## Base de Données Partagée

Le UserService utilise la base `plannoradb` partagée avec les autres services :

- ✅ Cohérence des données
- ✅ Transactions distribuées simplifiées
- ✅ Pas de duplication de données utilisateur
- ⚠️ Couplage entre services (à gérer avec soin)

## Évolutivité

### Scalabilité Horizontale
- Plusieurs instances du UserService peuvent tourner
- Eureka gère le load balancing
- Base de données partagée (attention aux connexions)

### Performance
- Index sur email (UNIQUE)
- Requêtes JPA optimisées
- Pas de N+1 queries grâce à JOINED

### Monitoring
- Actuator endpoints (à ajouter)
- Logs structurés
- Métriques Eureka

## Bonnes Pratiques Implémentées

1. ✅ Séparation des couches (Controller, Service, Repository)
2. ✅ DTOs pour isoler le modèle de données
3. ✅ Validation des entrées (@Valid, @NotBlank)
4. ✅ Gestion centralisée des erreurs
5. ✅ Sécurité par rôle (@PreAuthorize)
6. ✅ Hashage des mots de passe (BCrypt)
7. ✅ Pas de mot de passe dans les réponses
8. ✅ Transactions (@Transactional)
9. ✅ Héritage JPA propre (JOINED)
10. ✅ Configuration externalisée (application.properties)
