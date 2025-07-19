package com.multipagos.p2p_backend.backend.security.filter; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.security.JwtProvider; // Importa tu JwtProvider
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Para cargar detalles del usuario
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Para que Spring lo gestione como un bean
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService; // Para cargar el usuario de la DB (lo crearemos después)

    public JwtAuthenticationFilter(JwtProvider jwtProvider, UserDetailsService userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization"); // Obtener el encabezado Authorization
        final String jwt;
        final String userEmail;

        // 1. Verificar si el encabezado Authorization existe y tiene el formato correcto "Bearer <token>"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Si no hay token o no es Bearer, sigue la cadena de filtros
            return;
        }

        // 2. Extraer el token JWT del encabezado
        jwt = authHeader.substring(7); // Quitar "Bearer "

        // 3. Extraer el email del sujeto del token
        try {
            userEmail = jwtProvider.extractUsername(jwt);
        } catch (Exception e) {
            // Si el token es inválido (expirado, firma incorrecta, etc.), no podemos extraer el email
            // Simplemente logueamos y dejamos que Spring Security maneje la autenticación fallida más adelante
            System.err.println("Error al extraer email del token JWT: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Validar el token y autenticar al usuario si aún no está autenticado en el contexto de seguridad
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Cargar los detalles del usuario desde tu UserDetailsService
            // (Esto implicará buscar el usuario en tu base de datos)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Validar el token y que el usuario exista
            if (jwtProvider.validateToken(jwt) && userDetails != null) {
                // Si el token es válido, crear un objeto de autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establecer el usuario en el contexto de seguridad de Spring
                // Esto es lo que "autentica" al usuario para la petición actual
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response); // Continuar con la cadena de filtros
    }
}