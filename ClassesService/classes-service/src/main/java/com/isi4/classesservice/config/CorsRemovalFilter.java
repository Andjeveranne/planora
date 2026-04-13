package com.isi4.classesservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

/**
 * Filtre CORS désactivé - la configuration CORS est maintenant gérée par SecurityConfig
 */
//@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsRemovalFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        filterChain.doFilter(request, new HttpServletResponseWrapper(response) {
            @Override
            public void addHeader(String name, String value) {
                if ("Access-Control-Allow-Origin".equalsIgnoreCase(name)) return;
                super.addHeader(name, value);
            }

            @Override
            public void setHeader(String name, String value) {
                if ("Access-Control-Allow-Origin".equalsIgnoreCase(name)) return;
                super.setHeader(name, value);
            }
        });
    }
}
