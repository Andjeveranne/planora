# Guide Pipeline CI/CD - Plannora

## Vue d'ensemble

Ce projet inclut des pipelines CI/CD pour automatiser le build, les tests et le déploiement.

## Fichiers de pipeline

### 1. GitHub Actions
**Fichier**: `.github/workflows/ci-cd.yml`

**Déclencheurs**:
- Push sur `main` ou `develop`
- Pull requests vers `main` ou `develop`

**Jobs**:
1. **backend-build**: Build de tous les services backend (Maven)
2. **frontend-build**: Build du frontend Angular (npm)
3. **integration-tests**: Tests d'intégration (optionnel)
4. **code-quality**: Analyse de qualité du code (SonarQube)
5. **docker-build**: Build des images Docker
6. **notify**: Notifications de statut

### 2. GitLab CI/CD
**Fichier**: `.gitlab-ci.yml`

**Stages**:
1. **build**: Compilation de tous les services
2. **test**: Exécution des tests
3. **quality**: Analyse de qualité
4. **deploy**: Déploiement (staging/production)

## Docker

### Dockerfiles créés

1. **EurekaService/eureka/eureka/Dockerfile**
2. **UserService/user-service/Dockerfile**
3. **Frontend/plannora-frontend/Dockerfile**

### Docker Compose

**Fichier**: `docker-compose.yml`

**Services**:
- `mysql`: Base de données MySQL 8.0
- `eureka`: Service Discovery (port 8761)
- `gateway`: API Gateway (port 8081)
- `auth-service`: Service d'authentification (port 8085)
- `user-service`: Service utilisateurs (port 8086)
- `frontend`: Application Angular (port 80)

## Utilisation

### Développement local

#### Option 1: Scripts PowerShell (Windows)
```powershell
.\demarrer-services.ps1
```

#### Option 2: Docker Compose
```bash
# Build et démarrer tous les services
docker-compose up --build

# Démarrer en arrière-plan
docker-compose up -d

# Voir les logs
docker-compose logs -f

# Arrêter les services
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v
```

### CI/CD avec GitHub Actions

#### Configuration requise

1. **Secrets GitHub** (Settings > Secrets):
   - `DOCKER_USERNAME`: Nom d'utilisateur Docker Hub
   - `DOCKER_PASSWORD`: Mot de passe Docker Hub
   - `SONAR_HOST_URL`: URL SonarQube (optionnel)
   - `SONAR_TOKEN`: Token SonarQube (optionnel)

2. **Activer GitHub Actions**:
   - Aller dans Settings > Actions > General
   - Autoriser les workflows

#### Workflow automatique

Le pipeline se déclenche automatiquement sur:
- Push vers `main` ou `develop`
- Pull request vers `main` ou `develop`

### CI/CD avec GitLab

#### Configuration requise

1. **Variables CI/CD** (Settings > CI/CD > Variables):
   - `SONAR_HOST_URL`: URL SonarQube
   - `SONAR_TOKEN`: Token SonarQube
   - `DOCKER_USERNAME`: Nom d'utilisateur Docker Hub
   - `DOCKER_PASSWORD`: Mot de passe Docker Hub

2. **Runners GitLab**:
   - Configurer un runner GitLab
   - Ou utiliser les runners partagés

#### Déploiement manuel

Les jobs de déploiement sont configurés en mode `manual`:
```yaml
when: manual
```

Pour déployer:
1. Aller dans CI/CD > Pipelines
2. Cliquer sur le pipeline
3. Cliquer sur le bouton "Play" du job de déploiement

## Build des images Docker

### Build individuel

```bash
# Eureka
docker build -t plannora/eureka:latest ./EurekaService/eureka/eureka

# User Service
docker build -t plannora/user-service:latest ./UserService/user-service

# Frontend
docker build -t plannora/frontend:latest ./Frontend/plannora-frontend
```

### Push vers Docker Hub

```bash
# Login
docker login

# Tag et push
docker tag plannora/user-service:latest votre-username/plannora-user-service:latest
docker push votre-username/plannora-user-service:latest
```

## Tests

### Tests backend (Maven)

```bash
cd UserService/user-service
mvn test
```

### Tests frontend (npm)

```bash
cd Frontend/plannora-frontend
npm test
```

### Tests d'intégration

```bash
# À implémenter
./run-integration-tests.sh
```

## Qualité du code

### SonarQube (optionnel)

```bash
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your-token
```

### Linting

```bash
# Frontend
cd Frontend/plannora-frontend
npm run lint
```

## Déploiement

### Environnements

1. **Local**: Scripts PowerShell ou Docker Compose
2. **Staging**: Déploiement automatique sur `develop`
3. **Production**: Déploiement manuel sur `main`

### Stratégies de déploiement

#### Blue-Green Deployment
```bash
# Déployer la nouvelle version (green)
docker-compose -f docker-compose.green.yml up -d

# Tester
curl http://green.plannora.com/health

# Basculer le trafic
# Arrêter l'ancienne version (blue)
docker-compose -f docker-compose.blue.yml down
```

#### Rolling Update
```bash
# Mettre à jour service par service
docker-compose up -d --no-deps --build user-service
docker-compose up -d --no-deps --build auth-service
```

## Monitoring

### Health Checks

```bash
# Eureka
curl http://localhost:8761/actuator/health

# User Service
curl http://localhost:8086/actuator/health

# Frontend
curl http://localhost:80
```

### Logs Docker

```bash
# Tous les services
docker-compose logs -f

# Service spécifique
docker-compose logs -f user-service

# Dernières 100 lignes
docker-compose logs --tail=100 user-service
```

## Troubleshooting

### Pipeline échoue

1. **Vérifier les logs**:
   - GitHub: Actions > Workflow > Job
   - GitLab: CI/CD > Pipelines > Job

2. **Problèmes courants**:
   - Dépendances manquantes
   - Tests qui échouent
   - Problèmes de connexion réseau

### Docker Compose échoue

```bash
# Vérifier les logs
docker-compose logs

# Reconstruire les images
docker-compose build --no-cache

# Nettoyer et redémarrer
docker-compose down -v
docker-compose up --build
```

### Services ne démarrent pas

```bash
# Vérifier l'état
docker-compose ps

# Vérifier les logs d'un service
docker-compose logs user-service

# Redémarrer un service
docker-compose restart user-service
```

## Optimisations

### Cache Maven

Le pipeline utilise le cache Maven pour accélérer les builds:
```yaml
cache:
  paths:
    - .m2/repository
```

### Cache npm

Le pipeline utilise le cache npm:
```yaml
cache:
  paths:
    - Frontend/plannora-frontend/node_modules/
```

### Multi-stage Docker builds

Les Dockerfiles utilisent des builds multi-étapes pour réduire la taille des images:
```dockerfile
FROM maven:3.8-openjdk-17 AS build
# ... build stage

FROM openjdk:17-jdk-slim
# ... runtime stage
```

## Sécurité

### Secrets

- Ne jamais commiter de secrets dans le code
- Utiliser les secrets GitHub/GitLab
- Utiliser des variables d'environnement

### Images Docker

- Utiliser des images officielles
- Scanner les vulnérabilités
- Mettre à jour régulièrement

### Dépendances

- Auditer les dépendances npm: `npm audit`
- Vérifier les dépendances Maven: `mvn dependency:tree`

## Prochaines étapes

1. ✅ Pipeline CI/CD de base créé
2. ⏳ Ajouter tests d'intégration
3. ⏳ Configurer SonarQube
4. ⏳ Ajouter monitoring (Prometheus/Grafana)
5. ⏳ Configurer déploiement automatique
6. ⏳ Ajouter notifications (Slack/Discord)

## Ressources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitLab CI/CD Documentation](https://docs.gitlab.com/ee/ci/)
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
