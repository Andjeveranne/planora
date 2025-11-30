# Modification - Requête des Enseignants

## Changement effectué

Au lieu de chercher dans la table `enseignants`, le système récupère maintenant tous les utilisateurs ayant le rôle "ENSEIGNANT" depuis la table `utilisateurs`.

## Fichiers modifiés

### 1. UtilisateurRepository.java

**Ajout de la méthode**:
```java
List<Utilisateur> findByRole(String role);
```

Cette méthode permet de récupérer tous les utilisateurs par rôle.

### 2. UtilisateurService.java

**Avant**:
```java
public List<UtilisateurResponseDTO> getEnseignants() {
    return enseignantRepository.findAll().stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
}
```

**Après**:
```java
public List<UtilisateurResponseDTO> getEnseignants() {
    // Récupérer tous les utilisateurs avec le rôle ENSEIGNANT depuis la table utilisateurs
    return utilisateurRepository.findByRole("ENSEIGNANT").stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
}
```

## Avantages

1. **Plus simple**: Une seule table à interroger
2. **Plus flexible**: Fonctionne même si la table `enseignants` est vide
3. **Cohérent**: Utilise le champ `role` comme source de vérité
4. **Performant**: Requête directe avec filtre SQL

## Requête SQL générée

Spring Data JPA génère automatiquement:
```sql
SELECT * FROM utilisateurs WHERE role = 'ENSEIGNANT'
```

## Test

### 1. Redémarrer le UserService

```powershell
.\redemarrer-userservice.ps1
```

OU manuellement:
```powershell
cd UserService/user-service
# Ctrl+C pour arrêter
mvn spring-boot:run
```

### 2. Tester l'API

```powershell
.\test-direct-enseignants.ps1
```

### 3. Vérifier le résultat

Le script devrait afficher tous les utilisateurs avec `role = "ENSEIGNANT"`, qu'ils soient dans la table `enseignants` ou non.

## Comportement

### Avant
- Cherchait uniquement dans la table `enseignants`
- Ne trouvait que les utilisateurs de type Enseignant (héritage JPA)

### Après
- Cherche dans la table `utilisateurs` avec filtre sur le rôle
- Trouve tous les utilisateurs ayant `role = "ENSEIGNANT"`
- Plus flexible et cohérent avec la structure de données

## Impact

✅ Aucun impact sur le frontend
✅ Aucun impact sur l'API (même endpoint, même format de réponse)
✅ Amélioration de la logique métier
✅ Plus facile à maintenir

## Prochaines étapes

1. Redémarrer le UserService
2. Tester avec le script
3. Tester avec le frontend
4. Tout devrait fonctionner comme avant, mais avec une meilleure logique!
