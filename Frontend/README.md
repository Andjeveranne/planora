# Frontend Plannora - Angular

Application Angular pour le système de gestion de réservation de salles Plannora.

## Prérequis

- Node.js 18+ et npm
- Angular CLI 20+

## Installation

```bash
# Installer les dépendances locales
npm install
```

## Structure du projet

```
Frontend/
├── src/
│   ├── app/
│   │   ├── auth/
│   │   │   └── login/
│   │   │       ├── login.component.ts
│   │   │       ├── login.component.html
│   │   │       └── login.component.css
│   │   ├── services/
│   │   │   └── auth.service.ts
│   │   ├── interceptors/
│   │   │   └── auth.interceptor.ts
│   │   ├── guards/
│   │   │   └── auth.guard.ts
│   │   ├── app.module.ts
│   │   └── app-routing.module.ts
│   └── assets/
│       └── images/
│           ├── logo.png
│           └── teacher.jpg
```

## Configuration

L'API backend est configurée pour pointer vers la Gateway sur `http://localhost:8081`.

Si votre backend tourne sur un autre port, modifiez les URLs des services dans `src/app/services`.

## Démarrage

```bash
# Développement
npm start

# Production
npm run build
```

L'application sera accessible sur `http://localhost:4200`

## Fonctionnalités

- ✅ Connexion avec email/mot de passe
- ✅ Validation des formulaires
- ✅ Gestion des erreurs
- ✅ Stockage du token JWT
- ✅ Intercepteur HTTP pour ajouter le token
- ✅ Guard pour protéger les routes
- ✅ Redirection selon le rôle (ADMIN, ENSEIGNANT, ETUDIANT)
- ✅ Design responsive

## Utilisateurs de test

| Email | Mot de passe | Rôle |
|-------|--------------|------|
| admin@plannora.com | password123 | ADMIN |
| enseignant@plannora.com | password123 | ENSEIGNANT |
| etudiant@plannora.com | password123 | ETUDIANT |

## Prochaines étapes

1. Ajouter les composants pour les dashboards (admin, enseignant, étudiant)
2. Implémenter la gestion des réservations
3. Ajouter la gestion des salles
4. Créer les pages de profil utilisateur
5. Implémenter la réinitialisation du mot de passe
