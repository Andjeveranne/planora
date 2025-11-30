# UserService - Service de Gestion des Utilisateurs

Service en charge de la gestion des utilisateurs (administrateurs et enseignants) pour l'application Plannora.

## Architecture

### Modèle de Données

Le service utilise un modèle d'héritage JPA avec la stratégie `JOINED` :

- **Utilisateur** (classe parent) : Contient les informations communes
  - idUser (String) - Identifiant unique
  - mdp (String) - Mot de passe hashé
  - email (String) - Email unique
  - nomUser (String) - Nom
  - prenomUser (String) - Prénom
  - telephone (String) - Numéro de téléphone
  - role (String) - ADMIN ou ENSEIGNANT

- **Administrateur** (hérite de Utilisateur)
  - Peut gérer tous les utilisateurs (CRUD)
  - Peut gérer les droits d'accès

- **Enseignant** (hérite de Utilisateur)
  - specialite (String) - Spécialité de l'enseignant
  - departement (String) - Département d'affectation

### Base de Données

- **Base de données** : `plannoradb` (partagée avec les autres services)
- **Tables** :
  - `utilisateurs` : Table parent
  - `administrateurs` : Table enfant (JOINED)
  - `enseignants` : Table enfant (JOINED)

## Configuration

### Prérequis

- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Eureka Server en cours d'exécution
- Authentication Service en cours d'exécution

### Configuration MySQL

Modifiez `src/main/resources/application.properties` si nécessaire :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/plannoradb
spring.datasource.username=root
spring.datasource.password=
```

### Démarrage

```bash
cd UserService/user-service
mvn clean install
mvn spring-boot:run
```

Le service démarre sur le port **8083**.

## API Endpoints

Tous les endpoints nécessitent une authentification JWT (sauf indication contraire).

### Authentification

Utilisez le service d'authentification pour obtenir un token JWT :

```
POST http://localhost:8082/api/auth/login
```

Ajoutez le token dans le header : `Authorization: Bearer YOUR_TOKEN`

### Endpoints Utilisateurs

#### Créer un utilisateur (ADMIN uniquement)

```
POST /api/utilisateurs
Content-Type: application/json
Authorization: Bearer {token}

{
  "email": "user@example.com",
  "mdp": "password123",
  "nomUser": "Nom",
  "prenomUser": "Prénom",
  "telephone": "0612345678",
  "role": "ADMIN" ou "ENSEIGNANT"
}
```

#### Créer un enseignant (ADMIN uniquement)

```
POST /api/utilisateurs/enseignant
Content-Type: application/json
Authorization: Bearer {token}

{
  "email": "enseignant@example.com",
  "mdp": "password123",
  "nomUser": "Dupont",
  "prenomUser": "Jean",
  "telephone": "0612345678",
  "specialite": "Informatique",
  "departement": "Génie Logiciel"
}
```

#### Lister tous les utilisateurs (ADMIN uniquement)

```
GET /api/utilisateurs
Authorization: Bearer {token}
```

#### Lister tous les enseignants (ADMIN ou ENSEIGNANT)

```
GET /api/utilisateurs/enseignants
Authorization: Bearer {token}
```

#### Obtenir un utilisateur par ID (ADMIN uniquement)

```
GET /api/utilisateurs/{id}
Authorization: Bearer {token}
```

#### Mettre à jour un utilisateur (ADMIN uniquement)

```
PUT /api/utilisateurs/{id}
Content-Type: application/json
Authorization: Bearer {token}

{
  "nomUser": "Nouveau Nom",
  "prenomUser": "Nouveau Prénom",
  "telephone": "0699999999"
}
```

#### Supprimer un utilisateur (ADMIN uniquement)

```
DELETE /api/utilisateurs/{id}
Authorization: Bearer {token}
```

## Sécurité

### Contrôle d'Accès

- **ADMIN** : Accès complet à tous les endpoints (CRUD sur tous les utilisateurs)
- **ENSEIGNANT** : Accès en lecture seule à la liste des enseignants

### Authentification JWT

Le service valide les tokens JWT émis par le service d'authentification. Le token doit contenir :
- `sub` : ID de l'utilisateur
- `role` : Rôle de l'utilisateur (ADMIN ou ENSEIGNANT)

### Hashage des Mots de Passe

Les mots de passe sont automatiquement hashés avec BCrypt avant d'être stockés.

## Données Initiales

Un administrateur par défaut est créé au démarrage :

- **Email** : admin@plannora.com
- **Mot de passe** : admin123
- **ID** : admin001

## Tests

Consultez le fichier `GUIDE-TESTS-POSTMAN.md` pour des instructions détaillées sur les tests avec Postman.

Utilisez le fichier `test-user-api.http` avec l'extension REST Client de VS Code ou importez-le dans Postman.

## Structure du Projet

```
user-service/
├── src/main/java/com/isi4/userservice/
│   ├── config/
│   │   ├── DataInitializer.java
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   └── UtilisateurController.java
│   ├── dto/
│   │   ├── EnseignantDTO.java
│   │   ├── UtilisateurDTO.java
│   │   └── UtilisateurResponseDTO.java
│   ├── exception/
│   │   └── GlobalExceptionHandler.java
│   ├── model/
│   │   ├── Administrateur.java
│   │   ├── Enseignant.java
│   │   └── Utilisateur.java
│   ├── repository/
│   │   ├── AdministrateurRepository.java
│   │   ├── EnseignantRepository.java
│   │   └── UtilisateurRepository.java
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   └── JwtTokenProvider.java
│   ├── service/
│   │   └── UtilisateurService.java
│   └── UserServiceApplication.java
└── src/main/resources/
    └── application.properties
```

## Intégration avec les Autres Services

- **Eureka** : Le service s'enregistre automatiquement auprès d'Eureka
- **Gateway** : Accessible via le Gateway sur `http://localhost:8080/user-service/api/utilisateurs`
- **Authentication Service** : Utilise les tokens JWT émis par ce service

## Dépannage

### Erreur de connexion à la base de données

Vérifiez que MySQL est démarré et que la base `plannoradb` existe.

### Erreur 401 Unauthorized

Vérifiez que le token JWT est valide et correctement formaté dans le header.

### Erreur 403 Forbidden

Vérifiez que l'utilisateur a le rôle ADMIN pour les opérations CRUD.

## Évolutions Futures

- Gestion des groupes d'étudiants
- Gestion des UE par enseignant
- Synchronisation avec l'emploi du temps externe
- Notifications aux utilisateurs
