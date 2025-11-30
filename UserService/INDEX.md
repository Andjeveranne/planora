# 📚 Index de la Documentation - UserService

Bienvenue dans la documentation du UserService ! Ce fichier vous guide vers la bonne documentation selon vos besoins.

> 👋 **Première visite ?** Consultez [BIENVENUE.md](BIENVENUE.md) pour un accueil complet !

## 🚀 Démarrage Rapide

**Vous voulez démarrer rapidement ?**
→ Consultez [DEMARRAGE-RAPIDE.md](DEMARRAGE-RAPIDE.md)

**Vous voulez une checklist de vérification ?**
→ Consultez [CHECKLIST-DEMARRAGE.md](CHECKLIST-DEMARRAGE.md)

## 📖 Documentation Complète

**Vous voulez comprendre le service en détail ?**
→ Consultez [README.md](README.md)

**Vous voulez comprendre l'architecture ?**
→ Consultez [ARCHITECTURE.md](ARCHITECTURE.md)

**Vous voulez un résumé de l'implémentation ?**
→ Consultez [IMPLEMENTATION-COMPLETE.md](IMPLEMENTATION-COMPLETE.md)

**Vous voulez un résumé court ?**
→ Consultez [RESUME-IMPLEMENTATION.md](RESUME-IMPLEMENTATION.md)

## 🧪 Tests

**Vous voulez tester avec Postman ?**
→ Consultez [GUIDE-TESTS-POSTMAN.md](GUIDE-TESTS-POSTMAN.md)
→ Utilisez [test-user-api.http](test-user-api.http)

**Vous préférez cURL ?**
→ Consultez [EXEMPLES-CURL.md](EXEMPLES-CURL.md)

## 🗄️ Base de Données

**Vous voulez initialiser la base de données ?**
→ Consultez [init-database.sql](init-database.sql)

## 📂 Structure de la Documentation

```
UserService/
├── INDEX.md                          ← Vous êtes ici
├── README.md                         ← Documentation complète
├── DEMARRAGE-RAPIDE.md              ← Guide de démarrage (5 min)
├── CHECKLIST-DEMARRAGE.md           ← Checklist de vérification
├── ARCHITECTURE.md                   ← Architecture détaillée
├── IMPLEMENTATION-COMPLETE.md        ← Résumé de l'implémentation
├── RESUME-IMPLEMENTATION.md          ← Résumé court
├── GUIDE-TESTS-POSTMAN.md           ← Guide des tests Postman
├── EXEMPLES-CURL.md                 ← Exemples cURL
├── test-user-api.http               ← Collection de requêtes
├── init-database.sql                ← Script SQL
└── user-service/                    ← Code source
    ├── pom.xml
    ├── .gitignore
    └── src/
        ├── main/java/com/isi4/userservice/
        │   ├── UserServiceApplication.java
        │   ├── config/
        │   ├── controller/
        │   ├── dto/
        │   ├── exception/
        │   ├── model/
        │   ├── repository/
        │   ├── security/
        │   └── service/
        └── main/resources/
            └── application.properties
```

## 🎯 Par Cas d'Usage

### Je veux démarrer le service pour la première fois
1. [CHECKLIST-DEMARRAGE.md](CHECKLIST-DEMARRAGE.md) - Vérifier les prérequis
2. [DEMARRAGE-RAPIDE.md](DEMARRAGE-RAPIDE.md) - Démarrer en 5 minutes
3. [GUIDE-TESTS-POSTMAN.md](GUIDE-TESTS-POSTMAN.md) - Tester avec Postman

### Je veux comprendre comment ça marche
1. [README.md](README.md) - Vue d'ensemble
2. [ARCHITECTURE.md](ARCHITECTURE.md) - Architecture détaillée
3. [IMPLEMENTATION-COMPLETE.md](IMPLEMENTATION-COMPLETE.md) - Détails de l'implémentation

### Je veux tester l'API
1. [GUIDE-TESTS-POSTMAN.md](GUIDE-TESTS-POSTMAN.md) - Tests avec Postman
2. [test-user-api.http](test-user-api.http) - Collection de requêtes
3. [EXEMPLES-CURL.md](EXEMPLES-CURL.md) - Tests avec cURL

### Je veux modifier le code
1. [ARCHITECTURE.md](ARCHITECTURE.md) - Comprendre l'architecture
2. [README.md](README.md) - Documentation de référence
3. Code source dans `user-service/src/`

### Je rencontre un problème
1. [CHECKLIST-DEMARRAGE.md](CHECKLIST-DEMARRAGE.md) - Section Dépannage
2. [README.md](README.md) - Section Dépannage
3. Vérifier les logs du service

## 📊 Résumé Rapide

### Qu'est-ce que le UserService ?
Service de gestion des utilisateurs (Administrateurs et Enseignants) pour l'application Plannora.

### Fonctionnalités Principales
- ✅ CRUD des utilisateurs (ADMIN uniquement)
- ✅ Héritage : Utilisateur → Administrateur / Enseignant
- ✅ Authentification JWT
- ✅ Base de données unique : `plannoradb`
- ✅ Sécurité par rôle

### Technologies
- Spring Boot 3.5.7
- Spring Data JPA
- Spring Security
- JWT
- MySQL
- Eureka

### Ports
- **8083** : UserService
- **8082** : Authentication Service
- **8761** : Eureka Server
- **3306** : MySQL

### Compte par Défaut
- **Email** : admin@plannora.com
- **Mot de passe** : admin123
- **Rôle** : ADMIN

## 🔗 Liens Rapides

| Document | Description | Temps de lecture |
|----------|-------------|------------------|
| [DEMARRAGE-RAPIDE.md](DEMARRAGE-RAPIDE.md) | Démarrage en 5 minutes | 5 min |
| [CHECKLIST-DEMARRAGE.md](CHECKLIST-DEMARRAGE.md) | Checklist de vérification | 10 min |
| [GUIDE-TESTS-POSTMAN.md](GUIDE-TESTS-POSTMAN.md) | Guide des tests | 15 min |
| [README.md](README.md) | Documentation complète | 20 min |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Architecture détaillée | 15 min |
| [EXEMPLES-CURL.md](EXEMPLES-CURL.md) | Exemples cURL | 10 min |
| [IMPLEMENTATION-COMPLETE.md](IMPLEMENTATION-COMPLETE.md) | Résumé implémentation | 10 min |
| [RESUME-IMPLEMENTATION.md](RESUME-IMPLEMENTATION.md) | Résumé court | 5 min |

## 💡 Conseils

1. **Première fois ?** Commencez par [DEMARRAGE-RAPIDE.md](DEMARRAGE-RAPIDE.md)
2. **Problème ?** Consultez la section Dépannage dans [CHECKLIST-DEMARRAGE.md](CHECKLIST-DEMARRAGE.md)
3. **Tests ?** Utilisez [test-user-api.http](test-user-api.http) avec Postman ou REST Client
4. **Développement ?** Lisez [ARCHITECTURE.md](ARCHITECTURE.md) pour comprendre la structure

## 📞 Support

Si vous ne trouvez pas ce que vous cherchez :
1. Consultez [README.md](README.md) pour la documentation complète
2. Vérifiez les logs du service
3. Consultez la base de données MySQL

---

**Bonne lecture ! 📚**
