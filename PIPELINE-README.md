# Pipeline CI/CD Minimal - Plannora

## 🚀 Démarrage rapide

### Option 1: Docker Compose (Recommandé)

```bash
# Démarrer tous les services
docker-compose up --build

# Accéder à l'application
http://localhost:80
```

### Option 2: Test local du pipeline

```powershell
.\test-pipeline.ps1
```

### Option 3: Scripts PowerShell (Développement)

```powershell
.\demarrer-services.ps1
```

## 📋 Fichiers du pipeline

| Fichier | Description |
|---------|-------------|
| `.github/workflows/ci-cd.yml` | Pipeline GitHub Actions |
| `.gitlab-ci.yml` | Pipeline GitLab CI/CD |
| `docker-compose.yml` | Orchestration Docker |
| `*/Dockerfile` | Images Docker des services |
| `test-pipeline.ps1` | Test local du pipeline |

## 🏗️ Architecture du pipeline

```
┌─────────────────────────────────────────────┐
│           Pipeline CI/CD                     │
├─────────────────────────────────────────────┤
│                                              │
│  1. Build                                    │
│     ├─ Backend Services (Maven)             │
│     └─ Frontend (npm)                        │
│                                              │
│  2. Test                                     │
│     ├─ Unit Tests                            │
│     └─ Lint                                  │
│                                              │
│  3. Quality (optionnel)                      │
│     └─ SonarQube                             │
│                                              │
│  4. Docker                                   │
│     ├─ Build Images                          │
│     └─ Push to Registry                      │
│                                              │
│  5. Deploy (manuel)                          │
│     ├─ Staging                               │
│     └─ Production                            │
│                                              │
└─────────────────────────────────────────────┘
```

## 🐳 Services Docker

| Service | Port | Image |
|---------|------|-------|
| MySQL | 3306 | mysql:8.0 |
| Eureka | 8761 | plannora/eureka |
| Gateway | 8081 | plannora/gateway |
| Auth | 8085 | plannora/auth |
| User | 8086 | plannora/user |
| Frontend | 80 | plannora/frontend |

## 📝 Commandes utiles

### Docker Compose

```bash
# Démarrer
docker-compose up -d

# Voir les logs
docker-compose logs -f

# Arrêter
docker-compose down

# Rebuild
docker-compose up --build

# Nettoyer
docker-compose down -v
```

### Docker

```bash
# Build une image
docker build -t plannora/user-service ./UserService/user-service

# Lister les images
docker images | grep plannora

# Supprimer les images
docker rmi plannora/user-service
```

### Maven

```bash
# Build
mvn clean install -DskipTests

# Tests
mvn test

# Package
mvn package
```

### npm

```bash
# Install
npm ci

# Build
npm run build

# Test
npm test

# Lint
npm run lint
```

## 🔧 Configuration

### GitHub Actions

1. Créer les secrets dans Settings > Secrets:
   - `DOCKER_USERNAME`
   - `DOCKER_PASSWORD`

2. Le pipeline se déclenche automatiquement sur push/PR

### GitLab CI/CD

1. Créer les variables dans Settings > CI/CD > Variables:
   - `DOCKER_USERNAME`
   - `DOCKER_PASSWORD`

2. Configurer un runner GitLab

### Docker Compose

1. Modifier `docker-compose.yml` si nécessaire
2. Ajuster les variables d'environnement

## 🧪 Tests

### Test complet du pipeline

```powershell
.\test-pipeline.ps1
```

### Test d'un service spécifique

```bash
# Backend
cd UserService/user-service
mvn test

# Frontend
cd Frontend/plannora-frontend
npm test
```

## 📊 Monitoring

### Health Checks

```bash
# Eureka
curl http://localhost:8761/actuator/health

# User Service
curl http://localhost:8086/actuator/health
```

### Logs

```bash
# Docker Compose
docker-compose logs -f user-service

# Docker
docker logs plannora-user -f
```

## 🔒 Sécurité

- ✅ Secrets gérés via GitHub/GitLab
- ✅ Images Docker multi-stage
- ✅ Pas de credentials dans le code
- ✅ Variables d'environnement

## 📚 Documentation

- [PIPELINE-GUIDE.md](PIPELINE-GUIDE.md) - Guide complet
- [docker-compose.yml](docker-compose.yml) - Configuration Docker
- [.github/workflows/ci-cd.yml](.github/workflows/ci-cd.yml) - GitHub Actions
- [.gitlab-ci.yml](.gitlab-ci.yml) - GitLab CI/CD

## 🎯 Prochaines étapes

1. ✅ Pipeline de base créé
2. ⏳ Ajouter tests d'intégration
3. ⏳ Configurer SonarQube
4. ⏳ Ajouter monitoring
5. ⏳ Automatiser le déploiement

## 💡 Conseils

- Utilisez Docker Compose pour le développement local
- Testez le pipeline localement avant de push
- Gardez les images Docker légères
- Utilisez le cache pour accélérer les builds
- Documentez les changements

## 🆘 Support

En cas de problème:
1. Vérifiez les logs: `docker-compose logs`
2. Testez localement: `.\test-pipeline.ps1`
3. Consultez: [PIPELINE-GUIDE.md](PIPELINE-GUIDE.md)
