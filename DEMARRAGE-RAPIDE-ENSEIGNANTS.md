# Démarrage Rapide - Gestion des Enseignants

## 🚀 Démarrage en 3 étapes

### 1. Démarrer tous les services
```powershell
.\demarrer-services.ps1
```

Attendez que tous les services soient démarrés (environ 2-3 minutes).

### 2. Ouvrir l'application
Ouvrez votre navigateur : **http://localhost:4200**

### 3. Se connecter
- **Email** : `admin@plannora.com`
- **Mot de passe** : `password123`

## 📋 Utilisation

### Accéder à la gestion des enseignants
1. Dans le dashboard, cliquez sur **"Enseignants"** dans le menu latéral gauche
2. La liste des enseignants se charge automatiquement

### Ajouter un enseignant
1. Cliquez sur **"+ Ajouter un enseignant"**
2. Remplissez le formulaire :
   - Nom : `Diop`
   - Prénom : `Amadou`
   - Email : `amadou.diop@plannora.com`
   - Téléphone : `+221 77 123 45 67`
   - Spécialité : `Informatique` (optionnel)
   - Département : `Sciences et Technologies` (optionnel)
   - Mot de passe : `password123`
3. Cliquez sur **"Ajouter l'enseignant"**
4. ✅ L'enseignant apparaît dans la liste

### Supprimer un enseignant
1. Dans la liste, cliquez sur l'icône 🗑️ (corbeille)
2. Confirmez la suppression
3. ✅ L'enseignant est supprimé

## 🧪 Test automatique

Pour tester l'API directement :
```powershell
.\test-enseignants.ps1
```

Ce script teste automatiquement :
- ✅ Connexion
- ✅ Récupération de la liste
- ✅ Création d'enseignant
- ✅ Modification
- ✅ Suppression

## 📊 Ports utilisés

| Service | Port |
|---------|------|
| Frontend | 4200 |
| Gateway | 8888 |
| Eureka | 8761 |
| Auth Service | 8081 |
| User Service | 8082 |
| PostgreSQL | 5432 |

## ⚠️ Problèmes courants

### Services ne démarrent pas
```powershell
# Vérifier que les ports ne sont pas utilisés
netstat -ano | findstr "8888 8761 8081 8082 4200"

# Redémarrer les services
.\demarrer-services.ps1
```

### Liste vide
- Ajoutez des enseignants via le formulaire
- Vérifiez que le UserService est démarré

### Erreur de connexion
- Vérifiez que tous les services sont démarrés
- Attendez 2-3 minutes après le démarrage
- Vérifiez les logs dans les consoles

## 📚 Documentation complète

- **Guide de test** : `UserService/GUIDE-TEST-ENSEIGNANTS.md`
- **Résumé** : `UserService/RESUME-GESTION-ENSEIGNANTS.md`
- **Implémentation** : `UserService/IMPLEMENTATION-ENSEIGNANTS.md`

## ✨ C'est tout !

Vous êtes prêt à gérer les enseignants dans Plannora ! 🎉
