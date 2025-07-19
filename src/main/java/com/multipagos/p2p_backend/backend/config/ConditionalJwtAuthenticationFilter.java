package com.multipagos.p2p_backend.backend.config;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import com.multipagos.p2p_backend.backend.security.filter.JwtAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ConditionalJwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RequestMatcher publicEndpoints;
    
    public ConditionalJwtAuthenticationFilter(JwtAuthenticationFilter jwtAuthenticationFilter, 
                                            RequestMatcher publicEndpoints) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.publicEndpoints = publicEndpoints;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Si la ruta es pública, omitir el filtro JWT
        if (publicEndpoints.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Para rutas protegidas, aplicar el filtro JWT
        jwtAuthenticationFilter.doFilter(request, response, filterChain);
    }
}