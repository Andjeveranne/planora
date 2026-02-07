package com.isi4.gateway.filter;

import com.isi4.gateway.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// @Component - DISABLED: Using GlobalJwtFilter instead
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            
            System.out.println("JWT Filter - Processing path: " + path);
            
            // Vérifier si l'endpoint nécessite une authentification
            if (isPublicEndpoint(path)) {
                System.out.println("JWT Filter - Public endpoint, skipping authentication: " + path);
                return chain.filter(exchange);
            }

            System.out.println("JWT Filter - Protected endpoint, checking JWT: " + path);

            // Extraire le token JWT
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("JWT Filter - No valid Authorization header found");
                return onError(exchange, "Token manquant", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            
            // Valider le token
            if (!jwtUtil.validateToken(token)) {
                System.out.println("JWT Filter - Invalid token");
                return onError(exchange, "Token invalide", HttpStatus.UNAUTHORIZED);
            }

            // Ajouter les informations utilisateur aux headers pour les services downstream
            try {
                String userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .build();
                
                System.out.println("JWT Filter - Token valid, forwarding request with user info");
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                System.out.println("JWT Filter - Error extracting token info: " + e.getMessage());
                return onError(exchange, "Erreur lors de l'extraction des informations du token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicEndpoint(String path) {
        boolean isPublic = path.startsWith("/api/auth/") || 
               path.equals("/api/auth/login") || 
               path.equals("/api/auth/register") || 
               path.equals("/api/auth/forgot-password") || 
               path.equals("/api/auth/reset-password") ||
               path.equals("/api/auth/domain-validation") ||
               path.equals("/api/auth/health") ||
               path.equals("/api/auth");
        
        System.out.println("JWT Filter - isPublicEndpoint(" + path + ") = " + isPublic);
        return isPublic;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"" + err + "\"}";
        byte[] bytes = body.getBytes();
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    public static class Config {
        // Configuration properties if needed
    }
}