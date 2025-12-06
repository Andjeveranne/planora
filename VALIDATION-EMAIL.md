# Validation des Emails Institutionnels

## Format Requis

Le système n'accepte désormais **que les emails au format institutionnel** :

```
prenom.nom@saintjeaningenieur.org
```

## Règles de Validation

- **Format** : `prenom.nom@saintjeaningenieur.org`
- **Domaine** : Uniquement `@saintjeaningenieur.org`
- **Structure** : Prénom et nom séparés par un point
- **Caractères** : Lettres uniquement (a-z, A-Z)
- **Casse** : Insensible (accepte majuscules et minuscules)

## Exemples Valides

✅ `jean.dupont@saintjeaningenieur.org`
✅ `marie.martin@saintjeaningenieur.org`
✅ `admin.systeme@saintjeaningenieur.org`
✅ `Pierre.Durand@saintjeaningenieur.org` (majuscules acceptées)

## Exemples Invalides

❌ `admin@plannora.com` (mauvais domaine)
❌ `jeandupont@saintjeaningenieur.org` (pas de point entre prénom et nom)
❌ `jean.dupont@gmail.com` (mauvais domaine)
❌ `jean@saintjeaningenieur.org` (pas de nom)
❌ `jean.dupont.martin@saintjeaningenieur.org` (trop de parties)

## Nouveaux Identifiants de Test

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Admin | admin.systeme@saintjeaningenieur.org | password123 |
| Enseignant | jean.dupont@saintjeaningenieur.org | password123 |

## Implémentation Technique

### Services Concernés

1. **AuthentificationService**
   - Validation lors de l'inscription (`/api/auth/register`)
   - Validation lors de la connexion (`/api/auth/login`)

2. **UserService**
   - Validation lors de la création d'utilisateurs
   - Validation lors de la modification d'utilisateurs

### Classes de Validation

#### UserService
- `ValidInstitutionalEmail.java` - Annotation de validation
- `InstitutionalEmailValidator.java` - Logique de validation

#### AuthentificationService
- `ValidInstitutionalEmail.java` - Annotation de validation
- `InstitutionalEmailValidator.java` - Logique de validation

### Pattern Regex

```java
^[a-z]+\.[a-z]+@saintjeaningenieur\.org$
```

## Message d'Erreur

Lorsqu'un email invalide est fourni, le système retourne :

```json
{
  "email": "L'email doit être au format prenom.nom@saintjeaningenieur.org"
}
```

## Migration des Données Existantes

Si vous avez des utilisateurs existants avec d'autres formats d'email, vous devrez les mettre à jour manuellement dans la base de données pour respecter le nouveau format.

## Tests

Pour tester la validation :

```bash
# Test avec email valide
curl -X POST http://localhost:8085/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "marie.martin@saintjeaningenieur.org",
    "password": "password123",
    "nom": "Martin",
    "prenom": "Marie",
    "role": "ENSEIGNANT",
    "telephone": "0612345678"
  }'

# Test avec email invalide (devrait échouer)
curl -X POST http://localhost:8085/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "marie@gmail.com",
    "password": "password123",
    "nom": "Martin",
    "prenom": "Marie",
    "role": "ENSEIGNANT",
    "telephone": "0612345678"
  }'
```
