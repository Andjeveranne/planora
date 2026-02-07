package com.isi4.gateway.filter;

import com.isi4.gateway.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GlobalJwtFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        
        System.out.println("=== Global JWT Filter ===");
        System.out.println("Method: " + method + ", Path: " + path);
        System.out.println("Full URI: " + request.getURI());
        
        // Skip JWT validation for public endpoints
        if (isPublicEndpoint(path)) {
            System.out.println("✓ Public endpoint detected - skipping JWT validation");
            return chain.filter(exchange);
        }

        System.out.println("✗ Protected endpoint - JWT validation required");

        // Extract JWT token
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("✗ No valid Authorization header found");
            return onError(exchange, "Token manquant", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        
        // Validate token
        if (!jwtUtil.validateToken(token)) {
            System.out.println("✗ Invalid token");
            return onError(exchange, "Token invalide", HttpStatus.UNAUTHORIZED);
        }

        // Add user information to headers for downstream services
        try {
            String userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", role)
                .build();
            
            System.out.println("✓ Token valid - forwarding with user info");
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            System.out.println("✗ Error extracting token info: " + e.getMessage());
            return onError(exchange, "Erreur lors de l'extraction des informations du token", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicEndpoint(String path) {
        System.out.println("Checking if path is public: '" + path + "'");
        
        boolean isPublic = path.startsWith("/api/auth/") || 
               path.equals("/api/auth") ||
               path.startsWith("/debug/") ||
               path.startsWith("/actuator/");
        
        System.out.println("Result: " + (isPublic ? "PUBLIC" : "PROTECTED"));
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

    @Override
    public int getOrder() {
        return -100; // High priority
    }
}