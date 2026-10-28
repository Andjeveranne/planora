package com.isi4.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // ========== ROUTES PUBLIQUES (pas de JWT requis) ==========
            
            // Route pour l'authentification - DOIT ÊTRE EN PREMIER
            .route("authentification-service-public", r -> r
                .path("/api/auth/**")
                .uri("lb://authentification-service"))
            
            // Route pour les endpoints de debug du Gateway
            .route("gateway-debug", r -> r
                .path("/debug/**")
                .uri("forward:/"))
            
            // ========== ROUTES PROTÉGÉES (JWT requis via GlobalJwtFilter) ==========
            
            // Route pour le service utilisateurs
            .route("user-service", r -> r
                .path("/api/utilisateurs/**", "/api/users/**", "/api/courses/**", "/api/enseignant-ues/**", "/api/attendance/**")
                .uri("lb://user-service"))

            // Route pour le service des Unités d'enseignement (UE)
            .route("ue-service", r -> r
                .path("/api/ues/**")
                .uri("lb://ue-service"))
            
            // Route pour le service planning
            .route("planning-service", r -> r
                .path("/api/planning/**", "/api/disponibilites/**", "/api/emplois-du-temps/**")
                .uri("lb://planning-service"))
            
            // Route pour le service des salles (à implémenter)
            .route("salle-service", r -> r
                .path("/api/salles/**", "/api/rooms/**")
                .uri("lb://salle-service"))

            // Route pour le service des classes et filières
            .route("classes-service", r -> r
                .path("/api/classes/**", "/api/filieres/**", "/api/specialites/**")
                .uri("lb://classes-service"))
            
            // Route pour le service des écoles (CRUD)
            .route("ecole-service", r -> r
                .path("/api/ecoles/**", "/api/schools/**")
                .uri("lb://ecole-service"))
            
            // Route pour le service de réservation (à implémenter)
            .route("reservation-service", r -> r
                .path("/api/reservations/**", "/api/bookings/**")
                .uri("lb://reservation-service"))
            
            // Route pour le service de notification (à implémenter)
            .route("notification-service", r -> r
                .path("/api/notifications/**", "/api/emails/**")
                .uri("lb://notification-service"))
            
            // Route pour le service de reporting (à implémenter)
            .route("reporting-service", r -> r
                .path("/api/reports/**", "/api/analytics/**")
                .uri("lb://reporting-service"))
            
            // Route pour le service d'intégration (à implémenter)
            .route("integration-service", r -> r
                .path("/api/integration/**", "/api/sync/**")
                .uri("lb://integration-service"))
            
            .build();
    }
}