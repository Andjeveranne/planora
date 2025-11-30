# Dépannage - Gestion des Enseignants

## Problème: La liste ne charge pas

### Étape 1: Vérifier la console du navigateur

1. Ouvrez le navigateur (http://localhost:4200)
2. Appuyez sur **F12** pour ouvrir les outils de développement
3. Allez dans l'onglet **Console**
4. Cliquez sur "Enseignants" dans le menu
5. Regardez les messages dans la console

#### Messages possibles:

**🔍 "Chargement des enseignants..."**
- ✅ La fonction est appelée correctement

**❌ "Erreur lors du chargement des enseignants: status 0"**
- ❌ Le serveur ne répond pas
- **Solution**: Vérifiez que tous les services sont démarrés

**❌ "Erreur lors du chargement des enseignants: status 401"**
- ❌ Token invalide ou expiré
- **Solution**: Déconnectez-vous et reconnectez-vous

**❌ "Erreur lors du chargement des enseignants: status 403"**
- ❌ Permissions insuffisantes
- **Solution**: Vérifiez que vous êtes connecté en tant qu'ADMIN

**❌ "Erreur lors du chargement des enseignants: status 404"**
- ❌ Endpoint non trouvé
- **Solution**: Vérifiez que le UserService est enregistré dans Eureka

### Étape 2: Vérifier l'onglet Network

1. Dans les outils de développement, allez dans **Network** (Réseau)
2. Cliquez sur "Enseignants" dans le menu
3. Cherchez la requête vers `/user-service/api/utilisateurs/enseignants`

#### Vérifications:

**La requête n'apparaît pas**
- Le code ne s'exécute pas
- Vérifiez la console pour les erreurs JavaScript

**Status: (failed) ou (canceled)**
- Le serveur ne répond pas
- Vérifiez que les services sont démarrés

**Status: 401 Unauthorized**
- Cliquez sur la requête
- Allez dans **Headers**
- Vérifiez que `Authorization: Bearer ...` est présent
- Si absent: Le token n'est pas dans localStorage
- **Solution**: Reconnectez-vous

**Status: 403 Forbidden**
- Le token est valide mais les permissions sont insuffisantes
- **Solution**: Vérifiez le rôle dans la base de données

**Status: 404 Not Found**
- L'endpoint n'existe pas ou le service n'est pas accessible
- **Solution**: Vérifiez Eureka (http://localhost:8761)

**Status: 500 Internal Server Error**
- Erreur côté serveur
- **Solution**: Vérifiez les logs du UserService

### Étape 3: Vérifier les services backend

Exécutez le script de diagnostic:

```powershell
.\diagnostic-enseignants.ps1
```

Ce script vérifie:
- ✅ Tous les services sont démarrés
- ✅ La connexion fonctionne
- ✅ L'endpoint /enseignants est accessible
- ✅ La création d'enseignant fonctionne

### Étape 4: Vérifier le token

Dans la console du navigateur, tapez:

```javascript
localStorage.getItem('token')
```

**Si null ou undefined**:
- Vous n'êtes pas connecté
- **Solution**: Reconnectez-vous

**Si présent**:
- Copiez le token
- Allez sur https://jwt.io
- Collez le token
- Vérifiez:
  - `role`: doit être "ADMIN"
  - `exp`: ne doit pas être expiré

### Étape 5: Vérifier Eureka

1. Ouvrez http://localhost:8761
2. Vérifiez que **USER-SERVICE** est listé
3. Si absent:
   - Le UserService n'est pas démarré
   - Ou il n'arrive pas à s'enregistrer

## Problème: L'ajout ne fonctionne pas

### Vérifications:

**1. Le formulaire ne s'affiche pas**
- Cliquez sur le bouton "+ Ajouter un enseignant"
- Vérifiez la console pour les erreurs

**2. Le bouton "Ajouter l'enseignant" ne fait rien**
- Vérifiez que tous les champs obligatoires sont remplis
- Regardez la console pour les messages de validation

**3. Erreur lors de l'ajout**
- Regardez la console du navigateur
- Regardez l'onglet Network
- Vérifiez le status de la requête POST

**4. Email déjà existant**
- Message: "Un utilisateur avec cet email existe déjà"
- **Solution**: Utilisez un autre email

## Solutions rapides

### Redémarrer tout

```powershell
# Arrêter tous les services (Ctrl+C dans chaque terminal)

# Redémarrer
.\demarrer-services.ps1
```

### Vérifier la base de données

```powershell
# Se connecter à PostgreSQL
psql -U postgres -d plannora_users

# Vérifier les enseignants
SELECT * FROM utilisateurs WHERE role = 'ENSEIGNANT';

# Vérifier l'admin
SELECT * FROM utilisateurs WHERE role = 'ADMIN';
```

### Recréer l'utilisateur admin

Si l'admin n'existe pas ou a un problème:

```sql
-- Dans psql
DELETE FROM utilisateurs WHERE email = 'admin@plannora.com';

-- Redémarrer le UserService
-- Le DataInitializer va recréer l'admin
```

### Vider le cache du navigateur

1. Ouvrez les outils de développement (F12)
2. Clic droit sur le bouton de rafraîchissement
3. Choisissez "Vider le cache et actualiser"

### Vérifier les CORS

Si vous voyez des erreurs CORS dans la console:

1. Vérifiez la configuration du Gateway
2. Assurez-vous que `http://localhost:4200` est autorisé
3. Redémarrez le Gateway

## Checklist complète

- [ ] Tous les services sont démarrés (Gateway, Eureka, Auth, User, Frontend)
- [ ] Eureka affiche USER-SERVICE (http://localhost:8761)
- [ ] La connexion fonctionne (admin@plannora.com / admin123)
- [ ] Le token est présent dans localStorage
- [ ] Le token n'est pas expiré
- [ ] Le rôle est ADMIN
- [ ] L'endpoint /enseignants répond (test avec diagnostic-enseignants.ps1)
- [ ] La console du navigateur ne montre pas d'erreurs
- [ ] L'onglet Network montre des requêtes réussies

## Commandes utiles

```powershell
# Diagnostic complet
.\diagnostic-enseignants.ps1

# Test de l'API
.\test-enseignants.ps1

# Vérifier les ports utilisés
netstat -ano | findstr "8888 8761 8081 8082 4200"

# Logs du UserService
# Regardez dans le terminal où le UserService est démarré
```

## Encore des problèmes?

1. **Vérifiez les logs** de chaque service dans leurs terminaux respectifs
2. **Activez le mode debug** en ajoutant `logging.level.root=DEBUG` dans application.properties
3. **Testez l'API directement** avec Postman ou le fichier test-enseignants.http
4. **Vérifiez la base de données** pour voir si les données sont bien présentes

## Contact

Si le problème persiste après toutes ces vérifications, fournissez:
- Les logs de la console du navigateur
- Les logs du UserService
- Le résultat de `diagnostic-enseignants.ps1`
- Les captures d'écran des erreurs
