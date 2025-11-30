# Guide de Tests avec Postman - UserService

## Prérequis

1. **Services démarrés** :
   - Eureka Server (port 8761)
   - Gateway Service (port 8080)
   - Authentication Service (port 8082)
   - User Service (port 8083)

2. **Base de données MySQL** :
   - Base de données : `plannoradb`
   - Utilisateur : `root`
   - Mot de passe : (vide ou selon votre configuration)

## Étapes de Test

### 1. Démarrer les services

```bash
# Démarrer Eureka
cd EurekaService/eureka/eureka
mvn spring-boot:run

# Démarrer Gateway
cd GatewayService/gateway/gateway
mvn spring-boot:run

# Démarrer Authentication Service
cd AuthentificationService/Authentification/authentification
mvn spring-boot:run

# Démarrer User Service
cd UserService/user-service
mvn spring-boot:run
```

### 2. Authentification

**Endpoint** : `POST http://localhost:8082/api/auth/login`

**Body** :
```json
{
  "email": "admin@plannora.com",
  "password": "admin123"
}
```

**Réponse attendue** :
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "admin001",
  "role": "ADMIN"
}
```

**Important** : Copiez le token JWT pour l'utiliser dans les requêtes suivantes.

### 3. Tests CRUD des Utilisateurs

#### A. Créer un Enseignant (ADMIN uniquement)

**Endpoint** : `POST http://localhost:8083/api/utilisateurs/enseignant`

**Headers** :
- `Authorization: Bearer YOUR_JWT_TOKEN`
- `Content-Type: application/json`

**Body** :
```json
{
  "email": "enseignant1@plannora.com",
  "mdp": "password123",
  "nomUser": "Dupont",
  "prenomUser": "Jean",
  "telephone": "0612345678",
  "specialite": "Informatique",
  "departement": "Génie Logiciel"
}
```

**Réponse attendue** : `201 Created`
```json
{
  "idUser": "generated-uuid",
  "email": "enseignant1@plannora.com",
  "nomUser": "Dupont",
  "prenomUser": "Jean",
  "telephone": "0612345678",
  "role": "ENSEIGNANT"
}
```

#### B. Créer un Administrateur (ADMIN uniquement)

**Endpoint** : `POST http://localhost:8083/api/utilisateurs`

**Headers** :
- `Authorization: Bearer YOUR_JWT_TOKEN`
- `Content-Type: application/json`

**Body** :
```json
{
  "email": "admin2@plannora.com",
  "mdp": "password123",
  "nomUser": "Martin",
  "prenomUser": "Sophie",
  "telephone": "0623456789",
  "role": "ADMIN"
}
```

#### C. Lister tous les utilisateurs (ADMIN uniquement)

**Endpoint** : `GET http://localhost:8083/api/utilisateurs`

**Headers** :
- `Authorization: Bearer YOUR_JWT_TOKEN`

**Réponse attendue** : `200 OK`
```json
[
  {
    "idUser": "admin001",
    "email": "admin@plannora.com",
    "nomUser": "Admin",
    "prenomUser": "Système",
    "telephone": "0000000000",
    "role": "ADMIN"
  },
  ...
]
```

#### D. Lister tous les enseignants (ADMIN ou ENSEIGNANT)

**Endpoint** : `GET http://localhost:8083/api/utilisateurs/enseignants`

**Headers** :
- `Authorization: Bearer YOUR_JWT_TOKEN`

#### E. Obtenir un utilisateur par ID (ADMIN uniquement)

**Endpoint** : `GET http://localhost:8083/api/utilisateurs/{id}`

**Headers** :
- `Authorization: Bearer YOUR_JWT_TOKEN`

#### F. Mettre à jour un utilisateur (ADMIN uniquement)

**Endpoint** : `PUT http://localhost:8083/api/utilisateurs/{id}`

**Headers** :
- `Authorization: Bearer YOUR_JWT_TOKEN`
- `Content-Type: application/json`

**Body** :
```json
{
  "nomUser": "Dupont",
  "prenomUser": "Jean-Pierre",
  "telephone": "0699999999"
}
```

#### G. Supprimer un utilisateur (ADMIN uniquement)

**Endpoint** : `DELETE http://localhost:8083/api/utilisateurs/{id}`

**Headers** :
- `Authorization: Bearer YOUR_JWT_TOKEN`

**Réponse attendue** : `204 No Content`

## Tests de Sécurité

### 1. Accès sans token
Essayez d'accéder à n'importe quel endpoint sans le header `Authorization`.

**Résultat attendu** : `401 Unauthorized`

### 2. Accès avec un rôle insuffisant
Connectez-vous en tant qu'enseignant et essayez de créer un utilisateur.

**Résultat attendu** : `403 Forbidden`

## Collection Postman

Vous pouvez importer le fichier `test-user-api.http` dans Postman ou utiliser l'extension REST Client de VS Code.

### Configuration dans Postman

1. Créez une nouvelle collection "UserService"
2. Ajoutez une variable d'environnement `token` pour stocker le JWT
3. Configurez l'authentification Bearer Token automatique pour toute la collection

## Vérification dans la Base de Données

```sql
-- Voir tous les utilisateurs
SELECT * FROM utilisateurs;

-- Voir tous les enseignants
SELECT * FROM enseignants;

-- Voir tous les administrateurs
SELECT * FROM administrateurs;

-- Voir les détails complets d'un enseignant
SELECT u.*, e.specialite, e.departement 
FROM utilisateurs u 
JOIN enseignants e ON u.id_user = e.id_user;
```

## Erreurs Courantes

1. **401 Unauthorized** : Token manquant ou invalide
2. **403 Forbidden** : Rôle insuffisant pour l'opération
3. **400 Bad Request** : Données invalides (email déjà existant, champs manquants)
4. **404 Not Found** : Utilisateur non trouvé

## Notes

- Le mot de passe est automatiquement hashé avec BCrypt
- Les IDs sont générés automatiquement (UUID)
- L'héritage JPA utilise la stratégie JOINED (tables séparées)
- Seuls les administrateurs peuvent effectuer les opérations CRUD
