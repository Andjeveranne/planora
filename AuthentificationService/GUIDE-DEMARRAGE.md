# Guide de Démarrage - Service d'Authentification Plannora

## Prérequis

1. **Java 17** ou supérieur
2. **Maven 3.6+**
3. **MySQL 8.0+** installé et en cours d'exécution
4. **Eureka Server** en cours d'exécution sur le port 8761

## Installation et Configuration

### Étape 1: Configuration MySQL

1. Démarrez MySQL
2. Connectez-vous à MySQL:
   ```bash
   mysql -u root -p
   ```

3. (Optionnel) Créez la base de données manuellement:
   ```sql
   CREATE DATABASE IF NOT EXISTS PlannoraDB;
   ```
   
   Ou exécutez le script fourni:
   ```bash
   mysql -u root -p < init-database.sql
   ```

4. Si votre mot de passe MySQL n'est pas vide, modifiez `application.properties`:
   ```properties
   spring.datasource.password=votre_mot_de_passe
   ```

### Étape 2: Compilation du projet

```bash
cd AuthentificationService/Authentification/authentification
mvn clean install
```

### Étape 3: Démarrage du service

```bash
mvn spring-boot:run
```

Le service démarre sur **http://localhost:8082**

### Étape 4: Vérification

1. Vérifiez que le service est enregistré dans Eureka:
   - Ouvrez http://localhost:8761
   - Vous devriez voir "AUTHENTIFICATION" dans la liste des services

2. Testez l'API de login:
   ```bash
   curl -X POST http://localhost:8082/api/auth/login \
     -H "Content-Type: application/json" \
     -d "{\"email\":\"admin@plannora.com\",\"password\":\"password123\"}"
   ```

## Test de l'API

### Utilisation avec REST Client (VS Code)

1. Installez l'extension "REST Client" dans VS Code
2. Ouvrez le fichier `test-api.http`
3. Cliquez sur "Send Request" au-dessus de chaque requête

### Utilisation avec Postman

Importez les requêtes suivantes:

**Login:**
- Method: POST
- URL: http://localhost:8082/api/auth/login
- Body (JSON):
  ```json
  {
    "email": "admin@plannora.com",
    "password": "password123"
  }
  ```

**Register:**
- Method: POST
- URL: http://localhost:8082/api/auth/register
- Body (JSON):
  ```json
  {
    "email": "test@plannora.com",
    "password": "password123",
    "nom": "Test",
    "prenom": "User",
    "role": "ETUDIANT"
  }
  ```

## Utilisateurs de Test

| Email | Mot de passe | Rôle |
|-------|--------------|------|
| admin@plannora.com | password123 | ADMIN |
| enseignant@plannora.com | password123 | ENSEIGNANT |
| etudiant@plannora.com | password123 | ETUDIANT |

## Utilisation du Token JWT

Après une connexion réussie, vous recevrez un token JWT. Utilisez-le dans les autres services:

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

## Dépannage

### Erreur: "Access denied for user 'root'@'localhost'"
- Vérifiez votre mot de passe MySQL dans `application.properties`

### Erreur: "Unknown database 'PlannoraDB'"
- La base sera créée automatiquement grâce à `createDatabaseIfNotExist=true`
- Sinon, créez-la manuellement avec le script `init-database.sql`

### Erreur: "Table 'users' doesn't exist"
- Vérifiez que `spring.jpa.hibernate.ddl-auto=update` est dans `application.properties`
- Hibernate créera automatiquement les tables

### Le service ne s'enregistre pas dans Eureka
- Vérifiez qu'Eureka Server est démarré sur le port 8761
- Vérifiez la configuration dans `application.properties`

### Les utilisateurs de test ne sont pas créés
- Vérifiez que `spring.sql.init.mode=always` est dans `application.properties`
- Vérifiez les logs pour voir si `data.sql` a été exécuté

## Architecture

```
┌─────────────────┐
│   Frontend      │
│  (HTML/JS)      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Gateway        │
│  (Port 8080)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐      ┌──────────────┐
│ Authentification│◄────►│   Eureka     │
│  (Port 8082)    │      │ (Port 8761)  │
└────────┬────────┘      └──────────────┘
         │
         ▼
┌─────────────────┐
│  MySQL          │
│  PlannoraDB     │
└─────────────────┘
```

## Prochaines Étapes

1. Intégrer l'authentification avec le Gateway
2. Ajouter la validation du token JWT dans les autres microservices
3. Implémenter le refresh token
4. Ajouter la gestion des rôles et permissions
5. Implémenter la réinitialisation du mot de passe

## Support

Pour toute question ou problème, consultez la documentation Spring Boot:
- https://spring.io/projects/spring-boot
- https://spring.io/projects/spring-security
