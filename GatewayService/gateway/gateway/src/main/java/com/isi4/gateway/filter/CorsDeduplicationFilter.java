package com.isi4.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Supprime le header Access-Control-Allow-Origin des réponses des microservices
 * pour éviter le doublon avec celui ajouté par le Gateway.
 */
@Component
public class CorsDeduplicationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            // Si plusieurs valeurs ACAO, garder uniquement la première
            if (headers.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)) {
                String first = headers.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, first);
            }
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
