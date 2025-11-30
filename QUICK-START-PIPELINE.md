# Quick Start - Pipeline CI/CD

## 🚀 Démarrage en 3 commandes

### 1. Cloner le projet
```bash
git clone <votre-repo>
cd Plannora
```

### 2. Démarrer avec Docker Compose
```bash
docker-compose up --build
```

### 3. Accéder à l'application
```
http://localhost:80
```

**Credentials**:
- Email: `admin@plannora.com`
- Mot de passe: `password123`

## 📋 Commandes essentielles

### Docker Compose

```bash
# Démarrer
docker-compose up -d

# Voir les logs
docker-compose logs -f

# Arrêter
docker-compose down

# Rebuild
docker-compose up --build --force-recreate
```

### Test local du pipeline

```powershell
.\test-pipeline.ps1
```

## 🔧 Services disponibles

| Service | URL | Port |
|---------|-----|------|
| Frontend | http://localhost | 80 |
| Eureka | http://localhost:8761 | 8761 |
| Gateway | http://localhost:8081 | 8081 |
| Auth | http://localhost:8085 | 8085 |
| User | http://localhost:8086 | 8086 |
| MySQL | localhost:3306 | 3306 |

## 📝 Fichiers créés

```
Pipeline CI/CD:
├── .github/workflows/ci-cd.yml    # GitHub Actions
├── .gitlab-ci.yml                 # GitLab CI/CD
├── docker-compose.yml             # Orchestration
├── test-pipeline.ps1              # Test local
│
Dockerfiles:
├── EurekaService/.../Dockerfile
├── GatewayService/.../Dockerfile
├── AuthentificationService/.../Dockerfile
├── UserService/.../Dockerfile
└── Frontend/.../Dockerfile
│
Documentation:
├── PIPELINE-GUIDE.md              # Guide complet
├── PIPELINE-README.md             # Documentation
├── PIPELINE-SUMMARY.md            # Résumé
└── QUICK-START-PIPELINE.md        # Ce fichier
```

## ✅ Vérification

### 1. Services démarrés
```bash
docker-compose ps
```

Tous les services doivent être "Up"

### 2. Health checks
```bash
# Eureka
curl http://localhost:8761/actuator/health

# User Service
curl http://localhost:8086/actuator/health
```

### 3. Frontend
Ouvrir http://localhost et se connecter

## 🐛 Problèmes courants

### Services ne démarrent pas
```bash
# Voir les logs
docker-compose logs

# Redémarrer
docker-compose restart
```

### Port déjà utilisé
```bash
# Vérifier les ports
netstat -ano | findstr "80 8761 8081 8085 8086 3306"

# Arrêter les services existants
docker-compose down
```

### Images corrompues
```bash
# Nettoyer
docker-compose down -v
docker system prune -a

# Rebuild
docker-compose up --build
```

## 📚 Documentation complète

- [PIPELINE-GUIDE.md](PIPELINE-GUIDE.md) - Guide détaillé
- [PIPELINE-README.md](PIPELINE-README.md) - Documentation
- [PIPELINE-SUMMARY.md](PIPELINE-SUMMARY.md) - Résumé

## 🎯 Prochaines étapes

1. ✅ Démarrer avec Docker Compose
2. ✅ Tester l'application
3. ⏳ Configurer GitHub/GitLab
4. ⏳ Push et voir le pipeline en action

## 💡 Conseils

- Utilisez Docker Compose pour le développement
- Testez localement avec `test-pipeline.ps1`
- Consultez les logs avec `docker-compose logs -f`
- Gardez Docker à jour

C'est tout! Le pipeline est prêt à l'emploi! 🎉
