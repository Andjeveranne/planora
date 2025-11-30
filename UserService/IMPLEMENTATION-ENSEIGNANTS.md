# Implémentation - Gestion des Enseignants

## Vue d'ensemble

Cette implémentation ajoute la fonctionnalité complète de gestion des enseignants dans Plannora, permettant aux administrateurs de :
- Visualiser la liste des enseignants
- Ajouter de nouveaux enseignants
- Supprimer des enseignants
- Voir les détails (spécialité, département)

## Architecture

### Backend (UserService)

#### Endpoints API

| Méthode | Endpoint | Description | Rôle requis |
|---------|----------|-------------|-------------|
| GET | `/api/utilisateurs/enseignants` | Liste tous les enseignants | ADMIN, ENSEIGNANT |
| POST | `/api/utilisateurs/enseignant` | Crée un nouvel enseignant | ADMIN |
| GET | `/api/utilisateurs/{id}` | Récupère un enseignant par ID | ADMIN |
| PUT | `/api/utilisateurs/{id}` | Met à jour un enseignant | ADMIN |
| DELETE | `/api/utilisateurs/{id}` | Supprime un enseignant | ADMIN |

#### Modèles de données

**Enseignant** (extends Utilisateur)
```java
- idUser: String (UUID)
- email: String
- mdp: String (hashé)
- nomUser: String
- prenomUser: String
- telephone: String
- role: String (ENSEIGNANT)
- specialite: String
- departement: String
```

**EnseignantDTO** (Request)
```java
- email: String (required, valid email)
- mdp: String (required, min 6 chars)
- nomUser: String (required)
- prenomUser: String (required)
- telephone: String (required)
- role: String (ENSEIGNANT)
- specialite: String (optional)
- departement: String (optional)
```

**UtilisateurResponseDTO** (Response)
```java
- idUser: String
- email: String
- nomUser: String
- prenomUser: String
- telephone: String
- role: String
- specialite: String
- departement: String
```

#### Sécurité

- Authentification JWT requise pour tous les endpoints
- Autorisation basée sur les rôles (@PreAuthorize)
- Mots de passe hashés avec BCrypt
- Validation des données avec Jakarta Validation

### Frontend (Angular)

#### Service UserService

**Méthodes** :
- `getEnseignants()`: Observable<Enseignant[]>
- `creerEnseignant(enseignant)`: Observable<Enseignant>
- `updateEnseignant(id, enseignant)`: Observable<Enseignant>
- `deleteEnseignant(id)`: Observable<void>

**Configuration** :
- URL de base : `http://localhost:8888/user-service/api/utilisateurs`
- Headers : Authorization Bearer token + Content-Type JSON

#### Composant AdminDashboard

**Nouvelles propriétés** :
- `enseignants: Enseignant[]` - Liste des enseignants
- `showAddEnseignantForm: boolean` - Affichage du formulaire
- `newEnseignant: EnseignantRequest` - Données du formulaire
- `isLoadingEnseignants: boolean` - État de chargement
- `errorMessage: string` - Messages d'erreur
- `successMessage: string` - Messages de succès

**Nouvelles méthodes** :
- `loadEnseignants()` - Charge la liste depuis l'API
- `toggleAddEnseignantForm()` - Affiche/cache le formulaire
- `ajouterEnseignant()` - Crée un nouvel enseignant
- `deleteEnseignant(id)` - Supprime un enseignant
- `validateForm()` - Valide les données du formulaire
- `resetForm()` - Réinitialise le formulaire

#### Interface utilisateur

**Section Enseignants** :
1. **En-tête** :
   - Barre de recherche (à implémenter)
   - Bouton "Exporter la liste"
   - Bouton "Ajouter un enseignant"

2. **Formulaire d'ajout** (conditionnel) :
   - Champs : Nom, Prénom, Email, Téléphone
   - Champs optionnels : Spécialité, Département
   - Champ : Mot de passe
   - Boutons : Annuler, Ajouter

3. **Liste des enseignants** :
   - Table avec colonnes : Nom, Prénom, Email, Téléphone, Spécialité, Département, Actions
   - Actions : Modifier (icône), Supprimer (icône)
   - États : Chargement, Liste vide, Liste avec données

4. **Messages** :
   - Succès (vert) : Confirmation des actions
   - Erreur (rouge) : Messages d'erreur

## Fichiers modifiés/créés

### Backend

**Modifiés** :
- `UserService/user-service/src/main/java/com/isi4/userservice/dto/UtilisateurResponseDTO.java`
  - Ajout des champs `specialite` et `departement`

- `UserService/user-service/src/main/java/com/isi4/userservice/service/UtilisateurService.java`
  - Mise à jour de `mapToResponseDTO()` pour inclure les champs enseignant

**Existants (utilisés)** :
- `UserService/user-service/src/main/java/com/isi4/userservice/controller/UtilisateurController.java`
- `UserService/user-service/src/main/java/com/isi4/userservice/model/Enseignant.java`
- `UserService/user-service/src/main/java/com/isi4/userservice/dto/EnseignantDTO.java`

### Frontend

**Créés** :
- `Frontend/plannora-frontend/src/app/services/user.service.ts`
  - Service Angular pour les appels API utilisateurs

**Modifiés** :
- `Frontend/plannora-frontend/src/app/admin-dashboard/admin-dashboard.component.ts`
  - Ajout de la logique de gestion des enseignants
  - Import de FormsModule et UserService

- `Frontend/plannora-frontend/src/app/admin-dashboard/admin-dashboard.component.html`
  - Ajout de la section enseignants avec formulaire et liste
  - Correction du nom de section (users → enseignants)

- `Frontend/plannora-frontend/src/app/admin-dashboard/admin-dashboard.component.css`
  - Ajout des styles pour le formulaire, la table et les alertes

### Documentation

**Créés** :
- `UserService/test-enseignants.http`
  - Fichier de tests HTTP pour l'API

- `UserService/GUIDE-TEST-ENSEIGNANTS.md`
  - Guide complet de test de la fonctionnalité

- `UserService/IMPLEMENTATION-ENSEIGNANTS.md`
  - Ce document

## Flux de données

### Chargement de la liste

```
1. Utilisateur clique sur "Enseignants" dans le menu
2. AdminDashboard.setActiveSection('enseignants')
3. AdminDashboard.loadEnseignants()
4. UserService.getEnseignants()
5. HTTP GET → Gateway → UserService
6. UserService retourne List<UtilisateurResponseDTO>
7. Affichage dans la table
```

### Ajout d'un enseignant

```
1. Utilisateur clique sur "Ajouter un enseignant"
2. Formulaire s'affiche
3. Utilisateur remplit les champs
4. Utilisateur clique sur "Ajouter l'enseignant"
5. AdminDashboard.ajouterEnseignant()
6. Validation du formulaire
7. UserService.creerEnseignant(newEnseignant)
8. HTTP POST → Gateway → UserService
9. UserService crée l'enseignant en BD
10. Retour de l'enseignant créé
11. Ajout à la liste locale
12. Affichage du message de succès
13. Fermeture du formulaire
```

### Suppression d'un enseignant

```
1. Utilisateur clique sur l'icône de suppression
2. Confirmation demandée
3. AdminDashboard.deleteEnseignant(id)
4. UserService.deleteEnseignant(id)
5. HTTP DELETE → Gateway → UserService
6. UserService supprime l'enseignant de la BD
7. Retour code 204
8. Suppression de la liste locale
9. Affichage du message de succès
```

## Validations

### Backend (Spring Validation)

- Email : Format valide (@Email)
- Mot de passe : Non vide (@NotBlank)
- Nom : Non vide (@NotBlank)
- Prénom : Non vide (@NotBlank)
- Téléphone : Non vide (@NotBlank)
- Email unique : Vérification en base de données

### Frontend (TypeScript)

- Tous les champs obligatoires remplis
- Email : Format valide (regex)
- Mot de passe : Minimum 6 caractères
- Messages d'erreur clairs et en français

## Sécurité

### Authentification

- Token JWT requis pour tous les appels API
- Token stocké dans localStorage
- Token envoyé dans l'en-tête Authorization

### Autorisation

- Seuls les ADMIN peuvent créer/modifier/supprimer des enseignants
- Les ENSEIGNANT peuvent voir la liste (pour futures fonctionnalités)
- Vérification côté backend avec @PreAuthorize

### Protection des données

- Mots de passe hashés avec BCrypt
- Mots de passe jamais retournés dans les réponses API
- Validation des entrées côté client et serveur

## Tests recommandés

### Tests unitaires (à implémenter)

**Backend** :
- UtilisateurServiceTest
  - testCreerEnseignant()
  - testGetEnseignants()
  - testDeleteEnseignant()
  - testEmailDuplique()

**Frontend** :
- UserServiceSpec
  - should get enseignants
  - should create enseignant
  - should delete enseignant
- AdminDashboardComponentSpec
  - should load enseignants on section change
  - should validate form
  - should add enseignant

### Tests d'intégration

- Test du flux complet : Login → Liste → Ajout → Suppression
- Test des erreurs : Token invalide, Email dupliqué, etc.
- Test de la sécurité : Accès sans token, rôle insuffisant

### Tests E2E (à implémenter)

- Scénario complet utilisateur administrateur
- Vérification de l'interface utilisateur
- Vérification des messages d'erreur/succès

## Améliorations futures

### Fonctionnalités

1. **Recherche et filtrage**
   - Recherche par nom, email, spécialité
   - Filtres par département
   - Tri des colonnes

2. **Modification d'enseignant**
   - Modal de modification
   - Mise à jour des informations
   - Changement de mot de passe

3. **Pagination**
   - Liste paginée pour grandes quantités
   - Sélection du nombre d'éléments par page

4. **Export**
   - Export CSV/Excel de la liste
   - Export PDF avec mise en forme

5. **Import en masse**
   - Import CSV d'enseignants
   - Validation et rapport d'erreurs

### Technique

1. **Gestion d'état**
   - Utilisation de NgRx ou Akita
   - Cache des données

2. **Optimisation**
   - Lazy loading des données
   - Virtual scrolling pour grandes listes

3. **Tests**
   - Couverture de tests à 80%+
   - Tests E2E automatisés

4. **Accessibilité**
   - Support clavier complet
   - ARIA labels
   - Contraste des couleurs

## Conclusion

L'implémentation de la gestion des enseignants est complète et fonctionnelle. Elle respecte les bonnes pratiques de développement :
- Architecture en couches
- Séparation des responsabilités
- Sécurité renforcée
- Interface utilisateur intuitive
- Code maintenable et extensible

La fonctionnalité est prête pour les tests et peut être étendue avec les améliorations suggérées.
