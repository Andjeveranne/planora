# Configuration des Ports - Plannora

## Ports utilisés

| Service | Port | URL |
|---------|------|-----|
| Eureka | 8761 | http://localhost:8761 |
| Gateway | 8081 | http://localhost:8081 |
| AuthentificationService | 8085 | http://localhost:8085 |
| UserService | 8086 | http://localhost:8086 |
| Frontend | 4200 | http://localhost:4200 |

## Credentials par défaut

### Administrateur
- **Email**: `admin@plannora.com`
- **Mot de passe**: `password123`

### Enseignant (test)
- **Email**: `enseignant@plannora.com`
- **Mot de passe**: `password123`

## URLs des API

### Authentification
- Login: `http://localhost:8085/api/auth/login`
- Register: `http://localhost:8085/api/auth/register`

### User Service (via accès direct)
- Liste enseignants: `http://localhost:8086/api/utilisateurs/enseignants`
- Créer enseignant: `http://localhost:8086/api/utilisateurs/enseignant`
- Modifier enseignant: `http://localhost:8086/api/utilisateurs/{id}`
- Supprimer enseignant: `http://localhost:8086/api/utilisateurs/{id}`

## Configuration Frontend

Le frontend Angular doit utiliser:
- **Auth Service**: `http://localhost:8085/api/auth`
- **User Service**: `http://localhost:8081/user-service/api/utilisateurs` (via Gateway)
  OU
- **User Service**: `http://localhost:8086/api/utilisateurs` (accès direct)

## Note importante

Le Gateway (port 8081) peut router les requêtes vers les différents services, mais actuellement la configuration des routes n'est pas complète. Il est recommandé d'accéder directement aux services pour le moment.

## Vérification

Pour vérifier que tous les services sont démarrés:

```powershell
netstat -ano | findstr "LISTENING" | findstr ":80"
```

Vous devriez voir:
- 8761 (Eureka)
- 8081 (Gateway)
- 8085 (Auth)
- 8086 (User)
