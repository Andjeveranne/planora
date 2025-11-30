# Guide de Test - Gestion des Enseignants

## Vue d'ensemble
Ce guide explique comment tester la fonctionnalité de gestion des enseignants dans Plannora.

## Prérequis

1. **Services démarrés** :
   - EurekaService (port 8761)
   - GatewayService (port 8888)
   - AuthentificationService (port 8081)
   - UserService (port 8082)
   - Frontend (port 4200)

2. **Base de données** :
   - PostgreSQL en cours d'exécution
   - Base de données `plannora_users` créée
   - Migrations exécutées

## Démarrage des services

```powershell
# Depuis la racine du projet
.\demarrer-services.ps1
```

## Test Backend (API REST)

### 1. Connexion en tant qu'administrateur

```http
POST http://localhost:8888/auth-service/api/auth/login
Content-Type: application/json

{
  "email": "admin@plannora.com",
  "mdp": "admin123"
}
```

**Réponse attendue** :
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": "...",
  "email": "admin@plannora.com",
  "nom": "Admin",
  "prenom": "Super",
  "role": "ADMIN"
}
```

**Important** : Copiez le token pour les requêtes suivantes.

### 2. Récupérer la liste des enseignants

```http
GET http://localhost:8888/user-service/api/utilisateurs/enseignants
Authorization: Bearer VOTRE_TOKEN_ICI
```

**Réponse attendue** :
```json
[
  {
    "idUser": "...",
    "email": "enseignant@plannora.com",
    "nomUser": "Diop",
    "prenomUser": "Amadou",
    "telephone": "+221 77 123 45 67",
    "role": "ENSEIGNANT",
    "specialite": "Informatique",
    "departement": "Sciences et Technologies"
  }
]
```

### 3. Créer un nouvel enseignant

```http
POST http://localhost:8888/user-service/api/utilisateurs/enseignant
Authorization: Bearer VOTRE_TOKEN_ICI
Content-Type: application/json

{
  "email": "nouvel.enseignant@plannora.com",
  "mdp": "password123",
  "nomUser": "Sall",
  "prenomUser": "Fatou",
  "telephone": "+221 76 234 56 78",
  "role": "ENSEIGNANT",
  "specialite": "Mathématiques",
  "departement": "Sciences Exactes"
}
```

**Réponse attendue** : Code 201 Created avec les données de l'enseignant créé.

### 4. Supprimer un enseignant

```http
DELETE http://localhost:8888/user-service/api/utilisateurs/{idUser}
Authorization: Bearer VOTRE_TOKEN_ICI
```

**Réponse attendue** : Code 204 No Content

## Test Frontend (Interface Web)

### 1. Connexion

1. Ouvrez votre navigateur : `http://localhost:4200`
2. Connectez-vous avec :
   - Email : `admin@plannora.com`
   - Mot de passe : `admin123`

### 2. Accéder à la gestion des enseignants

1. Dans le dashboard administrateur, cliquez sur **"Enseignants"** dans le menu latéral
2. La liste des enseignants devrait se charger automatiquement

### 3. Ajouter un enseignant

1. Cliquez sur le bouton **"+ Ajouter un enseignant"**
2. Remplissez le formulaire :
   - **Nom** : Diop
   - **Prénom** : Amadou
   - **Email** : amadou.diop@plannora.com
   - **Téléphone** : +221 77 123 45 67
   - **Spécialité** : Informatique
   - **Département** : Sciences et Technologies
   - **Mot de passe** : password123
3. Cliquez sur **"Ajouter l'enseignant"**
4. Un message de succès devrait apparaître
5. L'enseignant devrait apparaître dans la liste

### 4. Supprimer un enseignant

1. Dans la liste des enseignants, cliquez sur l'icône de corbeille (🗑️)
2. Confirmez la suppression
3. L'enseignant devrait disparaître de la liste

## Vérifications

### Console du navigateur

Ouvrez la console du navigateur (F12) pour vérifier :
- Aucune erreur JavaScript
- Les requêtes HTTP réussissent (code 200, 201, 204)
- Le token est bien envoyé dans les en-têtes

### Logs du backend

Vérifiez les logs du UserService pour :
- Les requêtes reçues
- Les opérations sur la base de données
- Les erreurs éventuelles

## Problèmes courants

### Erreur 401 Unauthorized

**Cause** : Token expiré ou invalide

**Solution** :
1. Reconnectez-vous pour obtenir un nouveau token
2. Vérifiez que le token est bien envoyé dans l'en-tête Authorization

### Erreur 403 Forbidden

**Cause** : L'utilisateur n'a pas le rôle ADMIN

**Solution** :
1. Vérifiez que vous êtes connecté en tant qu'administrateur
2. Vérifiez le rôle dans la base de données

### Liste vide

**Cause** : Aucun enseignant dans la base de données

**Solution** :
1. Ajoutez des enseignants via le formulaire
2. Vérifiez la connexion à la base de données

### Erreur CORS

**Cause** : Configuration CORS incorrecte

**Solution** :
1. Vérifiez la configuration CORS dans le GatewayService
2. Redémarrez les services

## Tests de validation

### Validation des champs

Testez les validations suivantes :

1. **Email invalide** : Entrez "test" → Erreur attendue
2. **Mot de passe court** : Entrez "123" → Erreur attendue
3. **Champs vides** : Laissez des champs vides → Erreur attendue
4. **Email en double** : Créez deux enseignants avec le même email → Erreur attendue

### Sécurité

1. **Sans token** : Essayez d'accéder à l'API sans token → Erreur 401
2. **Token expiré** : Utilisez un vieux token → Erreur 401
3. **Rôle insuffisant** : Connectez-vous en tant qu'enseignant et essayez d'ajouter un enseignant → Erreur 403

## Résultats attendus

✅ La liste des enseignants se charge correctement
✅ Le formulaire d'ajout s'affiche et se cache correctement
✅ Un nouvel enseignant peut être créé avec succès
✅ Les validations fonctionnent correctement
✅ Un enseignant peut être supprimé avec confirmation
✅ Les messages de succès/erreur s'affichent correctement
✅ L'interface est responsive et agréable à utiliser

## Support

En cas de problème, vérifiez :
1. Les logs des services backend
2. La console du navigateur
3. L'état de la base de données
4. La configuration des ports
