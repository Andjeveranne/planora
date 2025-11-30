# Solution Immédiate - Gestion des Enseignants

## 🔴 Problème actuel

Le frontend ne peut pas contacter le UserService à cause d'un problème CORS.

## ✅ Solution en 3 étapes

### Étape 1: Arrêter le UserService

Dans le terminal où le UserService tourne:
```
Ctrl+C
```

### Étape 2: Redémarrer le UserService

**Option A - Script automatique**:
```powershell
.\redemarrer-userservice.ps1
```

**Option B - Manuel**:
```powershell
cd UserService/user-service
mvn clean install -DskipTests
mvn spring-boot:run
```

### Étape 3: Tester

**Dans un NOUVEAU terminal**:
```powershell
.\test-direct-enseignants.ps1
```

Résultat attendu:
```
[OK] Connexion reussie
[OK] Liste recuperee avec succes
```

## 🌐 Tester le frontend

1. Ouvrez http://localhost:4200
2. Connectez-vous:
   - Email: `admin@plannora.com`
   - Mot de passe: `password123`
3. Cliquez sur "Enseignants"
4. La liste devrait se charger!

## 🔍 Vérification

### Console du navigateur (F12)

Vous devriez voir:
```
🔍 Chargement des enseignants...
Token: eyJhbGciOiJIUzI1NiJ9...
✅ Enseignants chargés: []
```

### Onglet Network (F12)

La requête vers `http://localhost:8086/api/utilisateurs/enseignants` devrait avoir:
- Status: **200 OK**
- Pas d'erreur CORS

## ❓ Pourquoi ça ne marchait pas?

Le UserService n'avait pas de configuration CORS. Il bloquait donc toutes les requêtes venant du frontend (http://localhost:4200).

## 📝 Ce qui a été modifié

**Fichier**: `UserService/user-service/src/main/java/com/isi4/userservice/config/SecurityConfig.java`

**Ajout**:
- Configuration CORS autorisant http://localhost:4200
- Méthodes GET, POST, PUT, DELETE autorisées
- Headers Authorization autorisés

## 🚀 Après le redémarrage

Tout devrait fonctionner:
- ✅ Chargement de la liste des enseignants
- ✅ Ajout d'enseignants
- ✅ Suppression d'enseignants
- ✅ Messages de succès/erreur

## 📞 Besoin d'aide?

Si ça ne fonctionne toujours pas:

1. **Vérifiez les logs du UserService**
   - Cherchez "Started UserServiceApplication"
   - Cherchez des erreurs

2. **Testez l'API directement**
   ```powershell
   .\test-direct-enseignants.ps1
   ```

3. **Vérifiez la console du navigateur**
   - F12 > Console
   - Regardez les erreurs

4. **Videz le cache**
   - F12 > Clic droit sur rafraîchir > "Vider le cache et actualiser"

## 🎯 Résumé

1. **Arrêter** le UserService (Ctrl+C)
2. **Redémarrer** avec `.\redemarrer-userservice.ps1`
3. **Tester** avec http://localhost:4200

C'est tout! 🎉
