# Redémarrer le UserService

## Pourquoi redémarrer?

Nous avons ajouté la configuration CORS au UserService pour permettre au frontend (http://localhost:4200) de communiquer avec l'API.

## Étapes

### 1. Arrêter le UserService

Dans le terminal où le UserService est en cours d'exécution:
- Appuyez sur **Ctrl+C**

### 2. Recompiler (si nécessaire)

```powershell
cd UserService/user-service
mvn clean install -DskipTests
```

### 3. Redémarrer le UserService

```powershell
cd UserService/user-service
mvn spring-boot:run
```

OU si vous utilisez le script de démarrage:

```powershell
# Depuis la racine du projet
.\demarrer-services.ps1
```

### 4. Vérifier que le service est démarré

Attendez de voir dans les logs:
```
Started UserServiceApplication in X.XXX seconds
```

### 5. Tester l'API

```powershell
.\test-direct-enseignants.ps1
```

Vous devriez voir:
```
[OK] Connexion reussie
[OK] Liste recuperee avec succes
```

### 6. Tester le frontend

1. Ouvrez http://localhost:4200
2. Connectez-vous avec:
   - Email: `admin@plannora.com`
   - Mot de passe: `password123`
3. Cliquez sur "Enseignants"
4. La liste devrait se charger sans erreur

## Vérification CORS

Dans la console du navigateur (F12), vous ne devriez plus voir d'erreur CORS comme:
```
Access to XMLHttpRequest at 'http://localhost:8086/api/utilisateurs/enseignants' 
from origin 'http://localhost:4200' has been blocked by CORS policy
```

## Si le problème persiste

1. **Vider le cache du navigateur**:
   - F12 > Clic droit sur le bouton rafraîchir > "Vider le cache et actualiser"

2. **Vérifier les logs du UserService**:
   - Regardez dans le terminal du UserService
   - Cherchez des erreurs liées à CORS ou JWT

3. **Vérifier le token**:
   - Dans la console du navigateur: `localStorage.getItem('token')`
   - Si null, reconnectez-vous

4. **Redémarrer le frontend**:
   ```powershell
   cd Frontend/plannora-frontend
   # Ctrl+C pour arrêter
   npm start
   ```

## Configuration CORS ajoutée

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

Cette configuration permet:
- ✅ Requêtes depuis http://localhost:4200
- ✅ Méthodes GET, POST, PUT, DELETE, OPTIONS
- ✅ Tous les headers (y compris Authorization)
- ✅ Credentials (cookies, headers d'authentification)
