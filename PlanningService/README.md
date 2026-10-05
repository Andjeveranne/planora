# Planning Service

Service responsable de la gestion des disponibilités des enseignants et de la génération des emplois du temps.

## Fonctionnalités

- **Gestion des disponibilités** : Enregistrement et consultation des créneaux de disponibilité des enseignants
- **Validation des données** : Vérification de la cohérence des dates et heures
- **Authentification JWT** : Sécurisation des endpoints avec tokens JWT
- **API RESTful** : Endpoints pour créer, consulter et supprimer les disponibilités

## Architecture

```
planning-service/
├── src/main/java/com/isi4/planningservice/
│   ├── entity/              # Entités JPA
│   ├── dto/                 # Data Transfer Objects
│   ├── repository/          # Accès aux données
│   ├── service/             # Logique métier
│   ├── controller/          # Endpoints REST
│   ├── security/            # Authentification JWT
│   └── config/              # Configuration
└── src/main/resources/
    └── application.properties
```

## Endpoints

### Disponibilités

- `POST /api/planning/disponibilites` - Ajouter une disponibilité
- `GET /api/planning/disponibilites/enseignant/{enseignantId}` - Récupérer les disponibilités d'un enseignant
- `DELETE /api/planning/disponibilites/{id}` - Supprimer une disponibilité
- `DELETE /api/planning/disponibilites/enseignant/{enseignantId}` - Supprimer toutes les disponibilités d'un enseignant

## Configuration

### Base de données

```properties
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:plannoradb}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
```

### JWT

```properties
jwt.secret=your-secret-key-change-this-in-production-environment-variable
jwt.expiration=86400000
```

## Démarrage

```bash
mvn clean install
mvn spring-boot:run
```

Le service démarre sur le port **8084**.

## Format des données

### DisponibiliteDTO

```json
{
  "id": 1,
  "enseignantId": "ens001",
  "dateHeureDebut": "2026-04-01T08:00:00",
  "dateHeureFin": "2026-04-01T10:00:00",
  "commentaire": "Available for morning classes"
}
```

## Validation

- `enseignantId` : Requis, non vide
- `dateHeureDebut` : Requis, doit être avant `dateHeureFin`
- `dateHeureFin` : Requis, doit être après `dateHeureDebut`
- Les deux dates doivent être le même jour
