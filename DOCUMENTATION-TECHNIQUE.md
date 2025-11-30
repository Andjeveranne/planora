# Documentation Technique - Plannora

## Vue d'ensemble

Plannora est une application de gestion d'emploi du temps pour établissements d'enseignement, construite avec une architecture microservices.

## Architecture Microservices

### Principe de fonctionnement

L'application est divisée en services indépendants qui communiquent entre eux. Chaque service a sa propre responsabilité et peut être déployé/mis à jour indépendamment.

```
Frontend (Angular) → Gateway → Services Backend
                              ↓
                           Eureka (Discovery)
                              ↓
                           MySQL (Base de données)
```

### Services implémentés

| Service | Port | Rôle |
|---------|------|------|
| **Eureka** | 8761 | Registre de services - permet aux services de se découvrir |
| **Gateway** | 8081 | Point d'entrée unique - route les requêtes vers les bons services |
| **Authentification** | 8085 | Gestion de l'authentification et génération de tokens JWT |
| **UserService** | 8086 | Gestion des utilisateurs (Admin, Enseignants) |
| **Frontend** | 4200 | Interface utilisateur Angular |

## Flux d'authentification (JWT)

### Concept JWT

JWT (JSON Web Token) est un standard pour transmettre des informations de manière sécurisée. Un token JWT contient :
- **Header** : Type de token et algorithme de signature
- **Payload** : Données utilisateur (email, rôle, ID)
- **Signature** : Garantit que le token n'a pas été modifié

### Flow complet

```
1. Utilisateur → Login (email/password) → Frontend
2. Frontend → POST /api/auth/login → Service Auth (8085)
3. Service Auth vérifie les identifiants dans MySQL
4. Si OK : Service Auth génère un JWT token
5. Service Auth → Retourne {token, user info} → Frontend
6. Frontend stocke le token dans localStorage
7. Frontend redirige vers le dashboard approprié

Pour les requêtes suivantes :
8. Frontend → Requête + Header "Authorization: Bearer TOKEN" → Backend
9. Backend vérifie le token avec JwtAuthenticationFilter
10. Si valide : Traite la requête
11. Si invalide : Retourne 401 Unauthorized
```

### Sécurité JWT

**JwtTokenProvider** : Génère et valide les tokens
```java
// Génération
String token = Jwts.builder()
    .setSubject(email)
    .claim("userId", userId)
    .claim("role", role)
    .setIssuedAt(new Date())
    .setExpiration(new Date(System.currentTimeMillis() + expiration))
    .signWith(key, SignatureAlgorithm.HS512)
    .compact();
```

**JwtAuthenticationFilter** : Intercepte chaque requête HTTP
- Extrait le token du header `Authorization`
- Valide le token (signature, expiration)
- Charge les informations utilisateur dans le contexte Spring Security
- Permet ou refuse l'accès selon le rôle

## Base de données

### Structure

**Base unique** : `PlannoraDB` (MySQL 8.0+)

Tous les services partagent la même base pour assurer la cohérence des données.

### Table principale : utilisateurs

```sql
CREATE TABLE utilisateurs (
    id_user VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    mdp VARCHAR(255) NOT NULL,  -- BCrypt hashé
    nom_user VARCHAR(255),
    prenom_user VARCHAR(255),
    telephone VARCHAR(20),
    role ENUM('ADMIN', 'ENSEIGNANT', 'ETUDIANT'),
    dtype VARCHAR(31)  -- Discriminator pour l'héritage JPA
);

CREATE TABLE administrateurs (
    id_user VARCHAR(255) PRIMARY KEY,
    FOREIGN KEY (id_user) REFERENCES utilisateurs(id_user)
);

CREATE TABLE enseignants (
    id_user VARCHAR(255) PRIMARY KEY,
    specialite VARCHAR(255),
    departement VARCHAR(255),
    FOREIGN KEY (id_user) REFERENCES utilisateurs(id_user)
);
```

### Héritage JPA (JOINED Strategy)

Plannora utilise l'héritage JPA pour modéliser les différents types d'utilisateurs :

```
Utilisateur (classe abstraite)
    ↓
    ├── Administrateur
    └── Enseignant
```

**Avantages** :
- Évite la duplication de code
- Permet le polymorphisme
- Requêtes optimisées avec jointures SQL

**Configuration** :
```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
public abstract class Utilisateur {
    @Id
    private String idUser;
    private String email;
    private String mdp;
    // ...
}

@Entity
@DiscriminatorValue("ADMINISTRATEUR")
public class Administrateur extends Utilisateur {
    // Champs spécifiques admin
}
```

## Service Discovery avec Eureka

### Principe

Eureka est un registre de services. Chaque microservice s'enregistre au démarrage et peut découvrir les autres services.

### Fonctionnement

1. **Démarrage d'un service** :
   - Le service contacte Eureka (http://localhost:8761)
   - S'enregistre avec son nom et son adresse
   - Envoie un "heartbeat" toutes les 30 secondes

2. **Communication entre services** :
   - Service A veut appeler Service B
   - Service A demande à Eureka l'adresse de Service B
   - Service A appelle directement Service B

3. **Load Balancing** :
   - Si plusieurs instances d'un service existent
   - Eureka distribue les requêtes entre elles

### Configuration

```properties
# Service qui s'enregistre
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
spring.application.name=user-service
```

## Gateway - Point d'entrée unique

### Rôle

La Gateway est le seul point d'entrée pour le frontend. Elle route les requêtes vers les bons services.

### Avantages

- **Sécurité** : Un seul point à sécuriser (CORS, authentification)
- **Simplicité** : Le frontend n'a qu'une URL à connaître
- **Flexibilité** : Peut ajouter des filtres (logging, rate limiting)

### Configuration des routes

```properties
# Route vers le service d'authentification
spring.cloud.gateway.routes[0].id=authentification-service
spring.cloud.gateway.routes[0].uri=lb://authentification
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**
```

**lb://** signifie "Load Balanced" - la Gateway utilise Eureka pour trouver le service.

## Frontend Angular

### Architecture

```
src/app/
├── login/                    # Page de connexion
├── admin-dashboard/          # Dashboard administrateur
├── enseignant-dashboard/     # Dashboard enseignant
└── services/
    └── auth.service.ts       # Service d'authentification
```

### Gestion de l'authentification

**AuthService** :
- Stocke le token JWT dans `localStorage`
- Vérifie si l'utilisateur est connecté
- Gère la déconnexion
- Fournit les informations utilisateur

**Guards** (à implémenter) :
- Protègent les routes selon le rôle
- Redirigent vers login si non authentifié

### Communication avec le backend

```typescript
// Exemple de requête authentifiée
this.http.post('http://localhost:8085/api/auth/login', credentials)
  .subscribe(response => {
    localStorage.setItem('token', response.token);
    localStorage.setItem('currentUser', JSON.stringify(response));
  });
```

## Sécurité

### Hashage des mots de passe (BCrypt)

Les mots de passe ne sont **jamais** stockés en clair. BCrypt génère un hash unique avec un "salt" aléatoire.

```java
// Lors de l'inscription
String hashedPassword = passwordEncoder.encode("password123");
// Résultat : $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

// Lors de la connexion
boolean matches = passwordEncoder.matches(rawPassword, hashedPassword);
```

**Avantages** :
- Même mot de passe → hash différent à chaque fois (grâce au salt)
- Impossible de retrouver le mot de passe depuis le hash
- Résistant aux attaques par force brute (algorithme lent volontairement)

### CORS (Cross-Origin Resource Sharing)

Permet au frontend (port 4200) d'appeler le backend (port 8085).

```java
@CrossOrigin(origins = "*")  // En production : spécifier l'origine exacte
public class AuthController {
    // ...
}
```

### Configuration MySQL sécurisée

```properties
# allowPublicKeyRetrieval=true nécessaire pour MySQL 8.0+
# MySQL 8 utilise caching_sha2_password par défaut
spring.datasource.url=jdbc:mysql://localhost:3306/PlannoraDB?allowPublicKeyRetrieval=true
```

## Initialisation des données

### DataInitializer

Chaque service a un `DataInitializer` qui s'exécute au démarrage :

```java
@Component
public class DataInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // Crée les utilisateurs de test si la base est vide
        if (!userRepository.existsById("admin001")) {
            // Créer admin avec mot de passe hashé
        }
    }
}
```

**Utilisateurs créés automatiquement** :
- Admin : admin@plannora.com / password123
- Enseignant : enseignant@plannora.com / password123

## Gestion des erreurs

### Erreurs courantes et solutions

**1. "Public Key Retrieval is not allowed"**
- Cause : Configuration MySQL 8.0+
- Solution : Ajouter `allowPublicKeyRetrieval=true` dans l'URL JDBC

**2. "Duplicate entry for key PRIMARY"**
- Cause : Tentative de créer un utilisateur qui existe déjà
- Solution : Vérifier l'existence avant insertion avec `findById().isEmpty()`

**3. "401 Unauthorized"**
- Cause : Token JWT invalide ou expiré
- Solution : Se reconnecter pour obtenir un nouveau token

**4. Service ne démarre pas**
- Cause : Port déjà utilisé ou MySQL non accessible
- Solution : Vérifier les ports avec `Get-NetTCPConnection` et démarrer MySQL

## Déploiement

### Ordre de démarrage

1. **MySQL** - Base de données
2. **Eureka** - Registre de services
3. **Services métier** (Auth, User) - S'enregistrent dans Eureka
4. **Gateway** - Route vers les services
5. **Frontend** - Interface utilisateur

### Script automatique

```powershell
./demarrer-plannora.ps1
```

Ce script :
- Vérifie MySQL
- Démarre tous les services dans le bon ordre
- Attend que chaque service soit prêt
- Affiche les URLs et identifiants

## Tests

### Test de l'authentification

```powershell
./test-rapide.ps1
```

### Test manuel avec curl

```bash
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@plannora.com","password":"password123"}'
```

### Diagnostic complet

```powershell
./diagnostic-auth.ps1
```

Vérifie :
- État des services (ports ouverts)
- Connexion à la base de données
- Authentification fonctionnelle
- Configuration du frontend

## Évolutions futures

### Services à implémenter

- **PlanningService** : Gestion des emplois du temps
- **ReservationService** : Réservation de salles
- **SalleService** : Gestion des salles et équipements
- **NotificationService** : Notifications en temps réel
- **ReportingService** : Génération de rapports

### Améliorations techniques

- **Refresh tokens** : Renouveler le JWT sans se reconnecter
- **Rate limiting** : Limiter le nombre de requêtes par utilisateur
- **Caching** : Redis pour améliorer les performances
- **Monitoring** : Spring Boot Actuator + Prometheus
- **CI/CD** : Pipeline automatisé de déploiement
- **Docker** : Conteneurisation des services

## Ressources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security JWT](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Angular Documentation](https://angular.io/docs)
- [Netflix Eureka](https://spring.io/guides/gs/service-registration-and-discovery/)
- [JWT.io](https://jwt.io/) - Décodeur de tokens JWT
