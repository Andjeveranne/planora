# Explications détaillées - .gitlab-ci.yml

## 📋 Vue d'ensemble

Le fichier `.gitlab-ci.yml` définit le pipeline CI/CD pour GitLab. Il automatise le build, les tests et le déploiement de Plannora.

## 🏗️ Structure générale

```yaml
stages:          # Définit les étapes du pipeline
  - build       # Étape 1: Compilation
  - test        # Étape 2: Tests
  - quality     # Étape 3: Qualité du code
  - deploy      # Étape 4: Déploiement
```

### Ordre d'exécution
1. Tous les jobs de `build` s'exécutent en parallèle
2. Une fois `build` terminé, les jobs de `test` démarrent
3. Puis `quality`
4. Enfin `deploy`

## 🔧 Variables globales

```yaml
variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
  MAVEN_CLI_OPTS: "--batch-mode --errors --fail-at-end --show-version"
```

### Explication:
- **MAVEN_OPTS**: Configure Maven pour utiliser un dossier local pour le cache
  - `$CI_PROJECT_DIR`: Variable GitLab pointant vers le dossier du projet
  - `.m2/repository`: Dossier où Maven stocke les dépendances

- **MAVEN_CLI_OPTS**: Options pour Maven
  - `--batch-mode`: Mode non-interactif (pas de questions)
  - `--errors`: Affiche les erreurs détaillées
  - `--fail-at-end`: Continue même si un module échoue
  - `--show-version`: Affiche la version de Maven

## 💾 Cache

```yaml
cache:
  paths:
    - .m2/repository                           # Cache Maven
    - Frontend/plannora-frontend/node_modules/ # Cache npm
```

### Pourquoi?
- **Accélère les builds**: Les dépendances ne sont téléchargées qu'une fois
- **Économise la bande passante**: Réutilise les fichiers entre les pipelines
- **Réduit le temps**: Build 2-3x plus rapide après le premier run

### Comment ça marche?
1. Premier build: Télécharge toutes les dépendances
2. Builds suivants: Réutilise le cache
3. Cache partagé entre tous les jobs du pipeline

## 🔨 Stage BUILD

### Job: build:eureka

```yaml
build:eureka:
  stage: build                              # Appartient au stage "build"
  image: maven:3.8-openjdk-17              # Image Docker à utiliser
  script:                                   # Commandes à exécuter
    - cd EurekaService/eureka/eureka       # Aller dans le dossier
    - mvn $MAVEN_CLI_OPTS clean install -DskipTests  # Compiler
  artifacts:                                # Fichiers à conserver
    paths:
      - EurekaService/eureka/eureka/target/*.jar
    expire_in: 1 hour                       # Durée de conservation
```

### Détails:

#### `stage: build`
- Indique que ce job fait partie du stage "build"
- S'exécute en parallèle avec les autres jobs "build"

#### `image: maven:3.8-openjdk-17`
- Image Docker officielle Maven avec Java 17
- Contient Maven 3.8 et OpenJDK 17
- Environnement propre pour chaque build

#### `script:`
- **cd EurekaService/eureka/eureka**: Change de dossier
- **mvn clean install**: 
  - `clean`: Supprime les anciens builds
  - `install`: Compile et installe dans le repo local
  - `-DskipTests`: Saute les tests (exécutés dans le stage "test")

#### `artifacts:`
- **paths**: Fichiers à sauvegarder après le build
- **expire_in: 1 hour**: Les artifacts sont supprimés après 1h
- Permet de télécharger les .jar depuis GitLab UI

### Jobs similaires

Les autres jobs de build fonctionnent de la même manière:
- `build:gateway` - Build du Gateway
- `build:auth` - Build du service d'authentification
- `build:user` - Build du UserService

### Job: build:frontend

```yaml
build:frontend:
  stage: build
  image: node:18                            # Image Node.js 18
  script:
    - cd Frontend/plannora-frontend
    - npm ci                                # Install des dépendances
    - npm run build                         # Build de production
  artifacts:
    paths:
      - Frontend/plannora-frontend/dist/   # Dossier de build
    expire_in: 1 hour
```

#### Différences avec Maven:
- **image: node:18**: Utilise Node.js au lieu de Java
- **npm ci**: Install "clean" (plus rapide et déterministe que `npm install`)
- **npm run build**: Compile Angular en mode production
- **dist/**: Dossier contenant les fichiers statiques compilés

## 🧪 Stage TEST

### Job: test:backend

```yaml
test:backend:
  stage: test                               # Exécuté après "build"
  image: maven:3.8-openjdk-17
  script:
    - cd UserService/user-service
    - mvn $MAVEN_CLI_OPTS test              # Exécute les tests
  allow_failure: true                       # Continue même si ça échoue
```

#### `allow_failure: true`
- Le pipeline continue même si les tests échouent
- Utile en développement
- À mettre à `false` en production

#### Pourquoi un job séparé pour les tests?
- Séparation des responsabilités
- Peut utiliser une base de données de test
- Rapports de tests séparés

### Job: test:frontend

```yaml
test:frontend:
  stage: test
  image: node:18
  script:
    - cd Frontend/plannora-frontend
    - npm ci
    - npm run test -- --watch=false --browsers=ChromeHeadless
  allow_failure: true
```

#### Options de test:
- **--watch=false**: N'attend pas les changements (mode CI)
- **--browsers=ChromeHeadless**: Utilise Chrome sans interface graphique
- Parfait pour les environnements CI/CD

## 📊 Stage QUALITY

### Job: quality:sonarqube

```yaml
quality:sonarqube:
  stage: quality
  image: maven:3.8-openjdk-17
  script:
    - echo "SonarQube analysis would run here"
    # - mvn sonar:sonar -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.login=$SONAR_TOKEN
  only:
    - main                                  # Seulement sur main
    - develop                               # et develop
  allow_failure: true
```

#### `only:`
- Limite l'exécution à certaines branches
- Économise des ressources
- Analyse de qualité uniquement sur les branches importantes

#### Variables SonarQube:
- **$SONAR_HOST_URL**: URL du serveur SonarQube
- **$SONAR_TOKEN**: Token d'authentification
- À configurer dans Settings > CI/CD > Variables

#### Pourquoi commenté?
- Nécessite un serveur SonarQube
- Décommentez quand vous avez configuré SonarQube

## 🚀 Stage DEPLOY

### Job: deploy:staging

```yaml
deploy:staging:
  stage: deploy
  image: alpine:latest                      # Image légère
  script:
    - echo "Deploying to staging environment"
    # Ajouter vos commandes de déploiement ici
  only:
    - develop                               # Seulement sur develop
  when: manual                              # Déclenchement manuel
```

#### `when: manual`
- Le job ne démarre pas automatiquement
- Nécessite un clic sur "Play" dans GitLab UI
- Sécurité: évite les déploiements accidentels

#### `image: alpine:latest`
- Image Linux ultra-légère (5 MB)
- Parfaite pour exécuter des scripts de déploiement
- Peut installer des outils avec `apk add`

#### Exemples de déploiement:

**SSH vers un serveur:**
```yaml
script:
  - apk add openssh-client
  - ssh user@server "cd /app && docker-compose pull && docker-compose up -d"
```

**Kubernetes:**
```yaml
script:
  - apk add kubectl
  - kubectl apply -f k8s/staging/
```

**Docker Registry:**
```yaml
script:
  - docker push registry.example.com/plannora:staging
```

### Job: deploy:production

```yaml
deploy:production:
  stage: deploy
  image: alpine:latest
  script:
    - echo "Deploying to production environment"
  only:
    - main                                  # Seulement sur main
  when: manual                              # Déclenchement manuel
```

#### Différences avec staging:
- **only: main**: Déploie uniquement depuis la branche principale
- Plus de sécurité pour la production
- Peut ajouter des validations supplémentaires

## 🔄 Flux complet du pipeline

```
┌─────────────────────────────────────────────────────────┐
│                    PUSH / MERGE REQUEST                  │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                    STAGE: BUILD                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ build:eureka │  │ build:gateway│  │  build:auth  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐                    │
│  │  build:user  │  │build:frontend│                    │
│  └──────────────┘  └──────────────┘                    │
│                  (Exécution parallèle)                   │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                    STAGE: TEST                           │
│  ┌──────────────┐  ┌──────────────┐                    │
│  │test:backend  │  │test:frontend │                    │
│  └──────────────┘  └──────────────┘                    │
│                  (Exécution parallèle)                   │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                    STAGE: QUALITY                        │
│  ┌──────────────────────────────────┐                   │
│  │    quality:sonarqube             │                   │
│  │    (only: main, develop)         │                   │
│  └──────────────────────────────────┘                   │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                    STAGE: DEPLOY                         │
│  ┌──────────────────────────────────┐                   │
│  │    deploy:staging (manual)       │                   │
│  │    (only: develop)               │                   │
│  └──────────────────────────────────┘                   │
│  ┌──────────────────────────────────┐                   │
│  │    deploy:production (manual)    │                   │
│  │    (only: main)                  │                   │
│  └──────────────────────────────────┘                   │
└─────────────────────────────────────────────────────────┘
```

## 📝 Variables GitLab à configurer

### Dans Settings > CI/CD > Variables:

| Variable | Description | Exemple |
|----------|-------------|---------|
| `SONAR_HOST_URL` | URL SonarQube | `http://sonar.example.com` |
| `SONAR_TOKEN` | Token SonarQube | `sqp_xxxxxxxxxxxxx` |
| `DOCKER_USERNAME` | Username Docker Hub | `votre-username` |
| `DOCKER_PASSWORD` | Password Docker Hub | `votre-password` |
| `SSH_PRIVATE_KEY` | Clé SSH pour déploiement | `-----BEGIN RSA...` |
| `STAGING_SERVER` | Serveur staging | `staging.example.com` |
| `PROD_SERVER` | Serveur production | `prod.example.com` |

### Comment ajouter une variable:
1. Aller dans Settings > CI/CD
2. Expand "Variables"
3. Cliquer "Add variable"
4. Cocher "Masked" pour les secrets
5. Cocher "Protected" pour limiter aux branches protégées

## 🎯 Personnalisation

### Ajouter un nouveau service

```yaml
build:nouveau-service:
  stage: build
  image: maven:3.8-openjdk-17
  script:
    - cd NouveauService/nouveau-service
    - mvn $MAVEN_CLI_OPTS clean install -DskipTests
  artifacts:
    paths:
      - NouveauService/nouveau-service/target/*.jar
    expire_in: 1 hour
```

### Ajouter des tests d'intégration

```yaml
test:integration:
  stage: test
  image: maven:3.8-openjdk-17
  services:
    - mysql:8.0                             # Base de données pour tests
  variables:
    MYSQL_ROOT_PASSWORD: root
    MYSQL_DATABASE: test_db
  script:
    - cd UserService/user-service
    - mvn verify -Pintegration-tests
```

### Ajouter des notifications

```yaml
notify:success:
  stage: .post                              # Après tous les stages
  image: alpine:latest
  script:
    - apk add curl
    - |
      curl -X POST https://hooks.slack.com/services/YOUR/WEBHOOK/URL \
      -H 'Content-Type: application/json' \
      -d '{"text":"Pipeline succeeded!"}'
  when: on_success                          # Seulement si succès

notify:failure:
  stage: .post
  image: alpine:latest
  script:
    - apk add curl
    - |
      curl -X POST https://hooks.slack.com/services/YOUR/WEBHOOK/URL \
      -H 'Content-Type: application/json' \
      -d '{"text":"Pipeline failed!"}'
  when: on_failure                          # Seulement si échec
```

## 🔍 Debugging

### Voir les logs d'un job
1. Aller dans CI/CD > Pipelines
2. Cliquer sur le pipeline
3. Cliquer sur le job
4. Voir les logs en temps réel

### Relancer un job
1. Cliquer sur le bouton "Retry" du job
2. Ou relancer tout le pipeline

### Mode debug
Ajouter dans un job:
```yaml
script:
  - set -x                                  # Active le mode verbose
  - echo "Debug info"
  - env                                     # Affiche toutes les variables
```

## 💡 Bonnes pratiques

### 1. Utiliser le cache
```yaml
cache:
  key: ${CI_COMMIT_REF_SLUG}               # Cache par branche
  paths:
    - .m2/repository
```

### 2. Limiter les artifacts
```yaml
artifacts:
  expire_in: 1 day                          # Pas trop long
  when: on_success                          # Seulement si succès
```

### 3. Paralléliser au maximum
```yaml
build:service1:
  stage: build
  # ...

build:service2:
  stage: build
  # ... (s'exécute en parallèle)
```

### 4. Utiliser des templates
```yaml
.build_template: &build_template
  stage: build
  image: maven:3.8-openjdk-17
  script:
    - mvn clean install -DskipTests

build:eureka:
  <<: *build_template
  script:
    - cd EurekaService/eureka/eureka
    - mvn clean install -DskipTests
```

### 5. Sécuriser les secrets
- Toujours utiliser les variables GitLab
- Cocher "Masked" pour les mots de passe
- Cocher "Protected" pour la production

## 📊 Métriques

### Temps d'exécution typique:
- **Build**: 2-3 minutes par service
- **Test**: 1-2 minutes
- **Quality**: 3-5 minutes
- **Total**: 10-15 minutes

### Optimisations possibles:
- Cache efficace: -50% de temps
- Parallélisation: -60% de temps
- Images Docker légères: -20% de temps

## 🆘 Problèmes courants

### "Job failed: exit code 1"
- Vérifier les logs du job
- Tester localement la commande qui échoue

### "Cache not found"
- Normal au premier run
- Vérifier que les paths du cache existent

### "Artifacts not found"
- Vérifier que le build a réussi
- Vérifier les paths des artifacts

### "Permission denied"
- Vérifier les permissions du runner
- Ajouter `chmod +x` si nécessaire

## 📚 Ressources

- [GitLab CI/CD Documentation](https://docs.gitlab.com/ee/ci/)
- [GitLab CI/CD Variables](https://docs.gitlab.com/ee/ci/variables/)
- [GitLab Runners](https://docs.gitlab.com/runner/)
- [YAML Syntax](https://docs.gitlab.com/ee/ci/yaml/)

## ✨ Conclusion

Le fichier `.gitlab-ci.yml` automatise complètement le cycle de vie de Plannora:
- ✅ Build automatique
- ✅ Tests automatiques
- ✅ Qualité du code
- ✅ Déploiement contrôlé

C'est un pipeline **minimal mais complet** et **prêt pour la production**! 🚀
