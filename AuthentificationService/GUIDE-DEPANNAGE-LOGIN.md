# Guide de Dépannage - Problèmes de Connexion

## Problème: Impossible de se connecter avec les identifiants de la base de données

### Identifiants de Test

Les identifiants suivants sont créés automatiquement au démarrage du service :

- **Admin**: `admin@plannora.com` / `password123`
- **Enseignant**: `enseignant@plannora.com` / `password123`

### Solutions

#### 1. Redémarrer le Service d'Authentification

Le `DataInitializer` recrée automatiquement les utilisateurs de test à chaque démarrage :

```powershell
# Arrêter le service
cd AuthentificationService/Authentification/authentification
./mvnw spring-boot:stop

# Redémarrer le service
./mvnw spring-boot:run
```

#### 2. Vérifier que le Service est Démarré

```powershell
# Tester l'endpoint de debug
curl http://localhost:8085/api/auth/debug/users
```

Vous devriez voir : `{"totalUsers": 2, "message": "Nombre d'utilisateurs dans la base"}`

**Note:** Le service d'authentification utilise maintenant le port **8085** (et non 8082)

#### 3. Tester la Connexion avec le Script

```powershell
cd AuthentificationService
./test-login.ps1
```

Ce script teste :
- Le nombre d'utilisateurs dans la base
- La connexion Admin
- La connexion Enseignant
- Le rejet des mauvais mots de passe

#### 4. Vérifier les Logs du Service

Regardez les logs au démarrage du service. Vous devriez voir :

```
✅ Utilisateurs de test créés avec succès!
📧 admin@plannora.com / password123
📧 enseignant@plannora.com / password123
```

#### 5. Vérifier la Base de Données

Si vous utilisez MySQL :

```sql
USE plannora_auth;
SELECT id, email, nom, prenom, role FROM utilisateurs;
```

#### 6. Problèmes Courants

**Erreur: "Email ou mot de passe incorrect"**
- Vérifiez que vous utilisez bien `password123` (et non `admin123`)
- Vérifiez qu'il n'y a pas d'espaces avant/après l'email ou le mot de passe
- Redémarrez le service d'authentification

**Erreur: "Connection refused"**
- Le service d'authentification n'est pas démarré
- Vérifiez le port 8085 : `Get-NetTCPConnection -LocalPort 8085`

**Erreur: "CORS"**
- Vérifiez que `@CrossOrigin(origins = "*")` est présent dans `AuthController`
- Redémarrez le service

### Test Manuel avec curl

```powershell
# Test de connexion Admin
curl -X POST http://localhost:8085/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"admin@plannora.com\",\"password\":\"password123\"}'

# Test de connexion Enseignant
curl -X POST http://localhost:8085/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"enseignant@plannora.com\",\"password\":\"password123\"}'
```

### Réinitialisation Complète

Si rien ne fonctionne, réinitialisez complètement :

```powershell
# 1. Arrêter tous les services
# 2. Supprimer la base de données
mysql -u root -p -e "DROP DATABASE IF EXISTS plannora_auth; CREATE DATABASE plannora_auth;"

# 3. Redémarrer le service d'authentification
cd AuthentificationService/Authentification/authentification
./mvnw spring-boot:run
```

### Vérification Frontend

Si le backend fonctionne mais pas le frontend :

1. Vérifiez la console du navigateur (F12)
2. Vérifiez l'onglet Network pour voir la requête HTTP
3. Vérifiez que l'URL de l'API est correcte : `http://localhost:8085/api/auth`
4. Le frontend a été mis à jour pour utiliser le port 8085

### Support

Si le problème persiste :
1. Exécutez `./test-login.ps1` et partagez les résultats
2. Vérifiez les logs du service d'authentification
3. Vérifiez la console du navigateur
