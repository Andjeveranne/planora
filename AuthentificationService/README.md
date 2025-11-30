# Service d'Authentification - Plannora

Service d'authentification pour le système de gestion de réservation de salles Plannora.

## Configuration

### Base de données MySQL

1. Assurez-vous que MySQL est installé et en cours d'exécution
2. La base de données `PlannoraDB` sera créée automatiquement au démarrage
3. Configuration par défaut dans `application.properties`:
   - URL: `jdbc:mysql://localhost:3306/PlannoraDB`
   - Username: `root`
   - Password: (vide par défaut)

### Démarrage

```bash
cd AuthentificationService/Authentification/authentification
mvn clean install
mvn spring-boot:run
```

Le service démarre sur le port **8082**.

## API Endpoints

### 1. Connexion (Login)

**POST** `/api/auth/login`

**Body:**
```json
{
  "email": "admin@plannora.com",
  "password": "password123"
}
```

**Réponse:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "admin@plannora.com",
  "nom": "Admin",
  "prenom": "Système",
  "role": "ADMIN"
}
```

### 2. Inscription (Register)

**POST** `/api/auth/register`

**Body:**
```json
{
  "email": "nouveau@plannora.com",
  "password": "motdepasse123",
  "nom": "Nom",
  "prenom": "Prénom",
  "role": "ETUDIANT"
}
```

**Réponse:**
```json
{
  "message": "Utilisateur créé avec succès",
  "userId": 4
}
```

## Utilisateurs de test

Trois utilisateurs sont créés automatiquement au démarrage:

| Email | Mot de passe | Rôle |
|-------|--------------|------|
| admin@plannora.com | password123 | ADMIN |
| enseignant@plannora.com | password123 | ENSEIGNANT |
| etudiant@plannora.com | password123 | ETUDIANT |

## Rôles disponibles

- **ADMIN**: Administrateur système
- **ENSEIGNANT**: Enseignant
- **ETUDIANT**: Étudiant

## Sécurité

- Les mots de passe sont hashés avec BCrypt
- Authentification par JWT (JSON Web Token)
- Token valide pendant 24 heures (86400000 ms)
- CORS activé pour tous les origins (à configurer selon vos besoins)

## Technologies utilisées

- Spring Boot 3.5.7
- Spring Security
- Spring Data JPA
- MySQL
- JWT (jjwt 0.11.5)
- Lombok
- Bean Validation

## Structure du projet

```
src/main/java/com/isi4/authentification/
├── config/          # Configuration Spring Security
├── controller/      # Contrôleurs REST
├── dto/            # Data Transfer Objects
├── entity/         # Entités JPA
├── repository/     # Repositories JPA
├── service/        # Logique métier
└── util/           # Utilitaires (JWT)
```
