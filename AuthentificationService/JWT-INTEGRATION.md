# Intégration JWT dans les autres Microservices

Ce guide explique comment intégrer la validation JWT dans vos autres microservices (UserService, ReservationService, etc.)

## 1. Ajouter les dépendances

Dans le `pom.xml` de chaque microservice:

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## 2. Configuration dans application.properties

```properties
# JWT Configuration (doit être identique au service d'authentification)
jwt.secret=VotreCleSuperSecreteQuiDoitEtreTresLonguePourPlusDeSecurite2025
jwt.expiration=86400000
```

## 3. Copier la classe JwtUtil

Copiez la classe `JwtUtil.java` du service d'authentification dans votre microservice.

## 4. Créer un filtre JWT

```java
package com.isi4.votreservice.config;

import com.isi4.votreservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

## 5. Configuration de Spring Security

```java
package com.isi4.votreservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()  // Routes publiques
                .anyRequest().authenticated()                    // Toutes les autres routes nécessitent une authentification
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

## 6. Utilisation dans les contrôleurs

### Récupérer l'utilisateur connecté

```java
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    
    @GetMapping("/mes-reservations")
    public ResponseEntity<?> getMesReservations() {
        // Récupérer l'email de l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        // Utiliser l'email pour récupérer les données
        // ...
        
        return ResponseEntity.ok(reservations);
    }
}
```

### Vérifier les rôles (optionnel)

Si vous avez besoin de vérifier les rôles, vous pouvez extraire le rôle du token:

```java
// Dans JwtUtil.java, ajoutez cette méthode:
public String getRoleFromToken(String token) {
    Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    
    return claims.get("role", String.class);
}

// Dans le filtre JWT:
String role = jwtUtil.getRoleFromToken(token);
List<SimpleGrantedAuthority> authorities = 
    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

UsernamePasswordAuthenticationToken authentication = 
    new UsernamePasswordAuthenticationToken(email, null, authorities);
```

### Utiliser les annotations de sécurité

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        // Seulement accessible aux ADMIN
        return ResponseEntity.ok(users);
    }
}
```

N'oubliez pas d'ajouter `@EnableMethodSecurity` dans votre classe de configuration:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // ...
}
```

## 7. Test avec Postman/cURL

```bash
# 1. Se connecter pour obtenir un token
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@plannora.com","password":"password123"}'

# 2. Utiliser le token dans les autres services
curl -X GET http://localhost:8083/api/reservations/mes-reservations \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

## 8. Gestion des erreurs

Créez un gestionnaire d'exceptions global:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Accès refusé");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthentication(AuthenticationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Non authentifié");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
```

## Résumé

1. ✅ Ajouter les dépendances JWT et Spring Security
2. ✅ Copier JwtUtil.java
3. ✅ Créer JwtAuthenticationFilter
4. ✅ Configurer SecurityConfig
5. ✅ Utiliser Authentication dans les contrôleurs
6. ✅ Tester avec le token JWT

Votre microservice est maintenant sécurisé et peut valider les tokens JWT émis par le service d'authentification !
